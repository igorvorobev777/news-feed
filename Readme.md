# News Feed Service

Сервис ленты новостей с возможностью создания публикаций, комментариев, лайков и модерации контента.

## Описание предметной области

Проект представляет собой API для управления новостной лентой. Пользователи могут:
- Создавать, редактировать и удалять посты
- Публиковать посты (смена статуса)
- Оставлять комментарии к публикациям
- Ставить лайки
- Подавать жалобы на контент

Администраторы могут:
- Просматривать жалобы
- Модерировать контент

### Сущности
- **Post** — публикация (статусы: CREATED, PUBLISHED, BLOCKED)
- **Comment** — комментарий к посту
- **Like** — лайк поста
- **Report** — жалоба на пост (статусы: OPEN, CLOSED)
- **User** — пользователь с ролями USER / ADMIN
- **Subscription** — лента новостей генерируется исходя из подписок
- **FeedItem** — элемент ленты (привязан к конкретному пользователю, содержит скор-ранг для этого пользователя)
- **Notification**

## Схема сущностей

@startuml

entity users {
  id : BIGINT <<PK>>
  login : VARCHAR(50)
  email : VARCHAR(100)
  password_hash : VARCHAR(255)
  role : VARCHAR(20)
  created_at : TIMESTAMP
}

entity posts {
  id : BIGINT <<PK>>
  author_id : BIGINT <<FK>>
  text : TEXT
  status : VARCHAR(20)
  created_at : TIMESTAMP
  updated_at : TIMESTAMP
}

entity comments {
  id : BIGINT <<PK>>
  post_id : BIGINT <<FK>>
  author_id : BIGINT <<FK>>
  text : VARCHAR(1000)
  status : VARCHAR(20)
  created_at : TIMESTAMP
}

entity likes {
  id : BIGINT <<PK>>
  post_id : BIGINT <<FK>>
  user_id : BIGINT <<FK>>
  created_at : TIMESTAMP
}

entity reports {
  id : BIGINT <<PK>>
  post_id : BIGINT <<FK>>
  author_id : BIGINT <<FK>>
  reason : VARCHAR(500)
  status : VARCHAR(20)
  version : BIGINT
  created_at : TIMESTAMP
}

entity subscriptions {
  id : BIGINT <<PK>>
  follower_id : BIGINT <<FK>>
  followed_id : BIGINT <<FK>>
}

entity feed_items {
  id : BIGINT <<PK>>
  user_id : BIGINT <<FK>>
  post_id : BIGINT <<FK>>
  score_rank : BIGINT
  view_status : VARCHAR(20)
  generated_at : TIMESTAMP
}

entity notifications {
  id : BIGINT <<PK>>
  recipient_id : BIGINT <<FK>>
  text : VARCHAR(500)
  is_read : BOOLEAN
  created_at : TIMESTAMP
}

' === Связи пользователя ===
users ||--o{ posts : "creates"
users ||--o{ comments : "writes"
users ||--o{ likes : "puts"
users ||--o{ reports : "submits"

' === Подписки (N:M между пользователями) ===
users ||--o{ subscriptions : "follows (follower)"
users ||--o{ subscriptions : "is followed by (followed)"

' === Связи поста ===
posts ||--o{ comments : "has"
posts ||--o{ likes : "receives"
posts ||--o{ reports : "receives"

' === Лента ===
users ||--o{ feed_items : "receives in feed"
posts ||--o{ feed_items : "distributed to"

' === Уведомления ===
users ||--o{ notifications : "receives"

@enduml


## ИНФРАСТРУКТУРНЫЕ РЕШЕНИЯ
========================

Реализовано 4 инфраструктурных пункта из списка требований:
Spring Security, ApplicationEventPublisher, @Cacheable/@CacheEvict, @Scheduled.


1. SPRING SECURITY (JWT) + РОЛИ USER/ADMIN
-------------------------------------------

Что реализовано:
- JWT-аутентификация через endpoints /api/v1/auth/register и /api/v1/auth/login
- Две роли: USER и ADMIN
- Роли хранятся в таблице users в колонке role (значения 'USER' / 'ADMIN')
- Ограничение доступа к endpoint'ам через SecurityConfig:
    * POST /api/v1/posts/{id}/block — только ADMIN
    * PUT /api/v1/reports/{id}/moderate — только ADMIN
    * POST/PUT/DELETE запросы — только аутентифицированные пользователи
    * GET /api/v1/posts/** — публичные (чтение постов без токена)
- JwtAuthFilter извлекает токен из заголовка Authorization и устанавливает SecurityContext

К сожалению тут в реализации есть уязвимость, 
    а именно что userId передается как аргумент в сервисы и поэтому пользователь может "притвориться другим". Не успел  это поправить)

Ключевые файлы:
- model/AppUser.java, model/Role.java
- security/JwtAuthFilter.java
- service/JwtService.java, service/CustomUserDetailsService.java
- config/SecurityConfig.java
- controller/AuthController.java

Почему выбрано:
- Безопасность
- Разделение ролей необходимо для модерации контента (блокировка постов, обработка жалоб)


2. APPLICATIONEVENTPUBLISHER (2 СОБЫТИЯ + 2 ОБРАБОТЧИКА)
---------------------------------------------------------

Что реализовано:
- Событие 1: PostPublishedEvent
    Публикуется в PostService.publishPost() после успешной публикации поста.
    Обработчик: PostPublishedNotificationHandler — находит всех подписчиков автора
    через SubscriptionRepository и создаёт для каждого уведомление в NotificationRepository.
    Обработчик работает асинхронно.

- Событие 2: ReportClosedEvent
    Публикуется в ReportService.moderateReport() после закрытия жалобы.
    Обработчик: ReportClosedEventHandler — логирует результат модерации
    (был ли пост заблокирован или жалоба закрыта без блокировки).

Ключевые файлы:
- event/PostPublishedEvent.java, event/ReportClosedEvent.java
- event/PostPublishedNotificationHandler.java, event/ReportClosedEventHandler.java

Почему выбрано:
- Асинхронная обработка событий не блокирует основной поток


3. @CACHEABLE / @CACHEEVICT ДЛЯ КЭШИРОВАНИЯ ПОСТОВ
---------------------------------------------------

Что реализовано:
- Включено кэширование через @EnableCaching в главном классе приложения
- Настроен CacheManager с двумя кэшами: "posts" (список) и "postById" (один пост)
-findById(Long id) — аннотация @Cacheable(value = "postById", key = "#id")
    Кэширует пост по его ID. При повторном чтении того же поста данные берутся из кэша,
    без обращения к БД.
- create(PostRequest) — аннотация @CacheEvict(value = "posts", allEntries = true)
    При создании поста очищается кэш списка постов.
- update(Long id, ...) — аннотации @CachePut + @CacheEvict
    @CachePut обновляет кэш конкретного поста по ID,
    @CacheEvict очищает кэш списка постов.
- delete(Long id, ...) — аннотация @CacheEvict(value = {"posts", "postById"}, allEntries = true)
    При удалении поста очищаются оба кэша.
- publishPost(Long id, ...) — аннотация @CacheEvict(value = {"posts", "postById"}, allEntries = true)
    При публикации поста очищаются оба кэша.

Ключевые файлы:
- config/CacheConfig.java
- service/PostService.java

Почему выбрано:
- Уменьшение нагрузки на БД для часто читаемых постов


4. ПЛАНИРОВЩИК (@SCHEDULED) + ЛОГИРОВАНИЕ РЕЗУЛЬТАТА
-----------------------------------------------------

Что реализовано:
- Включено планирование через @EnableScheduling в главном классе приложения
- Сервис ScheduledTasksService с методом deleteOldUnpublishedPosts()
- Запускается по расписанию (каждый день в 03:00) через аннотацию @Scheduled
- Удаляет посты со статусом CREATED, которым более 30 дней
- Результат работы логируется: сколько постов было удалено за один запуск

Ключевые файлы:
- service/ScheduledTasksService.java

Почему выбрано:
- Автоматическая очистка устаревшего контента без участия пользователя
- Экономия места в БД за счёт удаления черновиков, которые пользователь забросил
- Логирование позволяет отслеживать работу планировщика и диагностировать проблемы


## Жизненный цикл поста
CREATED ──(publish)──> PUBLISHED ──(moderate)──> BLOCKED
    │                       │
    └──(delete)──> X      └──(генерация feed_items для подписчиков)

## Генерация ленты
1. Автор публикует пост (PUBLISHED)
2. PostService вызывает FeedService.generateFeedItemsForPost() (@Async)
3. FeedService находит всех подписчиков автора через subscriptions
4. Для каждого подписчика создаётся feed_item с score_rank
5. Пользователь читает ленту через GET /api/v1/feed -> SELECT из feed_items

##  Примеры curl
1. CRUD: Создание поста
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"authorId":1,"text":"пост 1"}'
2. CRUD: Получение поста
curl -X 'GET' \
  'http://localhost:8080/api/v1/posts/1' \
  -H 'accept: */*'

3. CRUD: Удаление поста
curl -X 'DELETE' \
  'http://localhost:8080/api/v1/posts/2?userId=3' \
  -H 'accept: */*'

4. Бизнес-операция: Публикация поста
curl -X 'POST' \
  'http://localhost:8080/api/v1/posts/1/publish?userId=1' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc4MDk2Mzk1MywiZXhwIjoxNzgxMDUwMzUzfQ.akjFzIbC6g9jhZcDXUex2sYoZAGZGFXYKoqAPDvDnWQ' \
  -d ''

5. Бизнес-операция: Поставить лайк (P.s сначала нужно создать пост и опубликовать его)
curl -X 'POST' \
  'http://localhost:8080/api/v1/posts/1/likes' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc4MDk2Mzk1MywiZXhwIjoxNzgxMDUwMzUzfQ.akjFzIbC6g9jhZcDXUex2sYoZAGZGFXYKoqAPDvDnWQ' \
  -H 'Content-Type: application/json' \
  -d '{
  "userId": 1
}'

6. Бизнес-операция: Модерация жалобы
curl -X 'PUT' \
  'http://localhost:8080/api/v1/reports/1/moderate?shouldBlockPost=true' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc4MDk2Mzk1MywiZXhwIjoxNzgxMDUwMzUzfQ.akjFzIbC6g9jhZcDXUex2sYoZAGZGFXYKoqAPDvDnWQ'

## Тесты
Запуск всех тестов: mvn test

| Тип теста | Класс | Что проверяет |
|-----------|-------|---------------|
| Unit | `PostServiceTest` | Права доступа при публикации/обновлении, проверка статусов, обработка отсутствующих постов |
| Unit | `ReportServiceTest` | Модерация жалоб, блокировка постов, валидация состояния жалобы |
| `@DataJpaTest` | `PostRepositoryTest` | Сложный JPQL-запрос `findFiltered()` с фильтрацией по статусу, автору и тексту |
| Конкурентный | `ReportModerationRaceConditionTest` | Оптимистичный локинг при одновременной модерации жалобы двумя админами — ровно одна модерация успешна, вторая получает `OptimisticLockException` |

## Swagger UI
открыть в браузере http://localhost:8080/swagger-ui/index.html
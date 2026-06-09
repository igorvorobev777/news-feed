package com.example.demo.event;

import com.example.demo.model.Notification;
import com.example.demo.model.Subscription;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

//Обработчик события публикации поста.
//Отправляет уведомления всем подписчикам автора.

@Component
public class PostPublishedNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(PostPublishedNotificationHandler.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    
    @Async
    @EventListener
    public void handlePostPublished(PostPublishedEvent event) {
        log.info("Обработка события PostPublishedEvent: postId={}, authorId={}", 
                event.getPostId(), event.getAuthorId());

        // Находим всех подписчиков автора
        List<Subscription> subscribers = subscriptionRepository.findByFollowedId(event.getAuthorId());
        
        if (subscribers.isEmpty()) {
            log.info("ℹУ автора {} нет подписчиков, уведомления не отправлены", event.getAuthorId());
            return;
        }

        // Создаём уведомления для каждого подписчика
        int notificationsCreated = 0;
        for (Subscription subscription : subscribers) {
            Notification notification = new Notification();
            notification.setRecipientId(subscription.getFollowerId());
            notification.setText("Пользователь " + event.getAuthorId() + 
                               " опубликовал новый пост (ID=" + event.getPostId() + ")");
            notification.setIsRead(false);
            notificationRepository.save(notification);
            notificationsCreated++;
        }
        
        log.info("Отправлено {} уведомлений подписчикам о посте {}", 
                notificationsCreated, event.getPostId());
    }
}
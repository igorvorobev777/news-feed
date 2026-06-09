-- Вставка тестовых пользователей
-- пароль для входа 123456 везде
INSERT INTO users (login, email, password_hash, role, created_at) VALUES
('admin', 'admin@example.com', '$2a$10$jL46RqFAX86RNjVySXEUee.OxB2qRQnWn/tvURkkLHgdq.5OxUKay', 'ADMIN', NOW()),
('user1', 'user1@example.com', '$2a$10$jL46RqFAX86RNjVySXEUee.OxB2qRQnWn/tvURkkLHgdq.5OxUKay', 'USER', NOW()),
('user2', 'user2@example.com', '$2a$10$jL46RqFAX86RNjVySXEUee.OxB2qRQnWn/tvURkkLHgdq.5OxUKay', 'USER', NOW());

-- Сброс sequence, чтобы следующие вставки шли с id=4
ALTER SEQUENCE users_id_seq RESTART WITH 4;
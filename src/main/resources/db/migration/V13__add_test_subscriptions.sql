-- Пользователь 2 подписан на пользователя 1
INSERT INTO subscriptions (follower_id, followed_id) VALUES (2, 1)
ON CONFLICT (follower_id, followed_id) DO NOTHING;

INSERT INTO subscriptions (follower_id, followed_id) VALUES (3, 1)
ON CONFLICT (follower_id, followed_id) DO NOTHING;

INSERT INTO subscriptions (follower_id, followed_id) VALUES (3, 2)
ON CONFLICT (follower_id, followed_id) DO NOTHING;

INSERT INTO subscriptions (follower_id, followed_id) VALUES (1, 2)
ON CONFLICT (follower_id, followed_id) DO NOTHING;
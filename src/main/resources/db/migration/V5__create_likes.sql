CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    --один пользователь может лайкнуть пост только один раз
    CONSTRAINT uk_post_user_like UNIQUE (post_id, user_id)
);

-- Индекс для быстрого поиска лайков пользователя или проверки дублей
CREATE INDEX idx_likes_post_user ON likes(post_id, user_id);
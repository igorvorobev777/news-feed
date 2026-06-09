CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    text VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE' CHECK (status IN ('VISIBLE', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Индекс для быстрого доступа к комментариям конкретного поста с сортировкой по времени
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at DESC);
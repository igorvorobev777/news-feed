CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id),
    text TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'PUBLISHED', 'BLOCKED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Индекс для фильтрации по автору и статусу, а также для сортировки постов по дате
CREATE INDEX idx_posts_author_status_created ON posts(author_id, status, created_at DESC);
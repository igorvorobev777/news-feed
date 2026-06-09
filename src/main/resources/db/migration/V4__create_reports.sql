CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- фильтрация жалоб по посту и статусу
CREATE INDEX idx_reports_post_status ON reports(post_id, status);
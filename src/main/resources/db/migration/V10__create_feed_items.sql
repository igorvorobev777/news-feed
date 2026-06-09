CREATE TABLE feed_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    score_rank BIGINT NOT NULL DEFAULT 0,
    view_status VARCHAR(20) NOT NULL DEFAULT 'UNSEEN' CHECK (view_status IN ('UNSEEN', 'SEEN')),
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_feed_item UNIQUE (user_id, post_id)
);

CREATE INDEX idx_feed_items_user_score ON feed_items(user_id, score_rank DESC);
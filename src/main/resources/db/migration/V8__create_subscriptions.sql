CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followed_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_subscription UNIQUE (follower_id, followed_id),
    CONSTRAINT chk_no_self_subscription CHECK (follower_id != followed_id)
);

CREATE INDEX idx_subscriptions_followed ON subscriptions(followed_id);
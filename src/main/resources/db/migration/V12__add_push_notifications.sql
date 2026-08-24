ALTER TABLE events
    ADD COLUMN notification_sent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE user_device_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(20)  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_device_tokens_user_id ON user_device_tokens (user_id);
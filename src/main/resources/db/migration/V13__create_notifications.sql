CREATE TABLE notifications
(
    id                BIGSERIAL    NOT NULL,
    user_id           BIGINT       NOT NULL,
    type              VARCHAR(50)  NOT NULL,
    title             VARCHAR(200) NOT NULL,
    body              VARCHAR(1000) NOT NULL,
    target_type       VARCHAR(30),
    target_id         BIGINT,
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    deduplication_key VARCHAR(150) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at           TIMESTAMPTZ,

    CONSTRAINT pk_notifications
        PRIMARY KEY (id),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_notifications_user_deduplication
        UNIQUE (user_id, deduplication_key),

    CONSTRAINT chk_notifications_target
        CHECK (
            (target_type IS NULL AND target_id IS NULL)
                OR
            (target_type IS NOT NULL AND target_id IS NOT NULL)
            ),

    CONSTRAINT chk_notifications_read_state
        CHECK (
            (is_read = FALSE AND read_at IS NULL)
                OR
            (is_read = TRUE AND read_at IS NOT NULL)
            )
);

CREATE INDEX idx_notifications_user_created_at
    ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON notifications (user_id, created_at DESC)
    WHERE is_read = FALSE;
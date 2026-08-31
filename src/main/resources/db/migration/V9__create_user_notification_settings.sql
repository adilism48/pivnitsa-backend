CREATE TABLE user_notification_settings
(
    user_id                       BIGINT      NOT NULL,
    event_notifications_enabled  BOOLEAN     NOT NULL DEFAULT FALSE,
    booking_notifications_enabled BOOLEAN    NOT NULL DEFAULT FALSE,
    version                       BIGINT      NOT NULL DEFAULT 0,
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_notification_settings
        PRIMARY KEY (user_id),

    CONSTRAINT fk_user_notification_settings_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

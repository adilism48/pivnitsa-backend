CREATE TABLE otp_codes
(
    id          BIGSERIAL PRIMARY KEY,

    phone       VARCHAR(30) NOT NULL,

    code        VARCHAR(6) NOT NULL,

    channel     VARCHAR(20) NOT NULL,

    failed_attempts    INTEGER NOT NULL DEFAULT 0,

    sent_at     TIMESTAMPTZ NOT NULL,

    expires_at  TIMESTAMPTZ NOT NULL,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    verified    BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE INDEX idx_otp_codes_phone_verified_expires
    ON otp_codes(phone, verified, expires_at);

CREATE INDEX idx_otp_codes_latest
    ON otp_codes(phone, created_at DESC);
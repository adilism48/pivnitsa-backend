CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(30)  NOT NULL UNIQUE,
    email      VARCHAR(255) UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE club_tables
(
    id           BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(30) NOT NULL UNIQUE,
    capacity     INTEGER     NOT NULL CHECK (capacity > 0),
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    banner_url  VARCHAR(1000),
    starts_at   TIMESTAMPTZ  NOT NULL,
    ends_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_event_period CHECK (ends_at IS NULL OR ends_at > starts_at)
);

CREATE TABLE bookings
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT         NOT NULL REFERENCES users (id),
    club_table_id BIGINT         NOT NULL REFERENCES club_tables (id),
    event_id      BIGINT         REFERENCES events (id),
    booking_at    TIMESTAMPTZ    NOT NULL,
    status        VARCHAR(30)    NOT NULL,
    amount        NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_status CHECK (
        status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'COMPLETED')
    )
);

CREATE TABLE payments
(
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT         NOT NULL REFERENCES bookings (id),
    provider            VARCHAR(50)    NOT NULL,
    provider_payment_id VARCHAR(255),
    amount              NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    status              VARCHAR(30)    NOT NULL,
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_status CHECK (
        status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')
    ),
    CONSTRAINT uq_payment_provider_id UNIQUE (provider, provider_payment_id)
);

CREATE INDEX idx_events_starts_at ON events (starts_at);
CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_table_date ON bookings (club_table_id, booking_at);
CREATE INDEX idx_bookings_event_id ON bookings (event_id);
CREATE INDEX idx_payments_booking_id ON payments (booking_id);

CREATE UNIQUE INDEX uq_active_booking_table_date
    ON bookings (club_table_id, booking_at)
    WHERE status IN ('PENDING_PAYMENT', 'CONFIRMED');

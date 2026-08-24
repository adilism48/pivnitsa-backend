CREATE TABLE table_unavailability_periods
(
    id            BIGSERIAL PRIMARY KEY,
    club_table_id BIGINT      NOT NULL REFERENCES club_tables (id),
    starts_at     TIMESTAMPTZ NOT NULL,
    ends_at       TIMESTAMPTZ NOT NULL,
    reason        VARCHAR     NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_unavailability_period CHECK ( ends_at > starts_at )
);
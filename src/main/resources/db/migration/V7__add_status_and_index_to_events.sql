ALTER TABLE events
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    ADD CONSTRAINT chk_event_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

CREATE INDEX idx_events_published_upcoming
    ON events (starts_at ASC)
    WHERE status = 'PUBLISHED';
CREATE TABLE halls
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO halls (name) VALUES ('Зал 1'), ('Зал 2');

ALTER TABLE club_tables
    ADD COLUMN hall_id BIGINT REFERENCES halls (id);

UPDATE club_tables SET hall_id = (SELECT id FROM halls WHERE name = 'Зал 1');

ALTER TABLE club_tables
    ALTER COLUMN hall_id SET NOT NULL;

ALTER TABLE club_tables
    ADD COLUMN position_x NUMERIC(5, 2) CHECK (position_x >= 0 AND position_x <= 100),
    ADD COLUMN position_y NUMERIC(5, 2) CHECK (position_y >= 0 AND position_y <= 100),
    ADD COLUMN category   VARCHAR(50),
    ADD COLUMN deposit_amount NUMERIC(10, 2);
ALTER TABLE otp_codes
    ADD COLUMN purpose VARCHAR(30);

UPDATE otp_codes
SET purpose = 'REGISTRATION';

ALTER TABLE otp_codes
    ALTER COLUMN purpose SET NOT NULL;

ALTER TABLE otp_codes
    ADD CONSTRAINT chk_otp_purpose
        CHECK (purpose IN ('REGISTRATION', 'LOGIN'));

CREATE INDEX idx_otp_codes_phone_purpose_created
    ON otp_codes(phone, purpose, created_at DESC);
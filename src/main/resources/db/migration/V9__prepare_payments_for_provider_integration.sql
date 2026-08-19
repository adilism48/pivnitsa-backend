-- Старое ограничение не разрешает статус PAID,
-- поэтому сначала удаляем его.
ALTER TABLE payments
DROP CONSTRAINT chk_payment_status;

-- Преобразуем старое название успешного статуса.
UPDATE payments
SET status = 'PAID'
WHERE status = 'SUCCEEDED';

-- Добавляем новые поля.
-- merchant_order_id сначала nullable для совместимости
-- с уже существующими платежами.
ALTER TABLE payments
    ADD COLUMN merchant_order_id VARCHAR(100),
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'KGS',
    ADD COLUMN provider_status VARCHAR(64),
    ADD COLUMN payment_url VARCHAR(2048),
    ADD COLUMN expires_at TIMESTAMPTZ,
    ADD COLUMN refunded_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN failure_code VARCHAR(100),
    ADD COLUMN failure_message VARCHAR(1000),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Присваиваем уникальные технические значения старым платежам.
-- Для новых платежей merchant_order_id будет формировать backend.
UPDATE payments
SET merchant_order_id = 'legacy-payment-' || id
WHERE merchant_order_id IS NULL;

ALTER TABLE payments
    ALTER COLUMN merchant_order_id SET NOT NULL;

-- Приводим типы и размеры к Payment.java.
ALTER TABLE payments
ALTER COLUMN provider TYPE VARCHAR(32),
    ALTER COLUMN provider_payment_id TYPE VARCHAR(128),
    ALTER COLUMN amount TYPE NUMERIC(19, 2);

-- Пересоздаём уникальное ограничение с тем же именем,
-- которое указано в Payment.java.
ALTER TABLE payments
DROP CONSTRAINT uq_payment_provider_id;

ALTER TABLE payments
    ADD CONSTRAINT uk_payments_merchant_order_id
        UNIQUE (merchant_order_id),

    ADD CONSTRAINT uk_payments_provider_payment_id
        UNIQUE (provider, provider_payment_id),

    ADD CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                'PENDING',
                'PAID',
                'FAILED',
                'EXPIRED',
                'PARTIALLY_REFUNDED',
                'REFUNDED'
            )
        ),

    ADD CONSTRAINT chk_payments_amount_positive
        CHECK (amount > 0),

    ADD CONSTRAINT chk_payments_refunded_amount_non_negative
        CHECK (refunded_amount >= 0),

    ADD CONSTRAINT chk_payments_refunded_amount_not_exceeded
        CHECK (refunded_amount <= amount),

    ADD CONSTRAINT chk_payments_currency_format
        CHECK (currency ~ '^[A-Z]{3}$');

-- Индексы для типовых запросов.
CREATE INDEX idx_payments_booking_id
    ON payments (booking_id);

CREATE INDEX idx_payments_status
    ON payments (status);

CREATE INDEX idx_payments_provider_payment_id
    ON payments (provider_payment_id);

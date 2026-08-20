CREATE UNIQUE INDEX uk_payments_one_pending_per_booking
    ON payments (booking_id)
    WHERE status = 'PENDING';

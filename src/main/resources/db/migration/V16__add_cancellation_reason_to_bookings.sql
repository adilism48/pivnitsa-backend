ALTER TABLE bookings
    ADD COLUMN cancellation_reason VARCHAR(255);

-- Для фильтрации по дате и сортировки в админке
CREATE INDEX IF NOT EXISTS idx_booking_booking_at ON bookings(booking_at ASC);

-- Для ускорения JOIN операций
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_club_table_id ON bookings(club_table_id);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
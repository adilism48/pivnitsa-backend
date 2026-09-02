package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.exception.booking.InvalidBookingDataException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class BookingDateValidator {
    private static final long MAX_BOOKING_DAY = 30;

    public void validateBookingDate(OffsetDateTime bookingAt) {
        if (bookingAt.isAfter(OffsetDateTime.now().plusDays(MAX_BOOKING_DAY))) {
            throw new InvalidBookingDataException("Дата брони не может быть более чем через 30 дней");
        }
    }
}

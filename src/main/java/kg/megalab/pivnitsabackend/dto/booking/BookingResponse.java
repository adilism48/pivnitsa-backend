package kg.megalab.pivnitsabackend.dto.booking;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        Long clubTableId,
        Long eventId,
        OffsetDateTime bookingAt,
        BookingStatus status,
        BigDecimal amount,
        Integer guestsCount
) {
}

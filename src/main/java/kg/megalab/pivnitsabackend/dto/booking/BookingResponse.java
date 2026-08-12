package kg.megalab.pivnitsabackend.dto.booking;

import kg.megalab.pivnitsabackend.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookingResponse (
    Long id,
    String tableNumber,
    Integer guestsCount,
    OffsetDateTime bookingAt,
    BigDecimal amount,
    BookingStatus status
    ) {

}

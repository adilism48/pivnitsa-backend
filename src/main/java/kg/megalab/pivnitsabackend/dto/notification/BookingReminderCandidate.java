package kg.megalab.pivnitsabackend.dto.notification;

import java.time.OffsetDateTime;

public record BookingReminderCandidate(
        Long bookingId,
        Long userId,
        OffsetDateTime bookingAt,
        String tableNumber
) {
}

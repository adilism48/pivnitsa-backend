package kg.megalab.pivnitsabackend.dto.notification;

public record EventPublishedEvent(
        Long eventId,
        String eventTitle
) {
}

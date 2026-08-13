package kg.megalab.pivnitsabackend.dto.event;

import kg.megalab.pivnitsabackend.entity.EventStatus;

import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String bannerUrl,
        EventStatus status,
        String shareUrl,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

package kg.megalab.pivnitsabackend.dto.event;

import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String bannerUrl,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
) {
}
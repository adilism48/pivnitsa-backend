package kg.megalab.pivnitsabackend.dto.event;

import java.time.OffsetDateTime;

public record EventBannerResponse(
        Long id,
        String title,
        String bannerUrl,
        OffsetDateTime startsAt
) {
}

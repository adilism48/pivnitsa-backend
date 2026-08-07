package kg.megalab.pivnitsabackend.dto.event;

import java.util.List;

public record EventBannersResponse(
    List<EventBannerResponse> eventBanners
) {
}

package kg.megalab.pivnitsabackend.dto.event;

import java.util.List;

public record EventPageResponse(
        List<EventListItemResponse> items,
        int page,
        int size,
        boolean hasNext
) {
}

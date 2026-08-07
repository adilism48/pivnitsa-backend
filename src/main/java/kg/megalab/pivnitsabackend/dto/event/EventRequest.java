package kg.megalab.pivnitsabackend.dto.event;

import kg.megalab.pivnitsabackend.entity.EventStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public record EventRequest(
        String title,
        String description,
        MultipartFile file,
        EventStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
) {
}

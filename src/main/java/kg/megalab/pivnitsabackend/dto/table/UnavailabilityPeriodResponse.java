package kg.megalab.pivnitsabackend.dto.table;

import java.time.OffsetDateTime;

public record UnavailabilityPeriodResponse(
        Long id,
        Long clubTableId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String reason
) {
}

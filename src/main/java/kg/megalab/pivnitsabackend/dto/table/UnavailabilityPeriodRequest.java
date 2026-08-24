package kg.megalab.pivnitsabackend.dto.table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record UnavailabilityPeriodRequest(
        @NotNull(message = "Дата начала обязательна")
        OffsetDateTime startsAt,
        @NotNull(message = "Дата окончания обязательна")
        OffsetDateTime endsAt,
        @NotBlank(message = "Причина обязательна")
        String reason
) {
}

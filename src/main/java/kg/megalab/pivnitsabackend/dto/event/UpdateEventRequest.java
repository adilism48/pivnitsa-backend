package kg.megalab.pivnitsabackend.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public record UpdateEventRequest(
        @NotBlank(message = "Заголовок не может быть пустым")
        @Size(min = 3, max = 255, message = "Заголовок должен быть от 3 до 255 символов")
        String title,

        @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
        String description,

        @NotBlank
        @Schema(type = "string", format = "binary", description = "Необязательный файл баннера", nullable = true)
        MultipartFile file,

        @NotNull(message = "Статус события обязателен")
        EventStatus status,

        @NotNull(message = "Дата начала обязательна")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime startsAt,

        @NotNull(message = "Дата окончания обязательна")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime endsAt
) {
    public UpdateEventRequest {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
    }
}

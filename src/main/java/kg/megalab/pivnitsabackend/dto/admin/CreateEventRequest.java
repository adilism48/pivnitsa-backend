package kg.megalab.pivnitsabackend.dto.admin;

import jakarta.validation.constraints.*;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public record CreateEventRequest(

        @NotBlank(message = "Заголовок обязателен")
        @Size(max = 255, message = "Заголовок должен быть от 3 до 255 символов")
        String title,

        @Size(max = 1000, message = "Описание не должно превышать 2000 символов")
        String description,

        @NotNull(message = "Файл баннера обязателен")
        MultipartFile file,

        @NotNull(message = "Статус события обязателен")
        EventStatus status,

        @NotNull(message = "Дата начала обязательна")
        @FutureOrPresent(message = "Дата начала не может быть в прошлом")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime startsAt,

        @NotNull(message = "Дата окончания обязательна")
        @Future(message = "Дата окончания должна быть в будущем")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime endsAt
) {
    public CreateEventRequest {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
    }
}

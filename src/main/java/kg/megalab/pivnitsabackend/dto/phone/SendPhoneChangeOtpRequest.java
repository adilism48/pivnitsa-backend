package kg.megalab.pivnitsabackend.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kg.megalab.pivnitsabackend.entity.NotificationChannel;

public record SendPhoneChangeOtpRequest(

        @NotBlank(message = "Новый номер обязателен")
        @Pattern(
                regexp = "^\\+996\\d{9}$",
                message = "Номер должен соответствовать формату +996XXXXXXXXX"
        )
        String newPhone,

        @NotNull(message = "Канал отправки обязателен")
        NotificationChannel channel
) {
}

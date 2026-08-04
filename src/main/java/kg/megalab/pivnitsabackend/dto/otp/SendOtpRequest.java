package kg.megalab.pivnitsabackend.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kg.megalab.pivnitsabackend.entity.NotificationChannel;

public record SendOtpRequest (
    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^\\+[0-9]{10,15}$",
            message = "Phone must be in international format"
    )
    String phone,
    @NotNull(message = "Channel is required")
    NotificationChannel channel
) {

}

package kg.megalab.pivnitsabackend.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kg.megalab.pivnitsabackend.entity.DeviceType;

public record RegisterTokenRequest(
        @NotBlank
        String token,
        @NotNull
        DeviceType deviceType
) {
}

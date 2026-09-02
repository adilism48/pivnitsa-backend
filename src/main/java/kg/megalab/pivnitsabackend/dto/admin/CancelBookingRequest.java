package kg.megalab.pivnitsabackend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record CancelBookingRequest(
        @NotBlank(message = "Причина отмены обязательна")
        String reason
) {
}

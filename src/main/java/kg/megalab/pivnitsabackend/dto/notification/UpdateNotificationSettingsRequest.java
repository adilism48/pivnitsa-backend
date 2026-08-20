package kg.megalab.pivnitsabackend.dto.notification;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationSettingsRequest(

        @NotNull(message = "Event notification preference is required")
        Boolean eventNotificationsEnabled,

        @NotNull(message = "Booking notification preference is required")
        Boolean bookingNotificationsEnabled
) {
}

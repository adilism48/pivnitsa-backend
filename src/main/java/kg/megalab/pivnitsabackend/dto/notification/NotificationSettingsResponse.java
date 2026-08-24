package kg.megalab.pivnitsabackend.dto.notification;

public record NotificationSettingsResponse(
        boolean eventNotificationsEnabled,
        boolean bookingNotificationsEnabled
) {
}

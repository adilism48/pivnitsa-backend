package kg.megalab.pivnitsabackend.dto.notification;

import java.util.Map;

public record PushNotification(
        String title,
        String body,
        Map<String, String> data
) {
}

package kg.megalab.pivnitsabackend.dto.notification;

import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
}

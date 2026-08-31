package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.NotificationPageResponse;
import kg.megalab.pivnitsabackend.dto.notification.NotificationResponse;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    NotificationPageResponse getCurrentUserNotifications(
            String phone,
            int page,
            int size
    );

    NotificationResponse markAsRead(String phone, Long notificationId);

    void createForUsers(
            List<Long> userIds,
            NotificationType type,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            String deduplicationKey
    );

    boolean createForUser(
            Long userId,
            NotificationType type,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            String deduplicationKey
    );

}

package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;

import java.util.List;

public interface UserNotificationSettingsService {

    NotificationSettingsResponse getCurrentUserSettings(String phone);

    NotificationSettingsResponse updateCurrentUserSettings(
            String phone,
            UpdateNotificationSettingsRequest request
    );

    List<Long> getAllEventSubscribedUserIds();
}

package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;

public interface UserNotificationSettingsService {

    NotificationSettingsResponse getCurrentUserSettings(String phone);

    NotificationSettingsResponse updateCurrentUserSettings(
            String phone,
            UpdateNotificationSettingsRequest request
    );
}

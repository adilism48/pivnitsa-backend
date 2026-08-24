package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.entity.UserNotificationSettings;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.UserNotificationSettingsRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationSettingsServiceImpl
        implements UserNotificationSettingsService {

    private final UserNotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationSettingsResponse getCurrentUserSettings(String phone) {
        User user = getVerifiedUser(phone);

        return settingsRepository.findById(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> new NotificationSettingsResponse(
                        false,
                        false
                ));
    }

    @Override
    @Transactional
    public NotificationSettingsResponse updateCurrentUserSettings(
            String phone,
            UpdateNotificationSettingsRequest request
    ) {
        User user = getVerifiedUser(phone);

        UserNotificationSettings settings = settingsRepository
                .findById(user.getId())
                .orElseGet(() -> UserNotificationSettings.builder()
                        .userId(user.getId())
                        .build());

        settings.setEventNotificationsEnabled(
                request.eventNotificationsEnabled()
        );

        settings.setBookingNotificationsEnabled(
                request.bookingNotificationsEnabled()
        );

        UserNotificationSettings savedSettings =
                settingsRepository.save(settings);

        return toResponse(savedSettings);
    }

    @Override
    public List<Long> getAllEventSubscribedUserIds() {
        return settingsRepository.findAllEventSubscribedUserIds();
    }

    private User getVerifiedUser(String phone) {
        return userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден")
                );
    }

    private NotificationSettingsResponse toResponse(
            UserNotificationSettings settings
    ) {
        return new NotificationSettingsResponse(
                settings.isEventNotificationsEnabled(),
                settings.isBookingNotificationsEnabled()
        );
    }
}

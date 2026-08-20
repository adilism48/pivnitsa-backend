package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.UserNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationSettingsRepository
    extends JpaRepository<UserNotificationSettings, Long> {

    boolean existsByUserIdAndEventNotificationsEnabledTrue(
            Long userId
    );

    boolean existsByUserIdAndBookingNotificationsEnabledTrue(
            Long userId
    );
}

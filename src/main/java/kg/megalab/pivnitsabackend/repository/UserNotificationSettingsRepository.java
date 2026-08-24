package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.UserNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserNotificationSettingsRepository
    extends JpaRepository<UserNotificationSettings, Long> {

    boolean existsByUserIdAndEventNotificationsEnabledTrue(
            Long userId
    );

    boolean existsByUserIdAndBookingNotificationsEnabledTrue(
            Long userId
    );

    @Query("SELECT s.userId FROM UserNotificationSettings s WHERE s.eventNotificationsEnabled = true")
    List<Long> findAllEventSubscribedUserIds();
}

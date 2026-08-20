package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.entity.UserNotificationSettings;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.UserNotificationSettingsRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationSettingsServiceImplTest {

    private static final String PHONE = "+996700123456";
    private static final Long USER_ID = 1L;

    @Mock
    private UserNotificationSettingsRepository settingsRepository;

    @Mock
    private UserRepository userRepository;

    private UserNotificationSettingsServiceImpl settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new UserNotificationSettingsServiceImpl(
                settingsRepository,
                userRepository
        );
    }

    @Test
    void shouldReturnSavedSettings() {
        User user = verifiedUser();
        UserNotificationSettings settings = UserNotificationSettings.builder()
                .userId(USER_ID)
                .eventNotificationsEnabled(true)
                .bookingNotificationsEnabled(false)
                .build();

        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(user));
        when(settingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));

        NotificationSettingsResponse response =
                settingsService.getCurrentUserSettings(PHONE);

        assertTrue(response.eventNotificationsEnabled());
        assertFalse(response.bookingNotificationsEnabled());
        verify(settingsRepository, never()).save(settings);
    }

    @Test
    void shouldReturnDisabledDefaultsWhenSettingsDoNotExist() {
        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(settingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        NotificationSettingsResponse response =
                settingsService.getCurrentUserSettings(PHONE);

        assertFalse(response.eventNotificationsEnabled());
        assertFalse(response.bookingNotificationsEnabled());
        verify(settingsRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldUpdateExistingSettings() {
        UserNotificationSettings settings = UserNotificationSettings.builder()
                .userId(USER_ID)
                .eventNotificationsEnabled(false)
                .bookingNotificationsEnabled(true)
                .build();

        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(settingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));
        when(settingsRepository.save(settings)).thenReturn(settings);

        NotificationSettingsResponse response =
                settingsService.updateCurrentUserSettings(
                        PHONE,
                        new UpdateNotificationSettingsRequest(true, false)
                );

        assertTrue(settings.isEventNotificationsEnabled());
        assertFalse(settings.isBookingNotificationsEnabled());
        assertTrue(response.eventNotificationsEnabled());
        assertFalse(response.bookingNotificationsEnabled());
        verify(settingsRepository).save(settings);
    }

    @Test
    void shouldCreateSettingsWhenTheyDoNotExist() {
        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(settingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());
        when(settingsRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingsResponse response =
                settingsService.updateCurrentUserSettings(
                        PHONE,
                        new UpdateNotificationSettingsRequest(true, true)
                );

        assertTrue(response.eventNotificationsEnabled());
        assertTrue(response.bookingNotificationsEnabled());
        verify(settingsRepository).save(
                org.mockito.ArgumentMatchers.argThat(settings ->
                        USER_ID.equals(settings.getUserId())
                                && settings.isEventNotificationsEnabled()
                                && settings.isBookingNotificationsEnabled()
                )
        );
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> settingsService.getCurrentUserSettings(PHONE)
        );

        verify(settingsRepository, never()).findById(USER_ID);
    }

    private User verifiedUser() {
        return User.builder()
                .id(USER_ID)
                .phone(PHONE)
                .phoneVerified(true)
                .build();
    }
}

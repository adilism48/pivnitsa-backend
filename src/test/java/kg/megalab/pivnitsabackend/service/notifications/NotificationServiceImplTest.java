package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.NotificationPageResponse;
import kg.megalab.pivnitsabackend.dto.notification.NotificationResponse;
import kg.megalab.pivnitsabackend.entity.Notification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.NotificationNotFoundException;
import kg.megalab.pivnitsabackend.repository.NotificationRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final String PHONE = "+996700123456";
    private static final Long USER_ID = 1L;

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                userRepository
        );
    }

    @Test
    void shouldReturnNormalizedNotificationPageAndUnreadCount() {
        Notification notification = notification();
        PageRequest expectedPageable = PageRequest.of(0, 100);

        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(
                USER_ID,
                expectedPageable
        )).thenReturn(new PageImpl<>(
                List.of(notification),
                expectedPageable,
                101
        ));
        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID))
                .thenReturn(5L);

        NotificationPageResponse response =
                notificationService.getCurrentUserNotifications(
                        PHONE,
                        -1,
                        500
                );

        assertEquals(0, response.page());
        assertEquals(100, response.size());
        assertTrue(response.hasNext());
        assertEquals(5L, response.unreadCount());
        assertEquals(1, response.items().size());
        assertEquals(10L, response.items().getFirst().id());
        assertFalse(response.items().getFirst().read());
    }

    @Test
    void shouldMarkOwnedNotificationAsRead() {
        Notification notification = notification();

        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(notificationRepository.findByIdAndUserId(10L, USER_ID))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification))
                .thenReturn(notification);

        NotificationResponse response =
                notificationService.markAsRead(PHONE, 10L);

        assertTrue(response.read());
        assertNotNull(response.readAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldRejectMissingOrForeignNotification() {
        when(userRepository.findByPhone(PHONE))
                .thenReturn(Optional.of(verifiedUser()));
        when(notificationRepository.findByIdAndUserId(99L, USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(PHONE, 99L)
        );

        verify(notificationRepository, never()).save(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldCreateNotificationsForDistinctUsers() {
        notificationService.createForUsers(
                List.of(1L, 1L, 2L),
                NotificationType.EVENT_PUBLISHED,
                "New event",
                "Friday Night",
                NotificationTargetType.EVENT,
                42L,
                "EVENT_PUBLISHED:42"
        );

        verifyInsert(1L);
        verifyInsert(2L);
    }

    @Test
    void shouldRejectPartiallySpecifiedTarget() {
        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.createForUsers(
                        List.of(USER_ID),
                        NotificationType.EVENT_PUBLISHED,
                        "New event",
                        "Friday Night",
                        NotificationTargetType.EVENT,
                        null,
                        "EVENT_PUBLISHED:42"
                )
        );

        verify(notificationRepository, never()).insertIfAbsent(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldReportWhetherNotificationWasCreated() {
        when(notificationRepository.insertIfAbsent(
                USER_ID,
                "BOOKING_REMINDER",
                "Booking reminder",
                "Table 7",
                "BOOKING",
                145L,
                "BOOKING_REMINDER:145"
        )).thenReturn(1);

        boolean created = notificationService.createForUser(
                USER_ID,
                NotificationType.BOOKING_REMINDER,
                "Booking reminder",
                "Table 7",
                NotificationTargetType.BOOKING,
                145L,
                "BOOKING_REMINDER:145"
        );

        assertTrue(created);
    }

    @Test
    void shouldReportDuplicateNotification() {
        when(notificationRepository.insertIfAbsent(
                USER_ID,
                "BOOKING_REMINDER",
                "Booking reminder",
                "Table 7",
                "BOOKING",
                145L,
                "BOOKING_REMINDER:145"
        )).thenReturn(0);

        boolean created = notificationService.createForUser(
                USER_ID,
                NotificationType.BOOKING_REMINDER,
                "Booking reminder",
                "Table 7",
                NotificationTargetType.BOOKING,
                145L,
                "BOOKING_REMINDER:145"
        );

        assertFalse(created);
    }

    private void verifyInsert(Long userId) {
        verify(notificationRepository).insertIfAbsent(
                userId,
                "EVENT_PUBLISHED",
                "New event",
                "Friday Night",
                "EVENT",
                42L,
                "EVENT_PUBLISHED:42"
        );
    }

    private User verifiedUser() {
        return User.builder()
                .id(USER_ID)
                .phone(PHONE)
                .phoneVerified(true)
                .build();
    }

    private Notification notification() {
        return Notification.builder()
                .id(10L)
                .userId(USER_ID)
                .type(NotificationType.EVENT_PUBLISHED)
                .title("New event")
                .body("Friday Night")
                .targetType(NotificationTargetType.EVENT)
                .targetId(42L)
                .deduplicationKey("EVENT_PUBLISHED:42")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}

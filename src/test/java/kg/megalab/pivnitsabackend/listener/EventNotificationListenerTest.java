package kg.megalab.pivnitsabackend.listener;

import kg.megalab.pivnitsabackend.dto.notification.EventPublishedEvent;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.service.notifications.NotificationService;
import kg.megalab.pivnitsabackend.service.notifications.PushNotificationService;
import kg.megalab.pivnitsabackend.service.notifications.UserNotificationSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventNotificationListenerTest {

    @Mock private UserNotificationSettingsService settingsService;
    @Mock private PushNotificationService pushNotificationService;
    @Mock private NotificationService notificationService;

    private EventNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new EventNotificationListener(
                settingsService,
                pushNotificationService,
                notificationService
        );
    }

    @Test
    void shouldSaveHistoryBeforeSendingPush() {
        List<Long> userIds = List.of(1L, 2L);
        when(settingsService.getAllEventSubscribedUserIds())
                .thenReturn(userIds);

        listener.handleEventPublished(
                new EventPublishedEvent(42L, "Friday Night")
        );

        InOrder inOrder = inOrder(
                notificationService,
                pushNotificationService
        );
        inOrder.verify(notificationService).createForUsers(
                userIds,
                NotificationType.EVENT_PUBLISHED,
                "Новое мероприятие!",
                "Friday Night",
                NotificationTargetType.EVENT,
                42L,
                "EVENT_PUBLISHED:42"
        );
        inOrder.verify(pushNotificationService).sendToUsers(
                eq(userIds),
                argThat((PushNotification notification) ->
                        "Новое мероприятие!".equals(notification.title())
                                && "Friday Night".equals(notification.body())
                                && "OPEN_EVENT".equals(
                                        notification.data().get("type")
                                )
                                && "42".equals(
                                        notification.data().get("eventId")
                                )
                )
        );
    }

    @Test
    void shouldDoNothingWhenThereAreNoSubscribers() {
        when(settingsService.getAllEventSubscribedUserIds())
                .thenReturn(List.of());

        listener.handleEventPublished(
                new EventPublishedEvent(42L, "Friday Night")
        );

        verifyNoInteractions(notificationService, pushNotificationService);
    }
}

package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.notification.EventPublishedEvent;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.listener.EventNotificationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationListenerTest {

    @Mock private UserNotificationSettingsService notificationSettingsService;
    @Mock private PushNotificationService pushNotificationService;
    @InjectMocks private EventNotificationListener eventNotificationListener;

    @Test
    void handleEventPublished_noSubscribers_doesNotSendPush() {
        when(notificationSettingsService.getAllEventSubscribedUserIds()).thenReturn(List.of());

        eventNotificationListener.handleEventPublished(new EventPublishedEvent(1L, "Concert"));

        verifyNoInteractions(pushNotificationService);
    }

    @Test
    void handleEventPublished_withSubscribers_sendsCorrectPayload() {
        when(notificationSettingsService.getAllEventSubscribedUserIds()).thenReturn(List.of(1L, 2L));

        eventNotificationListener.handleEventPublished(new EventPublishedEvent(1L, "Concert"));

        ArgumentCaptor<PushNotification> captor = ArgumentCaptor.forClass(PushNotification.class);
        verify(pushNotificationService).sendToUsers(eq(List.of(1L, 2L)), captor.capture());

        PushNotification sent = captor.getValue();
        assertEquals("Concert", sent.body());
        assertEquals("OPEN_EVENT", sent.data().get("type"));
        assertEquals("1", sent.data().get("eventId"));
    }
}

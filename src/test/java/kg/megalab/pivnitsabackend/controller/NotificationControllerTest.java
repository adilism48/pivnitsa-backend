package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.notification.NotificationPageResponse;
import kg.megalab.pivnitsabackend.dto.notification.NotificationResponse;
import kg.megalab.pivnitsabackend.service.notifications.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private static final String PHONE = "+996700123456";

    @Mock private NotificationService notificationService;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService);
    }

    @Test
    void shouldReturnNotificationPage() {
        NotificationPageResponse expected =
                new NotificationPageResponse(
                        List.of(),
                        0,
                        20,
                        false,
                        3L
                );
        when(notificationService.getCurrentUserNotifications(PHONE, 0, 20))
                .thenReturn(expected);

        ResponseEntity<NotificationPageResponse> response =
                controller.getNotifications(PHONE, 0, 20);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(notificationService)
                .getCurrentUserNotifications(PHONE, 0, 20);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        NotificationResponse expected = mock(NotificationResponse.class);
        when(notificationService.markAsRead(PHONE, 15L))
                .thenReturn(expected);

        ResponseEntity<NotificationResponse> response =
                controller.markAsRead(PHONE, 15L);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(notificationService).markAsRead(PHONE, 15L);
    }
}

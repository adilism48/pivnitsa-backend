package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;
import kg.megalab.pivnitsabackend.service.UserNotificationSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingsControllerTest {

    private static final String PHONE = "+996700123456";

    @Mock
    private UserNotificationSettingsService settingsService;

    private NotificationSettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationSettingsController(settingsService);
    }

    @Test
    void shouldReturnCurrentUserSettings() {
        NotificationSettingsResponse expectedResponse =
                new NotificationSettingsResponse(true, false);

        when(settingsService.getCurrentUserSettings(PHONE))
                .thenReturn(expectedResponse);

        ResponseEntity<NotificationSettingsResponse> response =
                controller.getSettings(PHONE);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(settingsService).getCurrentUserSettings(PHONE);
    }

    @Test
    void shouldUpdateCurrentUserSettings() {
        UpdateNotificationSettingsRequest request =
                new UpdateNotificationSettingsRequest(false, true);
        NotificationSettingsResponse expectedResponse =
                new NotificationSettingsResponse(false, true);

        when(settingsService.updateCurrentUserSettings(PHONE, request))
                .thenReturn(expectedResponse);

        ResponseEntity<NotificationSettingsResponse> response =
                controller.updateSettings(PHONE, request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(settingsService)
                .updateCurrentUserSettings(PHONE, request);
    }
}

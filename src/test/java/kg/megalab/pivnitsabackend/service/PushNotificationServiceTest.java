package kg.megalab.pivnitsabackend.service;

import com.google.firebase.messaging.*;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock private FirebaseMessaging firebaseMessaging;
    @Mock private DeviceTokenService deviceTokenService;
    @InjectMocks private PushNotificationService pushNotificationService;

    @Test
    void sendToUsers_noTokens_doesNotCallFirebase() {
        when(deviceTokenService.getTokensByUserIds(List.of(1L))).thenReturn(List.of());

        pushNotificationService.sendToUsers(List.of(1L), samplePush());

        verifyNoInteractions(firebaseMessaging);
    }

    @Test
    void sendToTokens_moreThan500_splitsIntoTwoChunks() throws FirebaseMessagingException {
        List<String> tokens = IntStream.range(0, 750).mapToObj(i -> "token" + i).toList();

        BatchResponse response = mock(BatchResponse.class);
        when(response.getSuccessCount()).thenReturn(1);
        when(response.getFailureCount()).thenReturn(0);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(response);

        pushNotificationService.sendToTokens(tokens, samplePush());

        verify(firebaseMessaging, times(2)).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void sendToTokens_unregisteredToken_isRemovedFromDb() throws FirebaseMessagingException {
        List<String> tokens = List.of("valid-token", "dead-token");

        SendResponse success = mock(SendResponse.class);
        when(success.isSuccessful()).thenReturn(true);

        SendResponse failure = mock(SendResponse.class);
        when(failure.isSuccessful()).thenReturn(false);
        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        when(ex.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(failure.getException()).thenReturn(ex);

        BatchResponse response = mock(BatchResponse.class);
        when(response.getSuccessCount()).thenReturn(1);
        when(response.getFailureCount()).thenReturn(1);
        when(response.getResponses()).thenReturn(List.of(success, failure));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(response);

        pushNotificationService.sendToTokens(tokens, samplePush());

        verify(deviceTokenService).removeInvalidTokens(List.of("dead-token"));
    }

    @Test
    void sendToTokens_firebaseThrows_doesNotPropagateException() throws FirebaseMessagingException {
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .thenThrow(FirebaseMessagingException.class);

        pushNotificationService.sendToTokens(List.of("token1"), samplePush());

        verify(deviceTokenService, never()).removeInvalidTokens(any());
    }

    private PushNotification samplePush() {
        return new PushNotification("Новое мероприятие!", "Concert", Map.of("type", "OPEN_EVENT", "eventId", "1"));
    }
}
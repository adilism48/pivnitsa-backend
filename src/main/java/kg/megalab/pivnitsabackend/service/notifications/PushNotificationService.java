package kg.megalab.pivnitsabackend.service.notifications;

import com.google.firebase.messaging.*;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {

    private static final int FCM_BATCH_SIZE = 500;

    private final DeviceTokenService deviceTokenService;
    private final FirebaseMessaging firebaseMessaging;


    public void sendToUsers(List<Long> userIds, PushNotification notification) {
        List<String> tokens = deviceTokenService.getTokensByUserIds(userIds);
        if (tokens.isEmpty()) return;
        sendToTokens(tokens, notification);
    }

    public void sendToTokens(List<String> tokens, PushNotification notification) {
        for (int i = 0; i < tokens.size(); i += FCM_BATCH_SIZE) {
            List<String> chunk = tokens.subList(i, Math.min(i + FCM_BATCH_SIZE, tokens.size()));
            sendChunk(chunk, notification, i / FCM_BATCH_SIZE + 1);
        }
    }

    private void sendChunk(List<String> chunkTokens, PushNotification notification, int chunkIndex) {
        if (firebaseMessaging == null) {
            log.error("FirebaseMessaging не инициализирован! Проверьте firebase-service-account.json");
            return;
        }

        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(notification.title())
                            .setBody(notification.body())
                            .build())
                    .addAllTokens(chunkTokens);

            notification.data().forEach(builder::putData);

            MulticastMessage message = builder.build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Чанк #{} отправлен. Успешно: {}, Ошибок: {}",
                    chunkIndex, response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<String> invalidTokens = extractInvalidTokens(chunkTokens, response.getResponses());
                deviceTokenService.removeInvalidTokens(invalidTokens);
            }
        } catch (Exception e) {
            log.error("Сбой отправки чанка #{}", chunkIndex, e);
        }
    }

    private List<String> extractInvalidTokens(List<String> chunkTokens, List<SendResponse> responses) {
        List<String> invalidTokens = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse res = responses.get(i);
            if (!res.isSuccessful()) {
                String token = chunkTokens.get(i);
                FirebaseMessagingException exception = res.getException();
                MessagingErrorCode errorCode = exception != null ? exception.getMessagingErrorCode() : null;

                log.warn("Ошибка отправки на токен [{}]: errorCode={}", token, errorCode);

                if (errorCode == MessagingErrorCode.UNREGISTERED
                        || errorCode == MessagingErrorCode.INVALID_ARGUMENT
                        || errorCode == MessagingErrorCode.SENDER_ID_MISMATCH
                        || errorCode == null) {
                    invalidTokens.add(token);
                }
            }
        }

        log.info("Определено невалидных токенов для удаления: {}", invalidTokens.size());
        return invalidTokens;
    }
}

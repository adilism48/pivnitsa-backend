package kg.megalab.pivnitsabackend.listener;

import com.google.firebase.messaging.*;
import kg.megalab.pivnitsabackend.dto.notification.EventPublishedEvent;
import kg.megalab.pivnitsabackend.service.DeviceTokenService;
import kg.megalab.pivnitsabackend.service.UserNotificationSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventNotificationListener {

    private final UserNotificationSettingsService notificationSettingsService;
    private final DeviceTokenService deviceTokenService;
    private final FirebaseMessaging firebaseMessaging;

    private static final int FCM_BATCH_SIZE = 500;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventPublished(EventPublishedEvent event) {

        List<Long> subscribedUserIds = notificationSettingsService.getAllEventSubscribedUserIds();
        log.info("Найдено подписчиков: {}", subscribedUserIds.size());
        if (subscribedUserIds.isEmpty()) return;

        List<String> tokens = deviceTokenService.getTokensByUserIds(subscribedUserIds);
        log.info("Найдено токенов: {}", tokens.size());
        if (tokens.isEmpty()) return;

        for (int i = 0; i < tokens.size(); i += FCM_BATCH_SIZE) {
            List<String> chunk = tokens.subList(i, Math.min(i + FCM_BATCH_SIZE, tokens.size()));
            sendChunk(chunk, event, (i / FCM_BATCH_SIZE + 1));
        }
    }

    private void sendChunk(List<String> chunkTokens, EventPublishedEvent event, int chunkIndex) {
        if (firebaseMessaging == null) {
            log.error("FirebaseMessaging не инициализирован! Проверьте firebase-service-account.json");
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle("Новое мероприятие!")
                            .setBody(event.eventTitle())
                            .build())
                    .putData("type", "OPEN_EVENT")
                    .putData("eventId", String.valueOf(event.eventId()))
                    .addAllTokens(chunkTokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Чанк #{} отправлен для eventId={}. Успешно: {}, Ошибок: {}",
                    chunkIndex, event.eventId(), response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<String> invalidTokens = extractInvalidTokens(chunkTokens, response.getResponses());
                deviceTokenService.removeInvalidTokens(invalidTokens);
            }
        } catch (Exception e) {
            log.error("Сбой отправки чанка #{} для eventId={}", chunkIndex, event.eventId(), e);
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

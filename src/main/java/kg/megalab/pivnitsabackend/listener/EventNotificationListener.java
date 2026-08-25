package kg.megalab.pivnitsabackend.listener;

import kg.megalab.pivnitsabackend.dto.notification.EventPublishedEvent;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.service.notifications.NotificationService;
import kg.megalab.pivnitsabackend.service.notifications.PushNotificationService;
import kg.megalab.pivnitsabackend.service.notifications.UserNotificationSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventNotificationListener {

    private final UserNotificationSettingsService notificationSettingsService;
    private final PushNotificationService pushNotificationService;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventPublished(EventPublishedEvent event) {
        log.info("handleEventPublished вызван для eventId={}", event.eventId());

        List<Long> subscribedUserIds = notificationSettingsService.getAllEventSubscribedUserIds();
        log.info("Найдено подписчиков: {}", subscribedUserIds.size());
        if (subscribedUserIds.isEmpty()) return;

        notificationService.createForUsers(
                subscribedUserIds,
                NotificationType.EVENT_PUBLISHED,
                "Новое мероприятие!",
                event.eventTitle(),
                NotificationTargetType.EVENT,
                event.eventId(),
                "EVENT_PUBLISHED:" + event.eventId()
        );

        pushNotificationService.sendToUsers(subscribedUserIds, new PushNotification(
                "Новое мероприятие!",
                event.eventTitle(),
                Map.of("type", "OPEN_EVENT", "eventId", String.valueOf(event.eventId()))
        ));

        log.info("sendToUsers вызван для eventId={}", event.eventId());
    }
}

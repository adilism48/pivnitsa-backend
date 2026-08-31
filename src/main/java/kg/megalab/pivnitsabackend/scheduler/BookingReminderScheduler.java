package kg.megalab.pivnitsabackend.scheduler;

import kg.megalab.pivnitsabackend.service.notifications.BookingReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.notifications.booking-reminder",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class BookingReminderScheduler {

    private final BookingReminderService bookingReminderService;

    @Scheduled(
            fixedDelayString = "${app.notifications.booking-reminder.fixed-delay:PT5M}",
            initialDelayString = "${app.notifications.booking-reminder.initial-delay:PT1M}"
    )
    public void sendBookingReminders() {
        int sentCount = bookingReminderService.sendDueReminders();
        if (sentCount > 0) {
            log.info("Отправлено напоминаний о бронировании: {}", sentCount);
        }
    }
}

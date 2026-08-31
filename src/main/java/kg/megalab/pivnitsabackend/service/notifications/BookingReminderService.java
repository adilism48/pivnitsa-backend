package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.config.BookingReminderProperties;
import kg.megalab.pivnitsabackend.dto.notification.BookingReminderCandidate;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReminderService {

    private static final String TITLE = "Напоминание о брони";
    private static final DateTimeFormatter BOOKING_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM 'в' HH:mm", Locale.forLanguageTag("ru"));

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final BookingReminderProperties properties;
    private final Clock clock;

    public int sendDueReminders() {
        OffsetDateTime target = OffsetDateTime.now(clock)
                .plus(properties.getAdvance());
        OffsetDateTime from = target.minus(properties.getLookback());
        OffsetDateTime to = target.plus(properties.getLookahead());

        List<BookingReminderCandidate> candidates =
                bookingRepository.findBookingReminderCandidates(from, to);

        int sentCount = 0;
        for (BookingReminderCandidate candidate : candidates) {
            try {
                if (sendReminder(candidate)) {
                    sentCount++;
                }
            } catch (RuntimeException exception) {
                log.error(
                        "Не удалось обработать напоминание для bookingId={}",
                        candidate.bookingId(),
                        exception
                );
            }
        }

        return sentCount;
    }

    private boolean sendReminder(BookingReminderCandidate candidate) {
        String formattedBookingTime = candidate.bookingAt()
                .atZoneSameInstant(properties.getTimezone())
                .format(BOOKING_TIME_FORMATTER);
        String body = "Ваша бронь " + formattedBookingTime
                + ", столик №" + candidate.tableNumber();
        String deduplicationKey = "BOOKING_REMINDER:"
                + candidate.bookingId()
                + ":"
                + candidate.bookingAt().toInstant().toEpochMilli();

        boolean created = notificationService.createForUser(
                candidate.userId(),
                NotificationType.BOOKING_REMINDER,
                TITLE,
                body,
                NotificationTargetType.BOOKING,
                candidate.bookingId(),
                deduplicationKey
        );

        if (!created) {
            return false;
        }

        pushNotificationService.sendToUsers(
                List.of(candidate.userId()),
                new PushNotification(
                        TITLE,
                        body,
                        Map.of(
                                "type", "OPEN_BOOKING",
                                "targetType", NotificationTargetType.BOOKING.name(),
                                "targetId", String.valueOf(candidate.bookingId()),
                                "bookingId", String.valueOf(candidate.bookingId())
                        )
                )
        );

        return true;
    }
}

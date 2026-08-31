package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.config.BookingReminderProperties;
import kg.megalab.pivnitsabackend.dto.notification.BookingReminderCandidate;
import kg.megalab.pivnitsabackend.dto.notification.PushNotification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingReminderServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-31T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock private BookingRepository bookingRepository;
    @Mock private NotificationService notificationService;
    @Mock private PushNotificationService pushNotificationService;

    private BookingReminderService bookingReminderService;

    @BeforeEach
    void setUp() {
        BookingReminderProperties properties = new BookingReminderProperties();
        properties.setAdvance(Duration.ofHours(24));
        properties.setLookback(Duration.ofMinutes(5));
        properties.setLookahead(Duration.ofMinutes(5));
        properties.setTimezone(ZoneId.of("Asia/Bishkek"));

        bookingReminderService = new BookingReminderService(
                bookingRepository,
                notificationService,
                pushNotificationService,
                properties,
                CLOCK
        );
    }

    @Test
    void shouldCreateHistoryAndSendPushForDueBooking() {
        BookingReminderCandidate candidate = candidate(145L, 42L, "7");
        OffsetDateTime expectedFrom = OffsetDateTime.parse("2026-09-01T11:55:00Z");
        OffsetDateTime expectedTo = OffsetDateTime.parse("2026-09-01T12:05:00Z");

        when(bookingRepository.findBookingReminderCandidates(expectedFrom, expectedTo))
                .thenReturn(List.of(candidate));
        when(notificationService.createForUser(
                eq(42L),
                eq(NotificationType.BOOKING_REMINDER),
                eq("Напоминание о брони"),
                eq("Ваша бронь 1 сентября в 18:00, столик №7"),
                eq(NotificationTargetType.BOOKING),
                eq(145L),
                any(String.class)
        )).thenReturn(true);

        int sentCount = bookingReminderService.sendDueReminders();

        assertEquals(1, sentCount);
        ArgumentCaptor<String> deduplicationKey = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createForUser(
                eq(42L),
                eq(NotificationType.BOOKING_REMINDER),
                eq("Напоминание о брони"),
                eq("Ваша бронь 1 сентября в 18:00, столик №7"),
                eq(NotificationTargetType.BOOKING),
                eq(145L),
                deduplicationKey.capture()
        );
        assertTrue(deduplicationKey.getValue().startsWith("BOOKING_REMINDER:145:"));

        ArgumentCaptor<PushNotification> push = ArgumentCaptor.forClass(PushNotification.class);
        verify(pushNotificationService).sendToUsers(eq(List.of(42L)), push.capture());
        assertEquals("OPEN_BOOKING", push.getValue().data().get("type"));
        assertEquals("145", push.getValue().data().get("bookingId"));
        assertEquals("BOOKING", push.getValue().data().get("targetType"));
    }

    @Test
    void shouldNotSendPushWhenNotificationAlreadyExists() {
        BookingReminderCandidate candidate = candidate(145L, 42L, "7");
        when(bookingRepository.findBookingReminderCandidates(any(), any()))
                .thenReturn(List.of(candidate));
        when(notificationService.createForUser(
                eq(42L),
                eq(NotificationType.BOOKING_REMINDER),
                any(),
                any(),
                eq(NotificationTargetType.BOOKING),
                eq(145L),
                any()
        )).thenReturn(false);

        int sentCount = bookingReminderService.sendDueReminders();

        assertEquals(0, sentCount);
        verify(pushNotificationService, never()).sendToUsers(any(), any());
    }

    @Test
    void shouldContinueAfterFailureForOneBooking() {
        BookingReminderCandidate first = candidate(145L, 42L, "7");
        BookingReminderCandidate second = candidate(146L, 43L, "8");
        when(bookingRepository.findBookingReminderCandidates(any(), any()))
                .thenReturn(List.of(first, second));
        when(notificationService.createForUser(
                eq(42L), any(), any(), any(), any(), eq(145L), any()
        )).thenThrow(new IllegalStateException("Database is unavailable"));
        when(notificationService.createForUser(
                eq(43L), any(), any(), any(), any(), eq(146L), any()
        )).thenReturn(true);

        int sentCount = bookingReminderService.sendDueReminders();

        assertEquals(1, sentCount);
        verify(pushNotificationService).sendToUsers(
                eq(List.of(43L)),
                any(PushNotification.class)
        );
    }

    private BookingReminderCandidate candidate(
            Long bookingId,
            Long userId,
            String tableNumber
    ) {
        return new BookingReminderCandidate(
                bookingId,
                userId,
                OffsetDateTime.parse("2026-09-01T12:00:00Z"),
                tableNumber
        );
    }
}

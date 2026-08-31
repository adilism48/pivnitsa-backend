package kg.megalab.pivnitsabackend.scheduler;

import kg.megalab.pivnitsabackend.service.notifications.BookingReminderService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingReminderSchedulerTest {

    @Test
    void shouldDelegateReminderSendingToService() {
        BookingReminderService service = mock(BookingReminderService.class);
        when(service.sendDueReminders()).thenReturn(2);
        BookingReminderScheduler scheduler = new BookingReminderScheduler(service);

        scheduler.sendBookingReminders();

        verify(service).sendDueReminders();
    }
}

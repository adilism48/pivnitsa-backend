package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.exception.booking.InvalidBookingDataException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Service
public class BookingDateValidator {
    private static final long MAX_BOOKING_DAY = 30;

    private static final LocalTime MON_WED_START = LocalTime.of(19, 0);
    private static final LocalTime MON_WED_END = LocalTime.of(21, 30);
    private static final LocalTime THU_SUN_START = LocalTime.of(20, 0);
    private static final LocalTime THU_SUN_END = LocalTime.of(21, 30);


    public void validateBookingDate(OffsetDateTime bookingAt) {
        if (bookingAt.isAfter(OffsetDateTime.now().plusDays(MAX_BOOKING_DAY))) {
            throw new InvalidBookingDataException("Дата брони не может быть более чем через 30 дней");
        }

        validateBookingTime(bookingAt);
    }

    private void validateBookingTime(OffsetDateTime bookingAt) {
        OffsetDateTime bishkekTime = bookingAt.withOffsetSameInstant(ZoneOffset.of("+06:00"));

        DayOfWeek dayOfWeek = bishkekTime.getDayOfWeek();
        LocalTime time = bishkekTime.toLocalTime();

        LocalTime start;
        LocalTime end;

        if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) {
            start = MON_WED_START;
            end = MON_WED_END;
        } else {
            start = THU_SUN_START;
            end = THU_SUN_END;
        }

        if (time.isBefore(start) || time.isAfter(end)) {
            throw new InvalidBookingDataException("Бронирование на это время недоступно. Прием броней от: " + start + "до: " + end);
        }
    }
}

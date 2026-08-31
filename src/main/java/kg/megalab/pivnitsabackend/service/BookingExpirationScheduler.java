package kg.megalab.pivnitsabackend.service;


import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {
    private final BookingRepository bookingRepository;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void findExpirationBooking() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, threshold);
        for (Booking expiredBooking : expiredBookings) {
            expiredBooking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(expiredBooking);
        }
    }
}

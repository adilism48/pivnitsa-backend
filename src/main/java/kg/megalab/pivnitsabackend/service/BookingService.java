package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.booking.BookingResponse;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookings(String phone) {

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        return bookingRepository.findActiveBookings(
                user.getId(),
                List.of(
                        BookingStatus.PENDING_PAYMENT,
                        BookingStatus.CONFIRMED
                ),
                OffsetDateTime.now()
        );
    }
}

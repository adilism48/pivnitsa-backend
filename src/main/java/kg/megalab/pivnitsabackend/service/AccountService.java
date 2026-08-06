package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.ActiveBookingExistsException;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public void deleteAccount(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        boolean hasActiveBooking = bookingRepository
                .existsActivePaidBookingByUserId(user.getId(), BookingStatus.CONFIRMED);

        if (hasActiveBooking) {
            throw new ActiveBookingExistsException(
                    "Нельзя удалить аккаунт: есть активная оплаченная бронь"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        user.setIsDeleted(true);
        user.setDeletedAt(now);
        user.setFirstName("Удалённый");
        user.setLastName("Пользователь");
        user.setEmail(null);
        user.setPhone(user.getPhone() + "_deleted_" + now.toEpochSecond());

        userRepository.save(user);
    }
}
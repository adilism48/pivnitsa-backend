package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import kg.megalab.pivnitsabackend.exception.tables.TableNotFoundException;
import kg.megalab.pivnitsabackend.exception.booking.*;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.dto.booking.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ClubTableRepository clubTableRepository;
    private final UserRepository userRepository;
    private final BookingDateValidator bookingDateValidator;

    @Transactional
    public BookingResponse createBooking(String phone, CreateBookingRequest request) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        bookingDateValidator.validateBookingDate(request.bookingAt());

        ClubTable bookingTable = clubTableRepository.findById(request.clubTableId())
                .orElseThrow(() -> new TableNotFoundException("Столик не найден"));

        if (bookingTable.getDepositAmount() == null) {
            throw new DepositNotConfiguredException("Нету данных о депозите");
        }

        if (bookingTable.getCapacity() < request.guestsCount()) {
            throw new GuestsExceedCapacityException("Превышено допустимое число гостей");
        }

        try {
            Booking booking = Booking.builder()
                    .userId(user.getId())
                    .clubTableId(bookingTable.getId())
                    .bookingAt(request.bookingAt())
                    .status(BookingStatus.PENDING_PAYMENT)
                    .amount(bookingTable.getDepositAmount())
                    .guestsCount(request.guestsCount())
                    .build();

            booking = bookingRepository.save(booking);

            return toResponse(booking, bookingTable);

        } catch (DataIntegrityViolationException e) {
            throw new TableNotAvailableException("Столик уже забронирован");
        }

    }

    private BookingResponse toResponse(Booking booking, ClubTable bookingTable) {
        return new BookingResponse(
                booking.getId(),
                booking.getClubTableId(),
                bookingTable.getTableNumber(),
                booking.getEventId(),
                booking.getBookingAt(),
                booking.getCreatedAt(),
                booking.getStatus(),
                booking.getAmount(),
                booking.getGuestsCount()
        );
    }
}

package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.exception.tables.TableNotFoundException;
import kg.megalab.pivnitsabackend.exception.booking.*;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.dto.booking.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ClubTableRepository clubTableRepository;
    private static final long MAX_BOOKING_DAY = 30;

    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {

        if (request.bookingAt().isAfter(OffsetDateTime.now().plusDays(MAX_BOOKING_DAY))) {
            throw new InvalidBookingDataException("Дата брони не может быть более чем 30 дней");
        }

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
                    .userId(userId)
                    .clubTableId(bookingTable.getId())
                    .bookingAt(request.bookingAt())
                    .status(BookingStatus.PENDING_PAYMENT)
                    .amount(bookingTable.getDepositAmount())
                    .guestsCount(request.guestsCount())
                    .build();

            booking = bookingRepository.save(booking);

            return toResponse(booking);
        } catch (DataIntegrityViolationException e) {
            throw new TableNotAvailableException("Столик уже забронирован");
        }

    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getClubTableId(),
                booking.getEventId(),
                booking.getBookingAt(),
                booking.getStatus(),
                booking.getAmount(),
                booking.getGuestsCount()
        );
    }
}

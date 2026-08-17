package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.booking.BookingResponse;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                userRepository,
                bookingRepository
        );
    }

    @Test
    void shouldReturnActiveBookingsForCurrentUser() {

        User user = User.builder()
                .id(1L)
                .phone("+77001234567")
                .build();

        OffsetDateTime bookingAt =
                OffsetDateTime.now(ZoneId.of("Asia/Bishkek"))
                        .plusDays(1);

        BookingResponse bookingResponse = new BookingResponse(
                100L,
                "A-5",
                3,
                bookingAt,
                new BigDecimal("15000.00"),
                BookingStatus.CONFIRMED
        );

        when(userRepository.findByPhone("+77001234567"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findActiveBookings(
                eq(1L),
                anyCollection(),
                any(OffsetDateTime.class)
        )).thenReturn(List.of(bookingResponse));

        List<BookingResponse> result =
                bookingService.getActiveBookings("+77001234567");

        assertEquals(1, result.size());

        BookingResponse response = result.getFirst();

        assertEquals(100L, response.id());
        assertEquals("A-5", response.tableNumber());
        assertEquals(3, response.guestsCount());
        assertEquals(new BigDecimal("15000.00"), response.amount());
        assertEquals(bookingAt, response.bookingAt());
        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoActiveBookings() {

        User user = User.builder()
                .id(1L)
                .phone("+77001234567")
                .build();

        when(userRepository.findByPhone("+77001234567"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findActiveBookings(
                eq(1L),
                anyCollection(),
                any(OffsetDateTime.class)
        )).thenReturn(List.of());

        // act
        List<BookingResponse> result =
                bookingService.getActiveBookings("+77001234567");

        // assert
        assertTrue(result.isEmpty());

    }

    @Test
    void shouldReturnBookingHistoryForCurrentUser() {
        User user = User.builder()
                .id(1L)
                .phone("+77001234567")
                .build();

        OffsetDateTime bookingAt =
                OffsetDateTime.now(ZoneId.of("Asia/Bishkek"))
                        .minusDays(1);

        BookingResponse bookingResponse = new BookingResponse(
                101L,
                "B-2",
                4,
                bookingAt,
                new BigDecimal("18000.00"),
                BookingStatus.COMPLETED
        );

        when(userRepository.findByPhone("+77001234567"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findBookingHistory(
                eq(1L),
                anyCollection(),
                any(OffsetDateTime.class)
        )).thenReturn(List.of(bookingResponse));

        List<BookingResponse> result =
                bookingService.getBookingHistory("+77001234567");

        assertEquals(1, result.size());
        assertEquals(bookingResponse, result.getFirst());

        verify(bookingRepository).findBookingHistory(
                eq(1L),
                argThat(statuses -> statuses.equals(List.of(
                        BookingStatus.COMPLETED,
                        BookingStatus.CANCELLED
                ))),
                any(OffsetDateTime.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // arrange
        String phone = "+77009999999";

        when(userRepository.findByPhone(phone))
                .thenReturn(Optional.empty());

        // act + assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getActiveBookings(phone)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verifyNoInteractions(
                bookingRepository
        );
    }
}

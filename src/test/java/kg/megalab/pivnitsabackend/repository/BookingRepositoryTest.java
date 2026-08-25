package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.dto.admin.AdminBookingResponse;
import kg.megalab.pivnitsabackend.dto.booking.BookingResponse;
import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.entity.Hall;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubTableRepository clubTableRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Test
    void shouldReturnAdminBookingsByDateWithPaymentStatus() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Bishkek"));

        User user = userRepository.save(
                User.builder()
                        .firstName("Jane")
                        .lastName("Doe")
                        .phone("+996500112233")
                        .phoneVerified(true)
                        .build()
        );

        Hall hall = hallRepository.save(
                Hall.builder()
                        .name("Main hall")
                        .build()
        );

        ClubTable table = clubTableRepository.save(
                ClubTable.builder()
                        .tableNumber("T-1")
                        .hallId(hall.getId())
                        .capacity(4)
                        .active(true)
                        .build()
        );

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .userId(user.getId())
                        .clubTableId(table.getId())
                        .guestsCount(4)
                        .bookingAt(now)
                        .status(BookingStatus.CONFIRMED)
                        .amount(new BigDecimal("1500.00"))
                        .cancellationReason(null)
                        .build()
        );

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .provider("?BANK")
                .amount(new BigDecimal("1500.00"))
                .status(PaymentStatus.SUCCEEDED)
                .build();

        entityManager.persist(payment);
        entityManager.flush();

        OffsetDateTime startOfDay = now.minusHours(1);
        OffsetDateTime endOfDay = now.plusHours(23);

        // ACT
        List<AdminBookingResponse> result = bookingRepository.findAdminBookingsByDate(startOfDay, endOfDay);

        // ASSERT
        assertEquals(1, result.size());

        AdminBookingResponse dto = result.getFirst();
        assertEquals(booking.getId(), dto.id());
        assertEquals("T-1", dto.tableNumber());
        assertEquals("Jane", dto.firstName());
        assertEquals("Doe", dto.lastName());
        assertEquals("+996500112233", dto.guestPhone());
        assertEquals(4, dto.guestsCount());
        assertEquals(0, new BigDecimal("1500.00").compareTo(dto.amount()));
        assertEquals(BookingStatus.CONFIRMED, dto.bookingStatus());
        assertEquals(PaymentStatus.SUCCEEDED, dto.paymentStatus());
        assertNull(dto.cancellationReason());
    }

    @Test
    @DisplayName("Должен возвращать ровно одну строку и статус ПОСЛЕДНЕГО платежа при нескольких попытках оплаты")
    void shouldReturnOnlyLatestPaymentStatusWhenMultiplePaymentsExist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Bishkek"));

        User user = userRepository.save(
                User.builder()
                        .firstName("Jane")
                        .lastName("Doe")
                        .phone("+996500112233")
                        .phoneVerified(true)
                        .build()
        );

        Hall hall = hallRepository.save(
                Hall.builder()
                        .name("Main hall")
                        .build()
        );

        ClubTable table = clubTableRepository.save(
                ClubTable.builder()
                        .tableNumber("T-2")
                        .hallId(hall.getId())
                        .capacity(4)
                        .active(true)
                        .build()
        );

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .userId(user.getId())
                        .clubTableId(table.getId())
                        .guestsCount(4)
                        .bookingAt(now)
                        .status(BookingStatus.CONFIRMED)
                        .amount(new BigDecimal("2000.00"))
                        .build()
        );

        Payment firstPayment = Payment.builder()
                .bookingId(booking.getId())
                .provider("QBANK")
                .amount(new BigDecimal("2000.00"))
                .status(PaymentStatus.FAILED)
                .build();
        entityManager.persist(firstPayment);

        Payment secondPayment = Payment.builder()
                .bookingId(booking.getId())
                .provider("WBANK")
                .amount(new BigDecimal("2000.00"))
                .status(PaymentStatus.SUCCEEDED)
                .build();
        entityManager.persist(secondPayment);

        entityManager.flush();

        // ACT
        List<AdminBookingResponse> result = bookingRepository.findAdminBookingsByDate(
                now.minusHours(1),
                now.plusHours(1)
        );

        assertEquals(1, result.size());

        assertEquals(PaymentStatus.SUCCEEDED, result.getFirst().paymentStatus());
    }
}

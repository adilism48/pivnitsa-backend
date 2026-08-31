package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.dto.booking.BookingResponse;
import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.entity.Hall;
import kg.megalab.pivnitsabackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Test
    void shouldReturnOnlyActiveBookingsSortedByNearestFirst() {
        OffsetDateTime now =
                OffsetDateTime.now(ZoneId.of("Asia/Bishkek"));

        User user = userRepository.save(
                User.builder()
                        .firstName("Test")
                        .lastName("User")
                        .phone("+996700123456")
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
                        .hallId(hall.getId())
                        .tableNumber("A-5")
                        .capacity(4)
                        .active(true)
                        .build()
        );

        Booking laterBooking = createBooking(
                user.getId(),
                table.getId(),
                now.plusDays(2)
        );

        Booking nearestBooking = createBooking(
                user.getId(),
                table.getId(),
                now.plusDays(1)
        );

        Booking pastBooking = createBooking(
                user.getId(),
                table.getId(),
                now.minusDays(1)
        );

        Booking cancelledBooking = createBooking(
                user.getId(),
                table.getId(),
                now.plusDays(3)
        );
        cancelledBooking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.saveAll(
                List.of(laterBooking,
                        nearestBooking,
                        pastBooking,
                        cancelledBooking
                )
        );

        List<BookingResponse> result = bookingRepository.findActiveBookings(
                user.getId(),
                List.of(
                        BookingStatus.PENDING_PAYMENT,
                        BookingStatus.CONFIRMED
                ),
                now
        );

        assertEquals(2, result.size());
        assertEquals(nearestBooking.getId(), result.get(0).id());
        assertEquals(laterBooking.getId(), result.get(1).id());
    }

    private Booking createBooking(
            Long userId,
            Long tableId,
            OffsetDateTime bookingAt
    ) {
        return Booking.builder()
                .userId(userId)
                .clubTableId(tableId)
                .guestsCount(2)
                .bookingAt(bookingAt)
                .status(BookingStatus.CONFIRMED)
                .amount(new BigDecimal("1000.00"))
                .build();
    }
}

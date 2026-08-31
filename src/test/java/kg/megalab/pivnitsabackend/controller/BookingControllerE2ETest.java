package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.entity.Hall;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.repository.HallRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookingControllerE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine")
            ).withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubTableRepository clubTableRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnActiveBookingsForAuthenticatedUser() throws Exception {
        // Arrange
        User user = userRepository.saveAndFlush(
                User.builder()
                        .firstName("Test")
                        .lastName("User")
                        .phone("+996700123456")
                        .phoneVerified(true)
                        .build()
        );

        Hall hall = hallRepository.saveAndFlush(
                Hall.builder()
                        .name("Main hall")
                        .build()
        );

        ClubTable table = clubTableRepository.saveAndFlush(
                ClubTable.builder()
                        .hallId(hall.getId())
                        .tableNumber("A-5")
                        .capacity(4)
                        .active(true)
                        .build()
        );

        OffsetDateTime bookingAt =
                OffsetDateTime.now(ZoneId.of("Asia/Bishkek"))
                        .plusDays(1);

        Booking booking = bookingRepository.saveAndFlush(
                Booking.builder()
                        .userId(user.getId())
                        .clubTableId(table.getId())
                        .guestsCount(3)
                        .bookingAt(bookingAt)
                        .amount(new BigDecimal("15000.00"))
                        .status(BookingStatus.CONFIRMED)
                        .build()
        );

        String token = jwtService.generateAccessToken(user.getPhone());

        // Act + Assert
        mockMvc.perform(
                        get("/api/v1/bookings/active")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(booking.getId()))
                .andExpect(jsonPath("$[0].tableNumber").value("A-5"))
                .andExpect(jsonPath("$[0].guestsCount").value(3))
                .andExpect(jsonPath("$[0].amount").value(15000.00))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }
}

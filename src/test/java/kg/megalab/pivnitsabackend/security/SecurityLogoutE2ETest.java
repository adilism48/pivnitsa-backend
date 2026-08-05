package kg.megalab.pivnitsabackend.security;

import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityLogoutE2ETest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer
                    ("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("После вызова /logout токен должен блокироваться в JwtAuthFilter и возвращать 401")
    void shouldBlockTokenAfterLogout() throws Exception {

        User user = User.builder()
                .firstName("Alex")
                .lastName("Petrov")
                .phone("+996555123456")
                .email("alex@mail.com")
                .phoneVerified(true)
                .build();

        userRepository.saveAndFlush(user);

        String token = jwtService.generateAccessToken(user.getPhone());
        String authHeader = "Bearer " + token;

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized());
    }
}
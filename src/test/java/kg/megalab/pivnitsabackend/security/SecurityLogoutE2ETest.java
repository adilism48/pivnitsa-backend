package kg.megalab.pivnitsabackend.security;

import kg.megalab.pivnitsabackend.BaseIntegrationTest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityLogoutE2ETest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

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
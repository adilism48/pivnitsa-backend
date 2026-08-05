package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.dto.profile.UpdateProfileRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void shouldUpdateCurrentUser() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .phone("+996700123456")
                .phoneVerified(true)
                .firstName("Old")
                .lastName("Name")
                .email("old@example.com")
                .build();

        when(userRepository.findByPhone("+996700123456"))
                .thenReturn(Optional.of(user));

        UpdateProfileRequest request =
                new UpdateProfileRequest(
                        "Bek",
                        "Saparov",
                        "bek@example.com"
                );

        // Act
        UserResponse response =
                userService.updateCurrentUser(
                        "+996700123456",
                        request
                );

        // Assert: проверяем ответ сервиса
        assertEquals(1L, response.id());
        assertEquals("Bek", response.firstName());
        assertEquals("Saparov", response.lastName());
        assertEquals("bek@example.com", response.email());
        assertEquals("+996700123456", response.phone());

        // Проверяем, что сама сущность изменилась
        assertEquals("Bek", user.getFirstName());
        assertEquals("Saparov", user.getLastName());
        assertEquals("bek@example.com", user.getEmail());

        // Телефон не должен измениться
        assertEquals("+996700123456", user.getPhone());

        verify(userRepository)
                .findByPhone("+996700123456");
    }
}
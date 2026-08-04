package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;


import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer
                    ("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByPhone() {

        User user = User.builder()
                .firstName("Alex")
                .lastName("Petrov")
                .phone("+996555123456")
                .email("alex@mail.com")
                .build();

        userRepository.save(user);

        Optional<User> found =
                userRepository.findByPhone("+996555123456");

        assertThat(found).isPresent();
        assertThat(found.get().getPhone())
                .isEqualTo("+996555123456");
    }
}
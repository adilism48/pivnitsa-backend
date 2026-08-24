package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.BaseIntegrationTest;
import kg.megalab.pivnitsabackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseIntegrationTest {

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
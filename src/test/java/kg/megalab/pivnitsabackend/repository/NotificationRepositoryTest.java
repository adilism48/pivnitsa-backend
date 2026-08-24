package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Notification;
import kg.megalab.pivnitsabackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class NotificationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void shouldInsertOnlyOnceForSameDeduplicationKey() {
        User user = userRepository.saveAndFlush(User.builder()
                .firstName("Test")
                .lastName("User")
                .phone("+996700123456")
                .phoneVerified(true)
                .build());

        int firstInsert = insertNotification(user.getId());
        int duplicateInsert = insertNotification(user.getId());

        Page<Notification> page = notificationRepository
                .findAllByUserIdOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        PageRequest.of(0, 20)
                );

        assertThat(firstInsert).isEqualTo(1);
        assertThat(duplicateInsert).isZero();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().isRead()).isFalse();
        assertThat(page.getContent().getFirst().getReadAt()).isNull();
        assertThat(notificationRepository
                .countByUserIdAndIsReadFalse(user.getId()))
                .isEqualTo(1);
    }

    private int insertNotification(Long userId) {
        return notificationRepository.insertIfAbsent(
                userId,
                "EVENT_PUBLISHED",
                "New event",
                "Friday Night",
                "EVENT",
                42L,
                "EVENT_PUBLISHED:42"
        );
    }
}

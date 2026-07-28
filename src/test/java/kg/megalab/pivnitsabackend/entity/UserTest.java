package kg.megalab.pivnitsabackend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("Проверка корректной сборки через Builder и работы геттеров")
    void shouldCreateUserWithBuilder() {
        User user = User.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .phone("+79991112233")
                .email("test@example.com")
                .build();

        assertThat(user.getFirstName()).isEqualTo("Иван");
        assertThat(user.getLastName()).isEqualTo("Иванов");
        assertThat(user.getPhone()).isEqualTo("+79991112233");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Проверка логики equals и hashCode по id")
    void testEqualsAndHashCode() {
        User user1 = User.builder().id(1L).phone("+79991112233").build();
        User user2 = User.builder().id(1L).phone("+79990000000").build();
        User user3 = User.builder().id(2L).phone("+79991112233").build();

        // Сравниваем по ID: если ID одинаковый, объекты равны
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
}
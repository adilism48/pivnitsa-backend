package kg.megalab.pivnitsabackend.dto.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
        @NotBlank(message = "Требуется Имя")
        String fullName,

        @NotBlank(message = "Требуется Email")
        @Email(message = "Введенный Email неправильный")
        String email,

        @NotBlank(message = "Требуется пароль")
        @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
        String password,

        @NotBlank(message = "Требуется Роль")
        String role


) {

}

package kg.megalab.pivnitsabackend.dto.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kg.megalab.pivnitsabackend.entity.StaffRole;
import org.aspectj.weaver.ast.Not;

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

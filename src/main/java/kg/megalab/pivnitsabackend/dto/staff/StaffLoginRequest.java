package kg.megalab.pivnitsabackend.dto.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StaffLoginRequest (
    @NotBlank(message = "Требуется Email")
    @Email(message = "Неправильно введен Email")
    String email,

    @NotBlank(message = "Требуется пароль")
    String password
) {

}


package kg.megalab.pivnitsabackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Имя обязательно")
        @Size(
                max = 100,
                message = "Имя не должно превышать 100 символов"
        )
        String firstName,

        @NotBlank(message = "Фамилия обязательна")
        @Size(
                max = 100,
                message = "Фамилия не должна превышать 100 символов"
        )
        @NotBlank String lastName,

        @Email(message = "Некорректный формат e-mail")
        @Size(
                max = 255,
                message = "E-mail не должен превышать 255 символов"
        ) String email
) {
}

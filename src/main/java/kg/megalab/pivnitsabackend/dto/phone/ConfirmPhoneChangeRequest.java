package kg.megalab.pivnitsabackend.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmPhoneChangeRequest(
        @NotBlank
        @Pattern(regexp = "^\\+996\\d{9}$")
        String newPhone,

        @NotBlank(message = "Код обязателен")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "Код должен состоять из 6 цифр"
        )
        String code
) {
}

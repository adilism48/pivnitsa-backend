package kg.megalab.pivnitsabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendOtpRequest (
    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^\\+[0-9]{10,15}$",
            message = "Phone must be in international format"
    )
    String phone
) {

}

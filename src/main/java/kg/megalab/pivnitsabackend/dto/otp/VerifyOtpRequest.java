package kg.megalab.pivnitsabackend.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^\\+[0-9]{10,15}$",
                message = "Phone must be in international format"
        )
        String phone,


        @NotBlank(message = "OTP code is required")
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "OTP must contain 6 digits"
        )
        String code

) {
}
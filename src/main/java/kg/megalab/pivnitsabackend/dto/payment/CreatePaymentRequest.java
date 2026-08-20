package kg.megalab.pivnitsabackend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
        @NotBlank(message = "Payment provider is required")
        @Size(max = 32, message = "Payment provider is too long")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Payment provider has an invalid format"
    )
        String providerCode
) {
}

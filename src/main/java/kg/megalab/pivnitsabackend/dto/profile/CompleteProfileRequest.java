package kg.megalab.pivnitsabackend.dto.profile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record CompleteProfileRequest(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @AssertTrue(message = "Terms must be accepted")
        boolean termsAccepted,

        @AssertTrue(message = "Privacy policy must be accepted")
        boolean privacyAccepted

) {
}
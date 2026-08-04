package kg.megalab.pivnitsabackend.dto.phone;

import kg.megalab.pivnitsabackend.dto.UserResponse;

public record PhoneChangeResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}

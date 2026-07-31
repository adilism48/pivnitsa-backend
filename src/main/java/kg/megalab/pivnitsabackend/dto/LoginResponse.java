package kg.megalab.pivnitsabackend.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String stage,
        UserResponse user
) {
}

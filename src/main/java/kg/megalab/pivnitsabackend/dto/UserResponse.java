package kg.megalab.pivnitsabackend.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone
) {
}

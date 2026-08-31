package kg.megalab.pivnitsabackend.dto.staff;

public record StaffLoginResponse(
        String token,
        String fullName,
        String role
) {
    
}


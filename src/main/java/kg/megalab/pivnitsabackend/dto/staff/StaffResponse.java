package kg.megalab.pivnitsabackend.dto.staff;

public record StaffResponse(
        Long id,
        String fullName,
        String email,
        String role,
        boolean canViewFinancialReport,
        boolean active
) {

}


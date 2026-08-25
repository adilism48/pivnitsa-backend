package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.admin.AdminBookingResponse;
import kg.megalab.pivnitsabackend.dto.admin.CancelBookingRequest;
import kg.megalab.pivnitsabackend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getBookingsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(bookingService.getAdminBookings(from, to));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request
    ) {
        bookingService.cancelBookingByAdmin(id, request.reason());
        return ResponseEntity.noContent().build();
    }
}

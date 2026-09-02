package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.admin.AdminBookingResponse;
import kg.megalab.pivnitsabackend.dto.admin.CancelBookingRequest;
import kg.megalab.pivnitsabackend.dto.admin.CreateEventRequest;
import kg.megalab.pivnitsabackend.dto.event.EventResponse;
import kg.megalab.pivnitsabackend.dto.admin.UpdateEventRequest;
import kg.megalab.pivnitsabackend.service.BookingService;
import kg.megalab.pivnitsabackend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

    private final BookingService bookingService;
    private final EventService eventService;

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getBookingsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(bookingService.getAdminBookings(from, to));
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request
    ) {
        bookingService.cancelBookingByAdmin(id, request.reason());
        return ResponseEntity.noContent().build();
    }


    @PostMapping(value = "/events",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> create(@Valid @ModelAttribute CreateEventRequest request) {

        EventResponse response = eventService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/events/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> update(@Valid @ModelAttribute UpdateEventRequest request, @PathVariable Long id) {

        EventResponse response = eventService.update(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/events/{id}/publish")
    public ResponseEntity<Void> publishEvent(@PathVariable Long id) {
        eventService.publishEvent(id);
        return ResponseEntity.ok().build();
    }
}

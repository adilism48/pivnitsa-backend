package kg.megalab.pivnitsabackend.controller;

import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.dto.staff.CreateStaffRequest;
import kg.megalab.pivnitsabackend.dto.staff.StaffResponse;
import kg.megalab.pivnitsabackend.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/manage")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long id,
            Authentication authentication
    ) {
        staffService.deactivateStaff(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/financial-access")
    public ResponseEntity<StaffResponse> setFinancialAccess(
            @PathVariable Long id,
            @RequestParam boolean canView
    ) {
        StaffResponse response = staffService.setFinancialAccess(id, canView);
        return ResponseEntity.ok(response);
    }
}
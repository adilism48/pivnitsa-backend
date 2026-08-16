package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.service.StaffService;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginRequest;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/staff/auth")
@RequiredArgsConstructor
public class StaffAuthController {
    private final StaffService staffService;

    @PostMapping("/login")
    public ResponseEntity<StaffLoginResponse> login(@Valid @RequestBody StaffLoginRequest request) {
        StaffLoginResponse response = staffService.login(request);
        return ResponseEntity.ok(response);
    }
}

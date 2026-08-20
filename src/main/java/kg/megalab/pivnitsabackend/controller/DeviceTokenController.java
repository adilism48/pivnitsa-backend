package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.notification.RegisterTokenRequest;
import kg.megalab.pivnitsabackend.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/tokens")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal String phone,
            @RequestBody @Valid RegisterTokenRequest request
    ) {
        deviceTokenService.saveOrUpdateTokenByPhone(phone, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<Void> removeToken(@RequestParam String token) {
        deviceTokenService.removeToken(token);
        return ResponseEntity.noContent().build();
    }
}

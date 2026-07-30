package kg.megalab.pivnitsabackend.controller;

import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.dto.CompleteProfileRequest;
import kg.megalab.pivnitsabackend.dto.SendOtpRequest;
import kg.megalab.pivnitsabackend.dto.VerifyOtpRequest;
import kg.megalab.pivnitsabackend.service.OtpService;
import kg.megalab.pivnitsabackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        otpService.sendOtp(request.phone(), request.channel());

        return ResponseEntity.ok(
                Map.of("message", "Код успешно отправлен.")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String preAuthToken = otpService.verifyOtp(request.phone(), request.code());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Номер телефона успешно подтвержден.",
                        "token", preAuthToken,
                        "stage", "PROFILE_REQUIRED"
                )
        );
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<Map<String, String>> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CompleteProfileRequest request
    ) {

        userService.completeProfile(authHeader, request);

        return ResponseEntity.ok(
                Map.of("message", "Профиль успешно заполнен.")
        );
    }
}
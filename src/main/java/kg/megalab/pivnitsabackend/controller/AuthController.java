package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.profile.CompleteProfileRequest;
import kg.megalab.pivnitsabackend.dto.otp.SendOtpRequest;
import kg.megalab.pivnitsabackend.dto.otp.VerifyOtpRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import kg.megalab.pivnitsabackend.security.JwtService;
import kg.megalab.pivnitsabackend.security.LogoutService;
import kg.megalab.pivnitsabackend.service.OtpService;
import kg.megalab.pivnitsabackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;
    private final JwtService jwtService;
    private final LogoutService logoutService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        otpService.sendOtp(request.phone(), request.channel(), OtpPurpose.REGISTRATION);

        return ResponseEntity.ok(
                Map.of("message", "Код успешно отправлен.")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {

        otpService.verifyOtp(request.phone(), request.code(), OtpPurpose.REGISTRATION);

        String preAuthToken =
                jwtService.generatePreAuthToken(request.phone());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Номер телефона успешно подтвержден.",
                        "token", preAuthToken,
                        "stage", "PROFILE_REQUIRED"
                )
        );
    }

    @PostMapping("/complete-profile")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<Map<String, Object>> completeProfile(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody CompleteProfileRequest request
    ) {
        User user = userService.completeProfile(phone, request);
        String accessToken = jwtService.generateAccessToken(phone);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Профиль успешно заполнен.",
                        "token", accessToken,
                        "stage", "AUTHENTICATED",
                        "user", Map.of(
                                "id", user.getId(),
                                "firstName", user.getFirstName(),
                                "lastName", user.getLastName(),
                                "phone", user.getPhone()
                        )
                )
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader) {

        logoutService.logout(authHeader);

        return ResponseEntity.noContent().build();
    }
}

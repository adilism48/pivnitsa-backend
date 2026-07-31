package kg.megalab.pivnitsabackend.controller;

import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.dto.LoginResponse;
import kg.megalab.pivnitsabackend.dto.SendOtpRequest;
import kg.megalab.pivnitsabackend.dto.VerifyOtpRequest;
import kg.megalab.pivnitsabackend.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @Valid @RequestBody SendOtpRequest request
            ) {
        loginService.sendOtp(
                request.phone(),
                request.channel()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Код для входа успешно отправлен.",
                        "retryAfterSeconds", 60
                )
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
            ) {
        LoginResponse response = loginService.verifyOtp(
                request.phone(),
                request.code()
        );

        return ResponseEntity.ok(response);
    }
}

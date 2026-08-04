package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.phone.ConfirmPhoneChangeRequest;
import kg.megalab.pivnitsabackend.dto.phone.PhoneChangeResponse;
import kg.megalab.pivnitsabackend.dto.phone.SendPhoneChangeOtpRequest;
import kg.megalab.pivnitsabackend.service.PhoneChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/phone-change")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class PhoneChangeController {

    private final PhoneChangeService phoneChangeService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(
            Authentication authentication,
            @Valid @RequestBody SendPhoneChangeOtpRequest request
    ) {
        phoneChangeService.sendOtp(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok(Map.of(
                "message", "Код отправлен на новый номер",
                "retryAfterSeconds", 60
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PhoneChangeResponse> confirm(
            Authentication authentication,
            @Valid @RequestBody ConfirmPhoneChangeRequest request
    ) {
        return ResponseEntity.ok(
                phoneChangeService.confirm(
                        authentication.getName(),
                        request
                )
        );
    }
}

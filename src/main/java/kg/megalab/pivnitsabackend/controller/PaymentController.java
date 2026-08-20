package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.config.PaymentRedirectProperties;
import kg.megalab.pivnitsabackend.dto.payment.CreatePaymentRequest;
import kg.megalab.pivnitsabackend.dto.payment.PaymentResponse;
import kg.megalab.pivnitsabackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentRedirectProperties redirectProperties;

    @PostMapping("/bookings/{bookingId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            Authentication authentication,

            @PathVariable
            @Positive
            Long bookingId,

            @RequestHeader("Idempotency-Key")
            @Size(min = 1, max = 100)
            @Pattern(regexp = "^[A-Za-z0-9._:-]+$")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreatePaymentRequest request
    ) {
        PaymentResponse response = PaymentResponse.from(
                paymentService.createPayment(
                        bookingId,
                        authentication.getName(),
                        request.providerCode(),
                        idempotencyKey,
                        redirectProperties.successUrl(),
                        redirectProperties.failureUrl()
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            Authentication authentication,

            @PathVariable
            @Positive
            Long paymentId
    ) {
        PaymentResponse response = PaymentResponse.from(
                paymentService.getPayment(
                        paymentId,
                        authentication.getName()
                )
        );

        return ResponseEntity.ok(response);
    }
}

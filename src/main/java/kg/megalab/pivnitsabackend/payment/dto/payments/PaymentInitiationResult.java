package kg.megalab.pivnitsabackend.payment.dto.payments;

import kg.megalab.pivnitsabackend.entity.PaymentStatus;

import java.net.URI;
import java.time.OffsetDateTime;

public record PaymentInitiationResult(
        Long paymentId,
        Long bookingId,
        PaymentStatus status,
        URI paymentUrl,
        OffsetDateTime expiresAt
) {
}

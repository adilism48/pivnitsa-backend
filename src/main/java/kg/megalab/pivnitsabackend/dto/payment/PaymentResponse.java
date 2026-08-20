package kg.megalab.pivnitsabackend.dto.payment;

import kg.megalab.pivnitsabackend.entity.PaymentStatus;
import kg.megalab.pivnitsabackend.payment.dto.payments.PaymentInitiationResult;

import java.net.URI;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        PaymentStatus status,
        URI paymentUrl,
        OffsetDateTime expiresAt
) {
    public static PaymentResponse from(PaymentInitiationResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.bookingId(),
                result.status(),
                result.paymentUrl(),
                result.expiresAt()
        );
    }
}

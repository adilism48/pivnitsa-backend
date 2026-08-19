package kg.megalab.pivnitsabackend.payment.dto.payments;

import java.net.URI;
import java.time.OffsetDateTime;

public record CreatePaymentResult(
        String providerPaymentId,
        String providerStatus,
        URI paymentUrl,
        OffsetDateTime expiresAt
) {
}
package kg.megalab.pivnitsabackend.payment.dto.payments;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentWebhookEvent(
        String eventId,
        String eventType,
        String providerPaymentId,
        String providerStatus,
        ProviderPaymentState state,
        BigDecimal amount,
        String currency,
        OffsetDateTime occurredAt
) {
}

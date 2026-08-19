package kg.megalab.pivnitsabackend.payment.dto.payments;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProviderPaymentResult(
        String providerPaymentId,
        String providerStatus,
        ProviderPaymentState state,
        BigDecimal amount,
        String currency,
        OffsetDateTime paidAt
) {
}
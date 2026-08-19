package kg.megalab.pivnitsabackend.payment.dto.refund;

import java.math.BigDecimal;

public record RefundCommand(
        String providerPaymentId,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String reason
) {
}
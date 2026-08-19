package kg.megalab.pivnitsabackend.payment.dto.payments;

import java.math.BigDecimal;
import java.net.URI;

public record CreatePaymentCommand(
        String merchantOrderId,
        BigDecimal amount,
        String currency,
        URI successUrl,
        URI failureUrl
) {
}
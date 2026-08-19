package kg.megalab.pivnitsabackend.payment.dto.payments;

public enum ProviderPaymentState {
    PENDING,
    PAID,
    FAILED,
    EXPIRED,
    REFUNDED,
    UNKNOWN
}
package kg.megalab.pivnitsabackend.payment.dto.refund;

public record RefundResult(
        String providerRefundId,
        String providerStatus,
        ProviderRefundState state
) {
}
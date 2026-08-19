package kg.megalab.pivnitsabackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_merchant_order_id",
                        columnNames = "merchant_order_id"
                ),
                @UniqueConstraint(
                        name = "uk_payments_provider_payment_id",
                        columnNames = {"provider", "provider_payment_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "merchant_order_id", nullable = false, length = 100)
    private String merchantOrderId;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "provider_status", length = 64)
    private String providerStatus;

    @Column(name = "payment_url", length = 2048)
    private String paymentUrl;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(
            name = "refunded_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal refundedAmount;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (status == null) {
            status = PaymentStatus.PENDING;
        }

        if (currency == null) {
            currency = "KGS";
        }

        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
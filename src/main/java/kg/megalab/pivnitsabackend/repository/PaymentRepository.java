package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Payment;
import kg.megalab.pivnitsabackend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantOrderId(String merchantOrderId);

    Optional<Payment> findByProviderAndProviderPaymentId(
            String provider,
            String providerPaymentId
    );

    List<Payment> findAllByBookingIdOrderByCreatedAtDesc(Long bookingId);

    Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
            Long bookingId,
            PaymentStatus status
    );

    @Query("""
        SELECT p
        FROM Payment p
        JOIN Booking b ON b.id = p.bookingId
        WHERE p.id = :paymentId
          AND b.userId = :userId
        """)
    Optional<Payment> findByIdAndBookingUserId(
            @Param("paymentId") Long paymentId,
            @Param("userId") Long userId
    );
}
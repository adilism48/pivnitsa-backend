package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantOrderId(String merchantOrderId);

    Optional<Payment> findByProviderAndProviderPaymentId(
            String provider,
            String providerPaymentId
    );

    List<Payment> findAllByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
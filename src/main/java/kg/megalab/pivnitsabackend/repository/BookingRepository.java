package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        JOIN Payment p ON p.bookingId = b.id
        WHERE b.userId = :userId
          AND b.status = :status
          AND b.bookingAt > CURRENT_TIMESTAMP
          AND p.status = kg.megalab.pivnitsabackend.entity.PaymentStatus.SUCCEEDED
        """)
    boolean existsActivePaidBookingByUserId(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status
    );

    Optional<Booking> findByIdAndUserId(
            Long bookingId,
            Long userId
    );
}
package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

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

    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.clubTableId = :tableId
              AND b.status IN (kg.megalab.pivnitsabackend.entity.BookingStatus.CONFIRMED,
                                kg.megalab.pivnitsabackend.entity.BookingStatus.PENDING_PAYMENT)
              AND b.bookingAt > CURRENT_TIMESTAMP
            """)
    boolean existsActiveBookingByTableId(@Param("tableId") Long tableId);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, OffsetDateTime threshold);
}
package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.dto.booking.BookingResponse;
import kg.megalab.pivnitsabackend.entity.Booking;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
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
    SELECT new kg.megalab.pivnitsabackend.dto.booking.BookingResponse(
        b.id,
        t.tableNumber,
        b.guestsCount,
        b.bookingAt,
        b.amount,
        b.status
    )
    FROM Booking b
    JOIN ClubTable t ON t.id = b.clubTableId
    WHERE b.userId = :userId
      AND b.status IN :statuses
      AND b.bookingAt >= :now
    ORDER BY b.bookingAt ASC
    """)
    List<BookingResponse> findActiveBookings(
            @Param("userId") Long userId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") OffsetDateTime now
    );

    @Query("""
    SELECT new kg.megalab.pivnitsabackend.dto.booking.BookingResponse(
        b.id,
        t.tableNumber,
        b.guestsCount,
        b.bookingAt,
        b.amount,
        b.status
    )
    FROM Booking b
    JOIN ClubTable t ON t.id = b.clubTableId
    WHERE b.userId = :userId
      AND b.status IN :statuses
      AND b.bookingAt < :now
    ORDER BY b.bookingAt DESC
    """)
    List<BookingResponse> findBookingHistory(
            @Param("userId") Long userId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") OffsetDateTime now
    );
}
package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.dto.admin.AdminBookingResponse;
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
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.clubTableId = :tableId
              AND b.status IN (kg.megalab.pivnitsabackend.entity.BookingStatus.CONFIRMED,
                                kg.megalab.pivnitsabackend.entity.BookingStatus.PENDING_PAYMENT)
              AND b.bookingAt > CURRENT_TIMESTAMP
            """)
    boolean existsActiveBookingByTableId(@Param("tableId") Long tableId);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, OffsetDateTime threshold);

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

    @Query("""
    SELECT new kg.megalab.pivnitsabackend.dto.admin.AdminBookingResponse(
        b.id,
        t.tableNumber,
        b.bookingAt,
        u.firstName,
        u.lastName,
        u.phone,
        b.guestsCount,
        b.amount,
        b.status,
        p.status,
        b.cancellationReason
    )
    FROM Booking b
    JOIN ClubTable t ON t.id = b.clubTableId
    JOIN User u ON u.id = b.userId
    LEFT JOIN Payment p ON p.bookingId = b.id
       AND p.id = (SELECT MAX(p2.id) FROM Payment p2 WHERE p2.bookingId = b.id)
    WHERE b.bookingAt >= :startOfDay AND b.bookingAt < :endOfDay
    ORDER BY b.bookingAt ASC
    """)
    List<AdminBookingResponse> findAdminBookingsByDate(
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );
}
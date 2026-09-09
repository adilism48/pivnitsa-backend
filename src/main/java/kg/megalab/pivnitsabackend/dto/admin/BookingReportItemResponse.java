package kg.megalab.pivnitsabackend.dto.admin;

import kg.megalab.pivnitsabackend.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookingReportItemResponse(
        Long bookingId,
        OffsetDateTime bookingAt,
        String tableNumber,
        BookingStatus status,
        BigDecimal bookingAmount,
        BigDecimal paidAmount,
        String cancellationReason
) {
    public BookingReportItemResponse {
        paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;
    }
}
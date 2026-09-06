package kg.megalab.pivnitsabackend.dto.admin;

import java.math.BigDecimal;

public record BookingReportSummaryResponse(
        Long totalBookings,
        BigDecimal totalPrepayment,
        Long totalCancellations
) {
    public BookingReportSummaryResponse {
        totalBookings = totalBookings != null ? totalBookings : 0L;
        totalPrepayment = totalPrepayment != null ? totalPrepayment : BigDecimal.ZERO;
        totalCancellations = totalCancellations != null ? totalCancellations : 0L;
    }
}

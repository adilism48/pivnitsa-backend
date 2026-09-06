package kg.megalab.pivnitsabackend.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;

public record BookingReportResponse(
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        BookingReportSummaryResponse summary,
        List<BookingReportItemResponse> items
) {
}

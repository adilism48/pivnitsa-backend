package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.admin.BookingReportItemResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportSummaryResponse;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.PaymentStatus;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingReportService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public BookingReportResponse getOwnerReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        BookingReportSummaryResponse summary = bookingRepository.getReportSummary(
                startDate,
                endDate,
                PaymentStatus.SUCCEEDED,
                BookingStatus.CANCELLED
        );

        List<BookingReportItemResponse> items = bookingRepository.getReportItems(
                startDate,
                endDate,
                PaymentStatus.SUCCEEDED
        );

        return new BookingReportResponse(startDate, endDate, summary, items);
    }
}

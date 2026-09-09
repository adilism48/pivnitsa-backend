package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.admin.BookingReportItemResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportSummaryResponse;
import kg.megalab.pivnitsabackend.entity.BookingStatus;
import kg.megalab.pivnitsabackend.entity.PaymentStatus;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingReportService bookingReportService;

    private static final ZoneId BISHKEK_ZONE = ZoneId.of("Asia/Bishkek");

    @Test
    @DisplayName("Должен возвращать полный отчёт (сводка + детализация) при корректном периоде")
    void getFinancialReport_ShouldReturnAggregatedReport() {
        // ARRANGE
        OffsetDateTime startDate = OffsetDateTime.now(BISHKEK_ZONE).minusDays(7);
        OffsetDateTime endDate = OffsetDateTime.now(BISHKEK_ZONE);

        BookingReportSummaryResponse mockSummary = new BookingReportSummaryResponse(
                10L, new BigDecimal("25000.00"), 2L
        );

        BookingReportItemResponse mockItem = new BookingReportItemResponse(
                101L,
                OffsetDateTime.now(BISHKEK_ZONE),
                "VIP-1",
                BookingStatus.CONFIRMED,
                new BigDecimal("2500.00"),
                new BigDecimal("2500.00"),
                null // cancellationReason
        );

        when(bookingRepository.getReportSummary(eq(startDate), eq(endDate), eq(PaymentStatus.SUCCEEDED), eq(BookingStatus.CANCELLED)))
                .thenReturn(mockSummary);
        when(bookingRepository.getReportItems(eq(startDate), eq(endDate), eq(PaymentStatus.SUCCEEDED)))
                .thenReturn(List.of(mockItem));

        // ACT
        BookingReportResponse report = bookingReportService.getOwnerReport(startDate, endDate);

        // ASSERT
        assertNotNull(report);
        assertEquals(mockSummary, report.summary());
        assertEquals(1, report.items().size());
        assertEquals(mockItem, report.items().getFirst());

        verify(bookingRepository, times(1)).getReportSummary(startDate, endDate, PaymentStatus.SUCCEEDED, BookingStatus.CANCELLED);
        verify(bookingRepository, times(1)).getReportItems(startDate, endDate, PaymentStatus.SUCCEEDED);
    }

    @Test
    @DisplayName("Должен возвращать пустую сводку и пустой список, если за период данных нет")
    void getFinancialReport_ShouldReturnEmptyDataWhenNoBookingsFound() {
        // ARRANGE
        OffsetDateTime startDate = OffsetDateTime.now(BISHKEK_ZONE).minusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now(BISHKEK_ZONE).minusDays(20);

        BookingReportSummaryResponse emptySummary = new BookingReportSummaryResponse(0L, BigDecimal.ZERO, 0L);

        when(bookingRepository.getReportSummary(any(), any(), any(), any())).thenReturn(emptySummary);
        when(bookingRepository.getReportItems(any(), any(), any())).thenReturn(List.of());

        // ACT
        BookingReportResponse report = bookingReportService.getOwnerReport(startDate, endDate);

        // ASSERT
        assertNotNull(report);
        assertEquals(0L, report.summary().totalBookings());
        assertEquals(0, BigDecimal.ZERO.compareTo(report.summary().totalPrepayment()));
        assertTrue(report.items().isEmpty());
    }
}

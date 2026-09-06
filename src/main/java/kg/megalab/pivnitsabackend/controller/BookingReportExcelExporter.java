package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.admin.BookingReportItemResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportResponse;
import kg.megalab.pivnitsabackend.dto.admin.BookingReportSummaryResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class BookingReportExcelExporter {

    private static final ZoneId BISHKEK_ZONE = ZoneId.of("Asia/Bishkek");
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ByteArrayInputStream exportToExcel(BookingReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Финансовый отчёт");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle periodStyle = createPeriodStyle(workbook);

            int currentRow = 0;

            // Summary header
            Row rowPeriod = sheet.createRow(currentRow++);
            Cell periodLabelCell = rowPeriod.createCell(0);
            periodLabelCell.setCellValue("Период отчёта:");
            periodLabelCell.setCellStyle(periodStyle);

            String periodFrom = report.startDate()
                    .atZoneSameInstant(BISHKEK_ZONE)
                    .format(PERIOD_FORMATTER);
            String periodTo = report.endDate()
                    .atZoneSameInstant(BISHKEK_ZONE)
                    .format(PERIOD_FORMATTER);

            Cell periodValueCell = rowPeriod.createCell(1);
            periodValueCell.setCellValue(periodFrom + " – " + periodTo);
            periodValueCell.setCellStyle(periodStyle);

            // Summary (Report)
            BookingReportSummaryResponse summary = report.summary();

            Row rowTotal = sheet.createRow(currentRow++);
            rowTotal.createCell(0).setCellValue("Всего броней:");
            rowTotal.createCell(1).setCellValue(summary.totalBookings());

            Row rowPaid = sheet.createRow(currentRow++);
            rowPaid.createCell(0).setCellValue("Общая предоплата:");
            Cell cellPaid = rowPaid.createCell(1);
            cellPaid.setCellValue(summary.totalPrepayment().doubleValue());
            cellPaid.setCellStyle(moneyStyle);

            Row rowCancelled = sheet.createRow(currentRow++);
            rowCancelled.createCell(0).setCellValue("Всего отмен:");
            rowCancelled.createCell(1).setCellValue(summary.totalCancellations());

            currentRow++; // Разделительный пустой ряд

            // Items header
            Row tableHeader = sheet.createRow(currentRow++);
            String[] headers = {"ID Брони", "Дата визита", "Стол", "Статус", "Депозит", "Оплачено", "Причина отмены"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = tableHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Items
            for (BookingReportItemResponse item : report.items()) {
                Row row = sheet.createRow(currentRow++);

                row.createCell(0).setCellValue(item.bookingId());

                Cell dateCell = row.createCell(1);

                LocalDateTime bishkekDateTime = item.bookingAt()
                        .atZoneSameInstant(BISHKEK_ZONE)
                        .toLocalDateTime();
                dateCell.setCellValue(bishkekDateTime);
                dateCell.setCellStyle(dateStyle);

                row.createCell(2).setCellValue(item.tableNumber());
                row.createCell(3).setCellValue(item.status().name());

                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(item.bookingAmount().doubleValue());
                amountCell.setCellStyle(moneyStyle);

                Cell paidCell = row.createCell(5);
                paidCell.setCellValue(item.paidAmount().doubleValue());
                paidCell.setCellStyle(moneyStyle);

                row.createCell(6).setCellValue(item.cancellationReason() != null ? item.cancellationReason() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при генерации Excel файла отчета", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd.MM.yyyy HH:mm"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createPeriodStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}

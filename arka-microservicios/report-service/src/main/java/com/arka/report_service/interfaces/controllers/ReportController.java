package com.arka.report_service.interfaces.controllers;

import com.arka.report_service.application.dto.WeeklySalesReportResponse;
import com.arka.report_service.application.usecases.GenerateWeeklySalesReportUseCase;
import com.arka.report_service.infrastructure.export.CsvExportService;
import com.arka.report_service.infrastructure.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final GenerateWeeklySalesReportUseCase generateWeeklySalesReportUseCase;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/sales/weekly")
    public ResponseEntity<WeeklySalesReportResponse> getWeeklySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/reports/sales/weekly - startDate: {}, endDate: {}", startDate, endDate);

        WeeklySalesReportResponse report = generateWeeklySalesReportUseCase.execute(startDate, endDate);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales/weekly/current")
    public ResponseEntity<WeeklySalesReportResponse> getCurrentWeeklySalesReport() {
        log.info("GET /api/v1/reports/sales/weekly/current");

        // Calcular semana actual (lunes a domingo)
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);

        WeeklySalesReportResponse report = generateWeeklySalesReportUseCase.execute(monday, sunday);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales/weekly/previous")
    public ResponseEntity<WeeklySalesReportResponse> getPreviousWeeklySalesReport() {
        log.info("GET /api/v1/reports/sales/weekly/previous");

        // Calcular semana anterior
        LocalDate today = LocalDate.now();
        LocalDate previousMonday = today.minusDays(today.getDayOfWeek().getValue() + 6);
        LocalDate previousSunday = previousMonday.plusDays(6);

        WeeklySalesReportResponse report = generateWeeklySalesReportUseCase.execute(previousMonday, previousSunday);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales/weekly/export/csv")
    public ResponseEntity<Resource> exportWeeklySalesReportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/reports/sales/weekly/export/csv - startDate: {}, endDate: {}", startDate, endDate);

        try {
            WeeklySalesReportResponse report = generateWeeklySalesReportUseCase.execute(startDate, endDate);
            File csvFile = csvExportService.exportWeeklySalesReport(report);

            FileSystemResource resource = new FileSystemResource(csvFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvFile.getName() + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
            log.error("Error exporting CSV report: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar reporte CSV: " + e.getMessage(), e);
        }
    }

    @GetMapping("/sales/weekly/export/pdf")
    public ResponseEntity<Resource> exportWeeklySalesReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/reports/sales/weekly/export/pdf - startDate: {}, endDate: {}", startDate, endDate);

        try {
            WeeklySalesReportResponse report = generateWeeklySalesReportUseCase.execute(startDate, endDate);
            File pdfFile = pdfExportService.exportWeeklySalesReport(report);

            FileSystemResource resource = new FileSystemResource(pdfFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFile.getName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error exporting PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar reporte PDF: " + e.getMessage(), e);
        }
    }
}

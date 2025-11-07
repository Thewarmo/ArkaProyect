package com.arka.report_service.infrastructure.export;

import com.arka.report_service.application.dto.TopCustomerDTO;
import com.arka.report_service.application.dto.TopProductDTO;
import com.arka.report_service.application.dto.WeeklySalesReportResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfExportService {

    @Value("${reports.export.temp-directory}")
    private String tempDirectory;

    public File exportWeeklySalesReport(WeeklySalesReportResponse report) throws IOException {
        log.info("Exporting weekly sales report to PDF");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("weekly_sales_report_%s_%s_%s.pdf",
                report.getStartDate(), report.getEndDate(), timestamp);
        File pdfFile = new File(tempDirectory, filename);

        // Crear directorio si no existe
        pdfFile.getParentFile().mkdirs();

        try {
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph title = new Paragraph("REPORTE SEMANAL DE VENTAS - ARKA")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Información general
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Período: " + report.getStartDate() + " a " + report.getEndDate()));
            document.add(new Paragraph("Total Ventas: $" + report.getTotalSales()));
            document.add(new Paragraph("Total Órdenes: " + report.getTotalOrders()));
            document.add(new Paragraph("Valor Promedio por Orden: $" + report.getAverageOrderValue()));
            document.add(new Paragraph("\n"));

            // Productos más vendidos
            document.add(new Paragraph("PRODUCTOS MÁS VENDIDOS").setBold().setFontSize(14));
            Table productsTable = new Table(new float[]{1, 3, 2, 2, 2});
            productsTable.addHeaderCell("ID");
            productsTable.addHeaderCell("Nombre");
            productsTable.addHeaderCell("Cantidad");
            productsTable.addHeaderCell("Ingresos");
            productsTable.addHeaderCell("# Órdenes");

            for (TopProductDTO product : report.getTopProducts()) {
                productsTable.addCell(String.valueOf(product.getProductId()));
                productsTable.addCell(product.getProductName());
                productsTable.addCell(String.valueOf(product.getTotalQuantitySold()));
                productsTable.addCell("$" + product.getTotalRevenue());
                productsTable.addCell(String.valueOf(product.getOrdersCount()));
            }
            document.add(productsTable);
            document.add(new Paragraph("\n"));

            // Clientes más frecuentes
            document.add(new Paragraph("CLIENTES MÁS FRECUENTES").setBold().setFontSize(14));
            Table customersTable = new Table(new float[]{1, 2, 2, 1, 2, 2});
            customersTable.addHeaderCell("ID");
            customersTable.addHeaderCell("Nombre");
            customersTable.addHeaderCell("Empresa");
            customersTable.addHeaderCell("Órdenes");
            customersTable.addHeaderCell("Total");
            customersTable.addHeaderCell("Promedio");

            for (TopCustomerDTO customer : report.getTopCustomers()) {
                customersTable.addCell(String.valueOf(customer.getCustomerId()));
                customersTable.addCell(customer.getCustomerName());
                customersTable.addCell(customer.getCompanyName());
                customersTable.addCell(String.valueOf(customer.getTotalOrders()));
                customersTable.addCell("$" + customer.getTotalSpent());
                customersTable.addCell("$" + customer.getAverageOrderValue());
            }
            document.add(customersTable);

            // Footer
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();

            log.info("PDF report exported successfully: {}", pdfFile.getAbsolutePath());
            return pdfFile;
        } catch (Exception e) {
            log.error("Error exporting PDF report: {}", e.getMessage(), e);
            throw new IOException("Error al exportar reporte PDF: " + e.getMessage(), e);
        }
    }
}

package com.arka.report_service.infrastructure.export;

import com.arka.report_service.application.dto.TopCustomerDTO;
import com.arka.report_service.application.dto.TopProductDTO;
import com.arka.report_service.application.dto.WeeklySalesReportResponse;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvExportService {

    @Value("${reports.export.temp-directory}")
    private String tempDirectory;

    public File exportWeeklySalesReport(WeeklySalesReportResponse report) throws IOException {
        log.info("Exporting weekly sales report to CSV");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("weekly_sales_report_%s_%s_%s.csv",
                report.getStartDate(), report.getEndDate(), timestamp);
        File csvFile = new File(tempDirectory, filename);

        // Crear directorio si no existe
        csvFile.getParentFile().mkdirs();

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            // Header principal
            writer.writeNext(new String[]{"REPORTE SEMANAL DE VENTAS - ARKA"});
            writer.writeNext(new String[]{""});
            writer.writeNext(new String[]{"Período", report.getStartDate() + " a " + report.getEndDate()});
            writer.writeNext(new String[]{"Total Ventas", "$" + report.getTotalSales()});
            writer.writeNext(new String[]{"Total Órdenes", String.valueOf(report.getTotalOrders())});
            writer.writeNext(new String[]{"Valor Promedio Orden", "$" + report.getAverageOrderValue()});
            writer.writeNext(new String[]{""});

            // Productos más vendidos
            writer.writeNext(new String[]{"PRODUCTOS MÁS VENDIDOS"});
            writer.writeNext(new String[]{"ID Producto", "Nombre", "Cantidad Vendida", "Ingresos", "# Órdenes"});
            for (TopProductDTO product : report.getTopProducts()) {
                writer.writeNext(new String[]{
                        String.valueOf(product.getProductId()),
                        product.getProductName(),
                        String.valueOf(product.getTotalQuantitySold()),
                        "$" + product.getTotalRevenue(),
                        String.valueOf(product.getOrdersCount())
                });
            }
            writer.writeNext(new String[]{""});

            // Clientes más frecuentes
            writer.writeNext(new String[]{"CLIENTES MÁS FRECUENTES"});
            writer.writeNext(new String[]{"ID Cliente", "Nombre", "Empresa", "# Órdenes", "Total Gastado", "Promedio por Orden"});
            for (TopCustomerDTO customer : report.getTopCustomers()) {
                writer.writeNext(new String[]{
                        String.valueOf(customer.getCustomerId()),
                        customer.getCustomerName(),
                        customer.getCompanyName(),
                        String.valueOf(customer.getTotalOrders()),
                        "$" + customer.getTotalSpent(),
                        "$" + customer.getAverageOrderValue()
                });
            }
        }

        log.info("CSV report exported successfully: {}", csvFile.getAbsolutePath());
        return csvFile;
    }
}

package com.arka.product_service.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LowStockReportResponse {

    private LocalDateTime reportDate;
    private Integer threshold;
    private Integer totalProductsFound;
    private List<ProductResponse> lowStockProducts;

    // Estadísticas adicionales
    private Integer totalStockValue; // Total del valor en inventario bajo
    private String criticalityLevel;  // "LOW", "MEDIUM", "HIGH"
}

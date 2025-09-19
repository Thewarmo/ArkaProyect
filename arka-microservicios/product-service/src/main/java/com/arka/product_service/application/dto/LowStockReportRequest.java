package com.arka.product_service.application.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LowStockReportRequest {

    @Min(value = 0, message = "El umbral debe ser mayor o igual a 0")
    private Integer threshold = 10; // Valor por defecto

    private Long categoryId; // Filtro opcional por categoría
    private Long brandId;    // Filtro opcional por marca
}

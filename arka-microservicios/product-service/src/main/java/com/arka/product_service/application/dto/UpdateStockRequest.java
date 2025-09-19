package com.arka.product_service.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer newStock;

    private String reason; // Motivo del cambio (opcional)

}

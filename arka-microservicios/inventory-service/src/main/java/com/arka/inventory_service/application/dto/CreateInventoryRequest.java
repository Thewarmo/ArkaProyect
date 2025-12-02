package com.arka.inventory_service.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El stock disponible es obligatorio")
    @Min(value = 0, message = "El stock disponible no puede ser negativo")
    private Integer availableStock;

    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Integer reservedStock = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock = 10;
}

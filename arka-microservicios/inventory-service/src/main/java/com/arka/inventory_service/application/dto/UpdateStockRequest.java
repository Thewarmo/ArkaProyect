package com.arka.inventory_service.application.dto;

import com.arka.inventory_service.domain.entities.MovementType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType movementType;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    @NotBlank(message = "La razón es obligatoria")
    private String reason;

    private String orderId;
}

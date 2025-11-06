package com.arka.inventory_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un movimiento de inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    private Long id;
    private Long inventoryId;
    private Long productId;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String orderId; // ID de la orden relacionada (opcional)
    private LocalDateTime createdAt;

    /**
     * Calcula la diferencia de stock
     */
    public Integer getStockDifference() {
        return newStock - previousStock;
    }

    /**
     * Verifica si es un movimiento de entrada
     */
    public boolean isIncoming() {
        return movementType == MovementType.IN ||
               movementType == MovementType.RELEASED ||
               (movementType == MovementType.ADJUSTMENT && quantity > 0);
    }

    /**
     * Verifica si es un movimiento de salida
     */
    public boolean isOutgoing() {
        return movementType == MovementType.OUT ||
               movementType == MovementType.RESERVED ||
               (movementType == MovementType.ADJUSTMENT && quantity < 0);
    }
}

package com.arka.order_service.infrastructure.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer totalStock;
    private Integer minStockLevel;
    private Boolean isLowStock;
}

package com.arka.inventory_service.application.dto;

import lombok.*;
import java.time.LocalDateTime;

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
    private Integer minimumStock;
    private boolean lowStock;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.arka.inventory_service.infrastructure.persistence.mappers;

import com.arka.inventory_service.domain.entities.StockMovement;
import com.arka.inventory_service.infrastructure.persistence.model.StockMovementJPA;
import org.springframework.stereotype.Component;

@Component
public class StockMovementEntityMapper {

    public StockMovement toDomain(StockMovementJPA jpa) {
        if (jpa == null) return null;
        return StockMovement.builder()
                .id(jpa.getId())
                .inventoryId(jpa.getInventoryId())
                .productId(jpa.getProductId())
                .movementType(jpa.getMovementType())
                .quantity(jpa.getQuantity())
                .previousStock(jpa.getPreviousStock())
                .newStock(jpa.getNewStock())
                .reason(jpa.getReason())
                .orderId(jpa.getOrderId())
                .createdAt(jpa.getCreatedAt())
                .build();
    }

    public StockMovementJPA toJPA(StockMovement domain) {
        if (domain == null) return null;
        return StockMovementJPA.builder()
                .id(domain.getId())
                .inventoryId(domain.getInventoryId())
                .productId(domain.getProductId())
                .movementType(domain.getMovementType())
                .quantity(domain.getQuantity())
                .previousStock(domain.getPreviousStock())
                .newStock(domain.getNewStock())
                .reason(domain.getReason())
                .orderId(domain.getOrderId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}

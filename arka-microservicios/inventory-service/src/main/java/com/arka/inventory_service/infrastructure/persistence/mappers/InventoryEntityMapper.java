package com.arka.inventory_service.infrastructure.persistence.mappers;

import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.infrastructure.persistence.model.InventoryJPA;
import org.springframework.stereotype.Component;

@Component
public class InventoryEntityMapper {

    public Inventory toDomain(InventoryJPA jpa) {
        if (jpa == null) return null;
        return Inventory.builder()
                .id(jpa.getId())
                .productId(jpa.getProductId())
                .availableStock(jpa.getAvailableStock())
                .reservedStock(jpa.getReservedStock())
                .minimumStock(jpa.getMinimumStock())
                .version(jpa.getVersion())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    public InventoryJPA toJPA(Inventory domain) {
        if (domain == null) return null;
        return InventoryJPA.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .availableStock(domain.getAvailableStock())
                .reservedStock(domain.getReservedStock())
                .minimumStock(domain.getMinimumStock())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

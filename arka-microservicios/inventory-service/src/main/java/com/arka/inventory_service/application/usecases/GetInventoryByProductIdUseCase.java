package com.arka.inventory_service.application.usecases;

import com.arka.inventory_service.application.dto.InventoryResponse;
import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.domain.exceptions.InventoryNotFoundException;
import com.arka.inventory_service.domain.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetInventoryByProductIdUseCase {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public InventoryResponse execute(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("productId", productId.toString()));

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .availableStock(inventory.getAvailableStock())
                .reservedStock(inventory.getReservedStock())
                .totalStock(inventory.getTotalStock())
                .minimumStock(inventory.getMinimumStock())
                .lowStock(inventory.isLowStock())
                .version(inventory.getVersion())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}

package com.arka.inventory_service.application.usecases;

import com.arka.inventory_service.application.dto.InventoryResponse;
import com.arka.inventory_service.application.dto.ReserveStockRequest;
import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.domain.exceptions.*;
import com.arka.inventory_service.domain.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReserveStockUseCase {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse execute(ReserveStockRequest request) {
        try {
            Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                    .orElseThrow(() -> new InventoryNotFoundException("productId", request.getProductId().toString()));

            if (!inventory.hasAvailableStock(request.getQuantity())) {
                throw new InsufficientStockException(
                    request.getProductId(),
                    inventory.getAvailableStock(),
                    request.getQuantity()
                );
            }

            inventory.reserveStock(request.getQuantity());
            Inventory saved = inventoryRepository.save(inventory);

            return mapToResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConcurrentModificationException(request.getProductId());
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
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

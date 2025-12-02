package com.arka.inventory_service.application.usecases;

import com.arka.inventory_service.application.dto.CreateInventoryRequest;
import com.arka.inventory_service.application.dto.InventoryResponse;
import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.domain.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse execute(CreateInventoryRequest request) {
        log.info("Creating inventory for productId: {}", request.getProductId());

        // Verificar si ya existe inventario para este producto
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new IllegalArgumentException(
                    "Ya existe inventario para el producto con ID: " + request.getProductId());
        }

        // Crear nueva entidad de inventario
        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .availableStock(request.getAvailableStock())
                .reservedStock(request.getReservedStock() != null ? request.getReservedStock() : 0)
                .minimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 10)
                .version(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Guardar en el repositorio
        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Inventory created successfully with id: {}", savedInventory.getId());

        // Retornar respuesta
        return InventoryResponse.builder()
                .id(savedInventory.getId())
                .productId(savedInventory.getProductId())
                .availableStock(savedInventory.getAvailableStock())
                .reservedStock(savedInventory.getReservedStock())
                .totalStock(savedInventory.getTotalStock())
                .minimumStock(savedInventory.getMinimumStock())
                .lowStock(savedInventory.isLowStock())
                .version(savedInventory.getVersion())
                .createdAt(savedInventory.getCreatedAt())
                .updatedAt(savedInventory.getUpdatedAt())
                .build();
    }
}

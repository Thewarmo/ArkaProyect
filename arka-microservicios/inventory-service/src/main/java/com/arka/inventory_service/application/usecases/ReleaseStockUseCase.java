package com.arka.inventory_service.application.usecases;

import com.arka.inventory_service.application.dto.ReleaseStockRequest;
import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.domain.entities.MovementType;
import com.arka.inventory_service.domain.entities.StockMovement;
import com.arka.inventory_service.domain.exceptions.InventoryNotFoundException;
import com.arka.inventory_service.domain.repositories.InventoryRepository;
import com.arka.inventory_service.domain.repositories.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReleaseStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public void execute(ReleaseStockRequest request) {
        log.info("Liberando stock reservado: Producto={}, Cantidad={}",
                request.getProductId(), request.getQuantity());

        // Buscar inventario del producto
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(
                        "No se encontró inventario para el producto: " + request.getProductId()));

        // Validar que hay suficiente stock reservado
        if (inventory.getReservedStock() < request.getQuantity()) {
            throw new IllegalStateException(
                    String.format("Stock reservado insuficiente. Reservado: %d, Solicitado liberar: %d",
                            inventory.getReservedStock(), request.getQuantity()));
        }

        // Liberar stock (mover de reservado a disponible)
        inventory.releaseReservedStock(request.getQuantity());
        inventory.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios en el inventario
        Inventory updatedInventory = inventoryRepository.save(inventory);

        // Registrar movimiento de stock
        StockMovement movement = StockMovement.builder()
                .inventoryId(updatedInventory.getId())
                .productId(updatedInventory.getProductId())
                .movementType(MovementType.RELEASED)
                .quantity(request.getQuantity())
                .previousStock(inventory.getAvailableStock() - request.getQuantity())
                .newStock(updatedInventory.getAvailableStock())
                .reason("Liberación de stock reservado")
                .createdAt(LocalDateTime.now())
                .build();

        stockMovementRepository.save(movement);

        log.info("Stock liberado exitosamente: Producto={}, Cantidad={}, Stock disponible ahora={}",
                request.getProductId(), request.getQuantity(), updatedInventory.getAvailableStock());
    }
}

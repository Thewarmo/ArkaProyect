package com.arka.inventory_service.domain.repositories;

import com.arka.inventory_service.domain.entities.MovementType;
import com.arka.inventory_service.domain.entities.StockMovement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de movimientos de stock
 */
public interface StockMovementRepository {

    /**
     * Busca un movimiento por ID
     */
    Optional<StockMovement> findById(Long id);

    /**
     * Obtiene todos los movimientos de un producto
     */
    List<StockMovement> findByProductId(Long productId);

    /**
     * Obtiene movimientos por tipo
     */
    List<StockMovement> findByMovementType(MovementType movementType);

    /**
     * Obtiene movimientos en un rango de fechas
     */
    List<StockMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Obtiene movimientos de un producto en un rango de fechas
     */
    List<StockMovement> findByProductIdAndDateRange(Long productId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Guarda un movimiento
     */
    StockMovement save(StockMovement movement);

    /**
     * Obtiene todos los movimientos
     */
    List<StockMovement> findAll();
}

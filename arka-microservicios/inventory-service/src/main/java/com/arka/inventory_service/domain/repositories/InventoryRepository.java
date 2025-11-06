package com.arka.inventory_service.domain.repositories;

import com.arka.inventory_service.domain.entities.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de inventario (Port en Clean Architecture).
 * Define las operaciones de persistencia sin depender de tecnologías específicas.
 */
public interface InventoryRepository {

    /**
     * Busca inventario por su ID
     */
    Optional<Inventory> findById(Long id);

    /**
     * Busca inventario por ID del producto
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Obtiene todo el inventario
     */
    List<Inventory> findAll();

    /**
     * Obtiene productos con bajo stock
     */
    List<Inventory> findLowStockProducts();

    /**
     * Obtiene productos sin stock disponible
     */
    List<Inventory> findOutOfStockProducts();

    /**
     * Guarda o actualiza inventario
     */
    Inventory save(Inventory inventory);

    /**
     * Elimina inventario por ID
     */
    void deleteById(Long id);

    /**
     * Verifica si existe inventario para un producto
     */
    boolean existsByProductId(Long productId);
}

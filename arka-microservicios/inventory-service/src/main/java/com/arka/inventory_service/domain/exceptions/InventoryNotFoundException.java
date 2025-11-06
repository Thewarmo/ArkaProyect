package com.arka.inventory_service.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra el inventario
 */
public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String message) {
        super(message);
    }

    public InventoryNotFoundException(Long id) {
        super("Inventario no encontrado con ID: " + id);
    }

    public InventoryNotFoundException(String field, String value) {
        super(String.format("Inventario no encontrado con %s: %s", field, value));
    }
}

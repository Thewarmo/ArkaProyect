package com.arka.inventory_service.domain.exceptions;

/**
 * Excepción lanzada cuando hay una modificación concurrente del inventario
 */
public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException(Long productId) {
        super("El inventario del producto " + productId +
              " fue modificado por otra transacción. Por favor, intente nuevamente.");
    }
}

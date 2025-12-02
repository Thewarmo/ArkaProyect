package com.arka.inventory_service.domain.exceptions;

public class DuplicateInventoryException extends RuntimeException {

    private final Long productId;

    public DuplicateInventoryException(Long productId) {
        super("Ya existe inventario para el producto con ID: " + productId +
              ". Use el endpoint GET /api/v1/inventory/product/" + productId +
              " para consultar o PUT para actualizar.");
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}

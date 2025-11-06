package com.arka.inventory_service.domain.exceptions;

/**
 * Excepción lanzada cuando no hay stock suficiente
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Integer available;
    private final Integer requested;

    public InsufficientStockException(Long productId, Integer available, Integer requested) {
        super(String.format("Stock insuficiente para producto %d. Disponible: %d, Solicitado: %d",
            productId, available, requested));
        this.productId = productId;
        this.available = available;
        this.requested = requested;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getAvailable() {
        return available;
    }

    public Integer getRequested() {
        return requested;
    }
}

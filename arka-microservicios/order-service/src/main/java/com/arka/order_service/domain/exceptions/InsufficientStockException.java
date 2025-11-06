package com.arka.order_service.domain.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, Integer available, Integer requested) {
        super(String.format("Stock insuficiente para producto %d. Disponible: %d, Solicitado: %d",
            productId, available, requested));
    }
}

package com.arka.product_service.domain.exceptions;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Producto con ID " + productId + " no encontrado");
    }
}

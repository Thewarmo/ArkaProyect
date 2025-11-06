package com.arka.order_service.domain.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Orden no encontrada con ID: " + id);
    }

    public OrderNotFoundException(String orderNumber) {
        super("Orden no encontrada con número: " + orderNumber);
    }
}

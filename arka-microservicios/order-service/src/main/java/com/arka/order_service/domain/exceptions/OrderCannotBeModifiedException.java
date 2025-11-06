package com.arka.order_service.domain.exceptions;

import com.arka.order_service.domain.entities.OrderStatus;

public class OrderCannotBeModifiedException extends RuntimeException {
    public OrderCannotBeModifiedException(OrderStatus status) {
        super("La orden no puede ser modificada en estado: " + status);
    }
}

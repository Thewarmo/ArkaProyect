package com.arka.customer_service.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un cliente
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long id) {
        super("Cliente no encontrado con ID: " + id);
    }

    public CustomerNotFoundException(String field, String value) {
        super(String.format("Cliente no encontrado con %s: %s", field, value));
    }
}

package com.arka.customer_service.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un cliente que ya existe
 */
public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }

    public CustomerAlreadyExistsException(String field, String value) {
        super(String.format("Ya existe un cliente con %s: %s", field, value));
    }
}

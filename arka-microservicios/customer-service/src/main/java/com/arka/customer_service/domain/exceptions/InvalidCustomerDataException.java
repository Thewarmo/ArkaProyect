package com.arka.customer_service.domain.exceptions;

/**
 * Excepción lanzada cuando los datos del cliente son inválidos
 */
public class InvalidCustomerDataException extends RuntimeException {

    public InvalidCustomerDataException(String message) {
        super(message);
    }
}

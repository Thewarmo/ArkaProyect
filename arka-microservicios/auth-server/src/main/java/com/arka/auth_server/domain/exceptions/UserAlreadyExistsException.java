package com.arka.auth_server.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar un usuario que ya existe
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("Ya existe un usuario con %s: %s", field, value));
    }
}

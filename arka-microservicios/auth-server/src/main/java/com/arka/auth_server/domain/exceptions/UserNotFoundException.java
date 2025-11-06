package com.arka.auth_server.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un usuario
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con ID: " + id);
    }

    public UserNotFoundException(String field, String value) {
        super(String.format("Usuario no encontrado con %s: %s", field, value));
    }
}

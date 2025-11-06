package com.arka.auth_server.domain.exceptions;

/**
 * Excepción lanzada cuando un usuario deshabilitado intenta autenticarse
 */
public class UserDisabledException extends RuntimeException {

    public UserDisabledException() {
        super("El usuario está deshabilitado");
    }

    public UserDisabledException(String username) {
        super("El usuario está deshabilitado: " + username);
    }
}

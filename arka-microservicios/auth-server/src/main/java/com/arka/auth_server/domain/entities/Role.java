package com.arka.auth_server.domain.entities;

/**
 * Enum que representa los roles disponibles en el sistema.
 */
public enum Role {
    /**
     * Rol de administrador con acceso completo al sistema
     */
    ADMIN,

    /**
     * Rol de cliente con acceso limitado a funciones de compra
     */
    CUSTOMER
}

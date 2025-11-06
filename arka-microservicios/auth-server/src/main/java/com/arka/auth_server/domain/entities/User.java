package com.arka.auth_server.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un usuario del sistema.
 * Esta clase es un POJO puro sin anotaciones de JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String email;
    private String password; // Debe estar hasheada
    private String firstName;
    private String lastName;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Valida si el usuario está activo y puede autenticarse
     */
    public boolean canAuthenticate() {
        return enabled;
    }

    /**
     * Verifica si el usuario tiene rol de administrador
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Verifica si el usuario tiene rol de cliente
     */
    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    /**
     * Obtiene el nombre completo del usuario
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

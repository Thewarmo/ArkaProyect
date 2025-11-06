package com.arka.auth_server.application.dto;

import com.arka.auth_server.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la validación de token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private boolean valid;
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String message;
}

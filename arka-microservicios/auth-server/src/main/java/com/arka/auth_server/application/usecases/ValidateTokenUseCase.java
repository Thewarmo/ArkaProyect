package com.arka.auth_server.application.usecases;

import com.arka.auth_server.application.dto.ValidateTokenResponse;
import com.arka.auth_server.domain.entities.Role;
import com.arka.auth_server.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para validar un token JWT
 */
@Service
@RequiredArgsConstructor
public class ValidateTokenUseCase {

    private final JwtUtil jwtUtil;

    public ValidateTokenResponse execute(String token) {
        try {
            // Validar el token
            boolean isValid = jwtUtil.validateToken(token);

            if (!isValid) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .message("Token inválido o expirado")
                        .build();
            }

            // Extraer información del token
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            String email = jwtUtil.extractEmail(token);
            String roleStr = jwtUtil.extractRole(token);
            Role role = Role.valueOf(roleStr);

            return ValidateTokenResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .email(email)
                    .role(role)
                    .message("Token válido")
                    .build();

        } catch (Exception e) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Error al validar token: " + e.getMessage())
                    .build();
        }
    }
}

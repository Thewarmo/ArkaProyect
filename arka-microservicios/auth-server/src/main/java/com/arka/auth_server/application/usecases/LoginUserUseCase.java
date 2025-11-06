package com.arka.auth_server.application.usecases;

import com.arka.auth_server.application.dto.AuthResponse;
import com.arka.auth_server.application.dto.LoginRequest;
import com.arka.auth_server.domain.entities.User;
import com.arka.auth_server.domain.exceptions.InvalidCredentialsException;
import com.arka.auth_server.domain.exceptions.UserDisabledException;
import com.arka.auth_server.domain.exceptions.UserNotFoundException;
import com.arka.auth_server.domain.repositories.UserRepository;
import com.arka.auth_server.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para autenticar un usuario (login)
 */
@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginRequest request) {
        // Buscar el usuario por username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("username", request.getUsername()));

        // Verificar si el usuario está habilitado
        if (!user.canAuthenticate()) {
            throw new UserDisabledException(user.getUsername());
        }

        // Verificar la contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generar token JWT
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        // Retornar respuesta
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login exitoso")
                .build();
    }
}

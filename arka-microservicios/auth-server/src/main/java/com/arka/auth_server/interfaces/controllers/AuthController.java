package com.arka.auth_server.interfaces.controllers;

import com.arka.auth_server.application.dto.*;
import com.arka.auth_server.application.usecases.GetUserByIdUseCase;
import com.arka.auth_server.application.usecases.LoginUserUseCase;
import com.arka.auth_server.application.usecases.RegisterUserUseCase;
import com.arka.auth_server.application.usecases.ValidateTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la autenticación y gestión de usuarios
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;

    /**
     * POST /api/v1/auth/register - Registrar un nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/login - Autenticar un usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/validate - Validar un token JWT
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = validateTokenUseCase.execute(request.getToken());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/auth/validate/{token} - Validar un token JWT (alternativa GET)
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<ValidateTokenResponse> validateTokenGet(@PathVariable String token) {
        ValidateTokenResponse response = validateTokenUseCase.execute(token);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/auth/users/{userId} - Obtener información de un usuario
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = getUserByIdUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }
}

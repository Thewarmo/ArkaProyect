package com.arka.auth_server.interfaces.controllers;

import com.arka.auth_server.application.dto.*;
import com.arka.auth_server.application.usecases.*;
import com.arka.auth_server.domain.entities.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de Controller de Autenticación
 *
 * Este test demuestra:
 * - Registro de usuarios con validación
 * - Login y generación de JWT
 * - Validación de tokens JWT
 * - Obtención de información de usuario
 * - Validación de entrada de seguridad
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private ValidateTokenUseCase validateTokenUseCase;

    @MockBean
    private GetUserByIdUseCase getUserByIdUseCase;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        authResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token")
                .type("Bearer")
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .message("Authentication successful")
                .build();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        // Given
        given(registerUserUseCase.execute(any(RegisterRequest.class)))
                .willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectRegisterWithoutUsername() throws Exception {
        // Given
        registerRequest.setUsername(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegisterWithShortUsername() throws Exception {
        // Given
        registerRequest.setUsername("ab"); // Menos de 3 caracteres

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegisterWithInvalidEmail() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegisterWithShortPassword() throws Exception {
        // Given
        registerRequest.setPassword("12345"); // Menos de 6 caracteres

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegisterWithoutRole() throws Exception {
        // Given
        registerRequest.setRole(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Given
        given(loginUserUseCase.execute(any(LoginRequest.class)))
                .willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void shouldRejectLoginWithoutUsername() throws Exception {
        // Given
        loginRequest.setUsername(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLoginWithoutPassword() throws Exception {
        // Given
        loginRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateTokenWithPost() throws Exception {
        // Given
        ValidateTokenRequest validateRequest = ValidateTokenRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token")
                .build();

        ValidateTokenResponse validateResponse = ValidateTokenResponse.builder()
                .valid(true)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .message("Token is valid")
                .build();

        given(validateTokenUseCase.execute(any(String.class)))
                .willReturn(validateResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldValidateTokenWithGet() throws Exception {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        ValidateTokenResponse validateResponse = ValidateTokenResponse.builder()
                .valid(true)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .message("Token is valid")
                .build();

        given(validateTokenUseCase.execute(eq(token)))
                .willReturn(validateResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .role(Role.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(getUserByIdUseCase.execute(1L))
                .willReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist()); // IMPORTANTE: Password nunca debe exponerse
    }

    @Test
    void shouldRegisterAdminUser() throws Exception {
        // Given
        registerRequest.setRole(Role.ADMIN);

        AuthResponse adminResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.token")
                .type("Bearer")
                .userId(2L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.ADMIN)
                .message("Admin registration successful")
                .build();

        given(registerUserUseCase.execute(any(RegisterRequest.class)))
                .willReturn(adminResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldHandleSpecialCharactersInUsername() throws Exception {
        // Given
        registerRequest.setUsername("user.name+test");

        given(registerUserUseCase.execute(any(RegisterRequest.class)))
                .willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest());
    }
}
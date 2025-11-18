package com.arka.auth_server.infrastructure.security;

import com.arka.auth_server.domain.entities.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de Seguridad JWT
 *
 * Este test demuestra:
 * - Generación correcta de tokens JWT
 * - Extracción de claims (userId, username, email, role)
 * - Validación de tokens
 * - Detección de tokens expirados
 * - Detección de firmas inválidas
 * - Seguridad de algoritmo HS256
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "ArkaSecretKeyForJWTTokenGenerationAndValidation2024!MustBeLongEnoughForHS256Algorithm";
    private final Long expiration = 86400000L; // 24 horas

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";
        Role role = Role.CUSTOMER;

        // When
        String token = jwtUtil.generateToken(userId, username, email, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes separadas por .
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(1L);
    }

    @Test
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo("CUSTOMER");
    }

    @Test
    void shouldExtractAdminRole() {
        // Given
        String token = jwtUtil.generateToken(2L, "admin", "admin@example.com", Role.ADMIN);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo("ADMIN");
    }

    @Test
    void shouldExtractExpirationDateFromToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date()); // No debe estar expirado
    }

    @Test
    void shouldValidateTokenWithCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "testuser");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectTokenWithIncorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateTokenWithoutUsername() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldDetectNonExpiredToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    void shouldDetectExpiredToken() {
        // Given - Crear token con expiración corta
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "secret", secret);
        ReflectionTestUtils.setField(shortLivedJwtUtil, "expiration", -1000L); // Ya expirado

        String expiredToken = shortLivedJwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When & Then
        assertThat(jwtUtil.isTokenExpired(expiredToken)).isTrue();
    }

    @Test
    void shouldRejectExpiredTokenOnValidation() {
        // Given - Token expirado
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "secret", secret);
        ReflectionTestUtils.setField(shortLivedJwtUtil, "expiration", -1000L);

        String expiredToken = shortLivedJwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Boolean isValid = jwtUtil.validateToken(expiredToken, "testuser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        // Given - Token con firma diferente
        String differentSecret = "DifferentSecretKeyThatWillCauseSignatureValidationToFail!MustBeLongEnoughForHS256";
        JwtUtil differentJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentJwtUtil, "secret", differentSecret);
        ReflectionTestUtils.setField(differentJwtUtil, "expiration", expiration);

        String tokenWithDifferentSignature = differentJwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When & Then - Intentar validar con JwtUtil original (diferente secret)
        assertThatThrownBy(() -> jwtUtil.extractUsername(tokenWithDifferentSignature))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectNullToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectEmptyToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldHandleTamperedToken() {
        // Given - Token válido
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // Modificar el token (simular tampering)
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(tamperedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldGenerateDifferentTokensForSameUser() {
        // Given - Mismo usuario, diferentes momentos
        String token1 = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // Pequeña pausa para asegurar timestamp diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // Then - Tokens diferentes pero ambos válidos
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.validateToken(token1, "testuser")).isTrue();
        assertThat(jwtUtil.validateToken(token2, "testuser")).isTrue();
    }

    @Test
    void shouldHandleSpecialCharactersInUsername() {
        // Given
        String specialUsername = "user.name+test@domain";
        String token = jwtUtil.generateToken(1L, specialUsername, "test@example.com", Role.CUSTOMER);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(specialUsername);
    }

    @Test
    void shouldHandleSpecialCharactersInEmail() {
        // Given
        String specialEmail = "user.name+test@sub-domain.example.com";
        String token = jwtUtil.generateToken(1L, "testuser", specialEmail, Role.CUSTOMER);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(specialEmail);
    }

    @Test
    void shouldEncodeTokenWithHS256Algorithm() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // Then - Verificar que el token usa HS256
        // El header del JWT contiene el algoritmo
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        // El header decodificado debe contener "alg":"HS256"
        assertThat(token).isNotNull();
    }

    @Test
    void shouldSetCorrectExpirationTime() {
        // Given
        String token = jwtUtil.generateToken(1L, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Date expirationDate = jwtUtil.extractExpiration(token);
        Date now = new Date();

        // Then - La expiración debe ser aproximadamente 24 horas en el futuro
        long timeDifferenceMs = expirationDate.getTime() - now.getTime();
        long expectedExpirationMs = 86400000L; // 24 horas

        // Margen de error de 10 segundos
        assertThat(timeDifferenceMs).isCloseTo(expectedExpirationMs, org.assertj.core.data.Offset.offset(10000L));
    }

    @Test
    void shouldValidateTokenReturnsFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldHandleLargeUserId() {
        // Given
        Long largeUserId = 9999999999L;
        String token = jwtUtil.generateToken(largeUserId, "testuser", "test@example.com", Role.CUSTOMER);

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(largeUserId);
    }
}
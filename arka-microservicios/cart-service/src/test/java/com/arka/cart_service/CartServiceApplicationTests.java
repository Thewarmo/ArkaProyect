package com.arka.cart_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context test for Cart Service.
 *
 * NOTA: Este test requiere MongoDB y se deshabilita en CI (GitHub Actions)
 * ya que el MongoDB embebido de Flapdoodle no está disponible para Ubuntu 24.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class CartServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

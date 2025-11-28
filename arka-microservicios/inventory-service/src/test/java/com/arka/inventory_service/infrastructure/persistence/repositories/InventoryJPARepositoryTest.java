package com.arka.inventory_service.infrastructure.persistence.repositories;

import com.arka.inventory_service.infrastructure.persistence.model.InventoryJPA;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de Repositorio de Inventario con énfasis en concurrencia
 *
 * Este test demuestra:
 * - Operaciones CRUD con inventario
 * - Optimistic Locking con @Version
 * - Detección de conflictos de concurrencia
 * - Consultas personalizadas (low stock, out of stock)
 * - Manejo de stock disponible y reservado
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryJPARepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private InventoryJPARepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private InventoryJPA testInventory;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testInventory = InventoryJPA.builder()
                .productId(1L)
                .availableStock(100)
                .reservedStock(0)
                .minimumStock(10)
                .build();
    }

    @Test
    void shouldSaveAndFindInventoryById() {
        // Given
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<InventoryJPA> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getProductId()).isEqualTo(1L);
        assertThat(found.get().getAvailableStock()).isEqualTo(100);
        assertThat(found.get().getReservedStock()).isEqualTo(0);
        assertThat(found.get().getMinimumStock()).isEqualTo(10);
        assertThat(found.get().getVersion()).isNotNull();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindInventoryByProductId() {
        // Given
        repository.save(testInventory);
        entityManager.flush();

        // When
        Optional<InventoryJPA> found = repository.findByProductId(1L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getProductId()).isEqualTo(1L);
        assertThat(found.get().getAvailableStock()).isEqualTo(100);
    }

    @Test
    void shouldEnforceUniqueProductIdConstraint() {
        // Given
        repository.save(testInventory);
        entityManager.flush();

        // When & Then - Intentar guardar otro inventario con el mismo productId
        InventoryJPA duplicate = InventoryJPA.builder()
                .productId(1L) // Mismo productId
                .availableStock(50)
                .reservedStock(0)
                .minimumStock(5)
                .build();

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    @Test
    void shouldCheckIfInventoryExistsByProductId() {
        // Given
        repository.save(testInventory);
        entityManager.flush();

        // When
        boolean exists = repository.existsByProductId(1L);
        boolean notExists = repository.existsByProductId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldUpdateInventoryAndIncrementVersion() {
        // Given
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        Integer initialVersion = saved.getVersion();

        // When - Actualizar stock disponible
        saved.setAvailableStock(80);
        saved.setReservedStock(20);
        InventoryJPA updated = repository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getAvailableStock()).isEqualTo(80);
        assertThat(updated.getReservedStock()).isEqualTo(20);
        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDetectOptimisticLockingConflict() {
        // Given - Guardar inventario inicial
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        // Simular dos transacciones concurrentes obteniendo la misma entidad
        InventoryJPA transaction1 = repository.findById(saved.getId()).get();
        InventoryJPA transaction2 = repository.findById(saved.getId()).get();

        // When - Transaction 1 actualiza primero
        transaction1.setAvailableStock(90);
        repository.save(transaction1);
        entityManager.flush();
        entityManager.clear();

        // Then - Transaction 2 intenta actualizar con versión obsoleta
        transaction2.setAvailableStock(95);
        assertThatThrownBy(() -> {
            repository.save(transaction2);
            entityManager.flush();
        }).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void shouldReserveStockAndUpdateVersion() {
        // Given
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        // When - Reservar 30 unidades
        InventoryJPA inventory = repository.findById(saved.getId()).get();
        inventory.setAvailableStock(inventory.getAvailableStock() - 30);
        inventory.setReservedStock(inventory.getReservedStock() + 30);
        InventoryJPA updated = repository.save(inventory);
        entityManager.flush();

        // Then
        assertThat(updated.getAvailableStock()).isEqualTo(70);
        assertThat(updated.getReservedStock()).isEqualTo(30);
        assertThat(updated.getAvailableStock() + updated.getReservedStock()).isEqualTo(100);
    }

    @Test
    void shouldFindLowStockProducts() {
        // Given - Crear varios inventarios
        repository.save(testInventory); // Total: 100, Min: 10 (NO bajo stock)

        InventoryJPA lowStock1 = InventoryJPA.builder()
                .productId(2L)
                .availableStock(5)
                .reservedStock(3)
                .minimumStock(10) // Total: 8, Min: 10 (SÍ bajo stock)
                .build();
        repository.save(lowStock1);

        InventoryJPA lowStock2 = InventoryJPA.builder()
                .productId(3L)
                .availableStock(10)
                .reservedStock(0)
                .minimumStock(15) // Total: 10, Min: 15 (SÍ bajo stock)
                .build();
        repository.save(lowStock2);

        entityManager.flush();

        // When
        List<InventoryJPA> lowStockProducts = repository.findLowStockProducts();

        // Then
        assertThat(lowStockProducts).hasSize(2);
        assertThat(lowStockProducts).extracting(InventoryJPA::getProductId)
                .containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void shouldFindOutOfStockProducts() {
        // Given
        repository.save(testInventory); // Available: 100 (NO out of stock)

        InventoryJPA outOfStock1 = InventoryJPA.builder()
                .productId(2L)
                .availableStock(0) // Out of stock
                .reservedStock(5)
                .minimumStock(10)
                .build();
        repository.save(outOfStock1);

        InventoryJPA outOfStock2 = InventoryJPA.builder()
                .productId(3L)
                .availableStock(0) // Out of stock
                .reservedStock(0)
                .minimumStock(5)
                .build();
        repository.save(outOfStock2);

        entityManager.flush();

        // When
        List<InventoryJPA> outOfStockProducts = repository.findOutOfStockProducts();

        // Then
        assertThat(outOfStockProducts).hasSize(2);
        assertThat(outOfStockProducts).extracting(InventoryJPA::getProductId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(outOfStockProducts).allMatch(i -> i.getAvailableStock() == 0);
    }

    @Test
    void shouldHandleMultipleStockOperations() {
        // Given
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        // When - Simular múltiples operaciones
        // Operación 1: Reservar 30
        InventoryJPA inv = repository.findByProductId(1L).get();
        inv.setAvailableStock(70);
        inv.setReservedStock(30);
        repository.save(inv);
        entityManager.flush();

        // Operación 2: Confirmar 15 reservados
        inv = repository.findByProductId(1L).get();
        inv.setReservedStock(15);
        repository.save(inv);
        entityManager.flush();

        // Operación 3: Agregar 50 nuevos
        inv = repository.findByProductId(1L).get();
        inv.setAvailableStock(120);
        repository.save(inv);
        entityManager.flush();

        // Then
        InventoryJPA finalInventory = repository.findByProductId(1L).get();
        assertThat(finalInventory.getAvailableStock()).isEqualTo(120);
        assertThat(finalInventory.getReservedStock()).isEqualTo(15);
        assertThat(finalInventory.getVersion()).isGreaterThan(saved.getVersion());
    }

    @Test
    void shouldDeleteInventory() {
        // Given
        InventoryJPA saved = repository.save(testInventory);
        entityManager.flush();
        Long inventoryId = saved.getId();

        // When
        repository.deleteById(inventoryId);
        entityManager.flush();

        // Then
        Optional<InventoryJPA> deleted = repository.findById(inventoryId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldHandleZeroStockCorrectly() {
        // Given - Inventario con stock cero
        InventoryJPA zeroStock = InventoryJPA.builder()
                .productId(99L)
                .availableStock(0)
                .reservedStock(0)
                .minimumStock(5)
                .build();

        // When
        InventoryJPA saved = repository.save(zeroStock);
        entityManager.flush();

        // Then
        Optional<InventoryJPA> found = repository.findByProductId(99L);
        assertThat(found).isPresent();
        assertThat(found.get().getAvailableStock()).isEqualTo(0);
        assertThat(found.get().getReservedStock()).isEqualTo(0);
    }
}
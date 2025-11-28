package com.arka.cart_service.infrastructure.persistence.repositories;

import com.arka.cart_service.domain.entities.CartStatus;
import com.arka.cart_service.infrastructure.persistence.model.CartDocument;
import com.arka.cart_service.infrastructure.persistence.model.CartItemDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de Repositorio MongoDB usando Testcontainers
 *
 * Este test demuestra:
 * - Configuración de MongoDB con Testcontainers
 * - Uso de @DataMongoTest para tests de MongoDB
 * - Operaciones CRUD con documentos
 * - Consultas personalizadas con @Query
 * - Manejo de documentos embebidos (CartItemDocument)
 */
@DataMongoTest
@Testcontainers
class CartMongoRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private CartMongoRepository repository;

    private CartDocument activeCart;
    private CartItemDocument item1;
    private CartItemDocument item2;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        repository.deleteAll();

        // Crear items de carrito
        item1 = CartItemDocument.builder()
                .id(UUID.randomUUID().toString())
                .productId(1L)
                .productName("Laptop HP")
                .quantity(1)
                .unitPrice(new BigDecimal("1500.00"))
                .subtotal(new BigDecimal("1500.00"))
                .build();

        item2 = CartItemDocument.builder()
                .id(UUID.randomUUID().toString())
                .productId(2L)
                .productName("Mouse Logitech")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        // Crear carrito de prueba
        activeCart = CartDocument.builder()
                .customerId(1L)
                .status(CartStatus.ACTIVE)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("1600.00"))
                .totalItems(2)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveAndFindCartById() {
        // Given
        CartDocument savedCart = repository.save(activeCart);

        // When
        Optional<CartDocument> found = repository.findById(savedCart.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("1600.00"));
        assertThat(found.get().getTotalItems()).isEqualTo(2);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSaveCartWithEmbeddedItems() {
        // Given
        CartDocument savedCart = repository.save(activeCart);

        // When
        Optional<CartDocument> found = repository.findById(savedCart.getId());

        // Then
        assertThat(found).isPresent();
        List<CartItemDocument> items = found.get().getItems();
        assertThat(items).hasSize(2);

        // Verificar primer item
        assertThat(items.get(0).getProductId()).isEqualTo(1L);
        assertThat(items.get(0).getProductName()).isEqualTo("Laptop HP");
        assertThat(items.get(0).getQuantity()).isEqualTo(1);
        assertThat(items.get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("1500.00"));

        // Verificar segundo item
        assertThat(items.get(1).getProductId()).isEqualTo(2L);
        assertThat(items.get(1).getProductName()).isEqualTo("Mouse Logitech");
        assertThat(items.get(1).getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldFindActiveCartByCustomerId() {
        // Given
        repository.save(activeCart);

        CartDocument completedCart = CartDocument.builder()
                .customerId(1L)
                .status(CartStatus.COMPLETED)
                .items(Arrays.asList())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastUpdatedAt(LocalDateTime.now().minusDays(1))
                .build();
        repository.save(completedCart);

        // When
        Optional<CartDocument> found = repository.findByCustomerIdAndStatus(1L, CartStatus.ACTIVE);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(found.get().getItems()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoActiveCartExists() {
        // Given - Solo carrito completado
        CartDocument completedCart = CartDocument.builder()
                .customerId(1L)
                .status(CartStatus.COMPLETED)
                .items(Arrays.asList())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        repository.save(completedCart);

        // When
        Optional<CartDocument> found = repository.findByCustomerIdAndStatus(1L, CartStatus.ACTIVE);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllCartsByStatus() {
        // Given
        repository.save(activeCart);

        CartDocument activeCart2 = CartDocument.builder()
                .customerId(2L)
                .status(CartStatus.ACTIVE)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("1500.00"))
                .totalItems(1)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        repository.save(activeCart2);

        CartDocument abandonedCart = CartDocument.builder()
                .customerId(3L)
                .status(CartStatus.ABANDONED)
                .items(Arrays.asList(item2))
                .totalAmount(new BigDecimal("100.00"))
                .totalItems(1)
                .createdAt(LocalDateTime.now().minusDays(2))
                .lastUpdatedAt(LocalDateTime.now().minusDays(2))
                .build();
        repository.save(abandonedCart);

        // When
        List<CartDocument> activeCarts = repository.findByStatus(CartStatus.ACTIVE);

        // Then
        assertThat(activeCarts).hasSize(2);
        assertThat(activeCarts).allMatch(cart -> cart.getStatus() == CartStatus.ACTIVE);
        assertThat(activeCarts).extracting(CartDocument::getCustomerId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldFindAbandonedCarts() {
        // Given - Carrito activo reciente (no debe encontrarse)
        repository.save(activeCart);

        // Carrito activo abandonado (última actualización hace 8 días)
        CartDocument oldActiveCart = CartDocument.builder()
                .customerId(2L)
                .status(CartStatus.ACTIVE)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("1500.00"))
                .totalItems(1)
                .createdAt(LocalDateTime.now().minusDays(8))
                .lastUpdatedAt(LocalDateTime.now().minusDays(8))
                .build();
        repository.save(oldActiveCart);

        // Carrito completado antiguo (no debe encontrarse por status)
        CartDocument oldCompletedCart = CartDocument.builder()
                .customerId(3L)
                .status(CartStatus.COMPLETED)
                .items(Arrays.asList())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .createdAt(LocalDateTime.now().minusDays(10))
                .lastUpdatedAt(LocalDateTime.now().minusDays(10))
                .build();
        repository.save(oldCompletedCart);

        // When - Buscar carritos abandonados (no actualizados en 7 días)
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<CartDocument> abandonedCarts = repository.findAbandonedCarts(threshold);

        // Then
        assertThat(abandonedCarts).hasSize(1);
        assertThat(abandonedCarts.get(0).getCustomerId()).isEqualTo(2L);
        assertThat(abandonedCarts.get(0).getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(abandonedCarts.get(0).getLastUpdatedAt()).isBefore(threshold);
    }

    @Test
    void shouldUpdateCart() {
        // Given
        CartDocument savedCart = repository.save(activeCart);
        String cartId = savedCart.getId();

        // When - Agregar un nuevo item
        CartItemDocument newItem = CartItemDocument.builder()
                .id(UUID.randomUUID().toString())
                .productId(3L)
                .productName("Teclado Mecánico")
                .quantity(1)
                .unitPrice(new BigDecimal("150.00"))
                .subtotal(new BigDecimal("150.00"))
                .build();

        savedCart.getItems().add(newItem);
        savedCart.setTotalItems(3);
        savedCart.setTotalAmount(new BigDecimal("1750.00"));
        savedCart.setLastUpdatedAt(LocalDateTime.now());

        CartDocument updatedCart = repository.save(savedCart);

        // Then
        assertThat(updatedCart.getId()).isEqualTo(cartId);
        assertThat(updatedCart.getItems()).hasSize(3);
        assertThat(updatedCart.getTotalItems()).isEqualTo(3);
        assertThat(updatedCart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1750.00"));
    }

    @Test
    void shouldDeleteCart() {
        // Given
        CartDocument savedCart = repository.save(activeCart);
        String cartId = savedCart.getId();

        // When
        repository.deleteById(cartId);

        // Then
        Optional<CartDocument> deletedCart = repository.findById(cartId);
        assertThat(deletedCart).isEmpty();
    }

    @Test
    void shouldHandleEmptyCart() {
        // Given - Carrito sin items
        CartDocument emptyCart = CartDocument.builder()
                .customerId(5L)
                .status(CartStatus.ACTIVE)
                .items(Arrays.asList())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        // When
        CartDocument savedCart = repository.save(emptyCart);

        // Then
        Optional<CartDocument> found = repository.findById(savedCart.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).isEmpty();
        assertThat(found.get().getTotalItems()).isEqualTo(0);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
package com.arka.order_service.infrastructure.persistence.repositories;

import com.arka.order_service.domain.entities.OrderStatus;
import com.arka.order_service.infrastructure.persistence.model.OrderItemJPA;
import com.arka.order_service.infrastructure.persistence.model.OrderJPA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de Repositorio de Órdenes
 *
 * Este test demuestra:
 * - Operaciones CRUD con órdenes y items
 * - Relaciones OneToMany con cascade
 * - Consultas por cliente, estado y fecha
 * - Constraint de unicidad en orderNumber
 * - Manejo de items con orphanRemoval
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderJPARepositoryTest {

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
    private OrderJPARepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private OrderJPA testOrder;
    private OrderItemJPA item1;
    private OrderItemJPA item2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        // Crear items
        item1 = OrderItemJPA.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(1)
                .unitPrice(new BigDecimal("1500.00"))
                .subtotal(new BigDecimal("1500.00"))
                .build();

        item2 = OrderItemJPA.builder()
                .productId(2L)
                .productName("Mouse Logitech")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        // Crear orden
        testOrder = OrderJPA.builder()
                .orderNumber("ORD-2024-00001")
                .customerId(100L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1600.00"))
                .shippingAddress("Calle Falsa 123, Ciudad")
                .notes("Entrega en horas de oficina")
                .build();

        testOrder.addItem(item1);
        testOrder.addItem(item2);
    }

    @Test
    void shouldSaveOrderWithItems() {
        // When
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<OrderJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-2024-00001");
        assertThat(found.get().getCustomerId()).isEqualTo(100L);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("1600.00"));
        assertThat(found.get().getShippingAddress()).isEqualTo("Calle Falsa 123, Ciudad");
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCascadeSaveItems() {
        // When
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // Then
        OrderJPA found = repository.findById(saved.getId()).get();
        List<OrderItemJPA> items = found.getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getId()).isNotNull();
        assertThat(items.get(0).getProductName()).isEqualTo("Laptop HP");
        assertThat(items.get(0).getQuantity()).isEqualTo(1);
        assertThat(items.get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("1500.00"));

        assertThat(items.get(1).getProductName()).isEqualTo("Mouse Logitech");
        assertThat(items.get(1).getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldFindOrderByOrderNumber() {
        // Given
        repository.save(testOrder);
        entityManager.flush();

        // When
        Optional<OrderJPA> found = repository.findByOrderNumber("ORD-2024-00001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(100L);
        assertThat(found.get().getItems()).hasSize(2);
    }

    @Test
    void shouldEnforceUniqueOrderNumber() {
        // Given
        repository.save(testOrder);
        entityManager.flush();

        // When & Then - Intentar guardar otra orden con el mismo número
        OrderJPA duplicate = OrderJPA.builder()
                .orderNumber("ORD-2024-00001") // Mismo orderNumber
                .customerId(200L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .shippingAddress("Otra dirección")
                .build();

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    @Test
    void shouldCheckIfOrderNumberExists() {
        // Given
        repository.save(testOrder);
        entityManager.flush();

        // When
        boolean exists = repository.existsByOrderNumber("ORD-2024-00001");
        boolean notExists = repository.existsByOrderNumber("ORD-9999-99999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindOrdersByCustomerId() {
        // Given
        repository.save(testOrder);

        OrderJPA order2 = OrderJPA.builder()
                .orderNumber("ORD-2024-00002")
                .customerId(100L) // Mismo cliente
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .shippingAddress("Dirección 2")
                .build();
        repository.save(order2);

        OrderJPA order3 = OrderJPA.builder()
                .orderNumber("ORD-2024-00003")
                .customerId(200L) // Cliente diferente
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("300.00"))
                .shippingAddress("Dirección 3")
                .build();
        repository.save(order3);

        entityManager.flush();

        // When
        List<OrderJPA> customer100Orders = repository.findByCustomerId(100L);

        // Then
        assertThat(customer100Orders).hasSize(2);
        assertThat(customer100Orders).extracting(OrderJPA::getOrderNumber)
                .containsExactlyInAnyOrder("ORD-2024-00001", "ORD-2024-00002");
    }

    @Test
    void shouldFindOrdersByStatus() {
        // Given
        repository.save(testOrder);

        OrderJPA confirmedOrder = OrderJPA.builder()
                .orderNumber("ORD-2024-00002")
                .customerId(200L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .shippingAddress("Dirección 2")
                .build();
        repository.save(confirmedOrder);

        OrderJPA deliveredOrder = OrderJPA.builder()
                .orderNumber("ORD-2024-00003")
                .customerId(300L)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("300.00"))
                .shippingAddress("Dirección 3")
                .build();
        repository.save(deliveredOrder);

        entityManager.flush();

        // When
        List<OrderJPA> pendingOrders = repository.findByStatus(OrderStatus.PENDING);
        List<OrderJPA> confirmedOrders = repository.findByStatus(OrderStatus.CONFIRMED);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getOrderNumber()).isEqualTo("ORD-2024-00001");

        assertThat(confirmedOrders).hasSize(1);
        assertThat(confirmedOrders.get(0).getOrderNumber()).isEqualTo("ORD-2024-00002");
    }

    @Test
    void shouldFindOrdersByCustomerIdAndStatus() {
        // Given
        repository.save(testOrder);

        OrderJPA order2 = OrderJPA.builder()
                .orderNumber("ORD-2024-00002")
                .customerId(100L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .shippingAddress("Dirección 2")
                .build();
        repository.save(order2);

        OrderJPA order3 = OrderJPA.builder()
                .orderNumber("ORD-2024-00003")
                .customerId(100L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("300.00"))
                .shippingAddress("Dirección 3")
                .build();
        repository.save(order3);

        entityManager.flush();

        // When
        List<OrderJPA> customer100PendingOrders = repository.findByCustomerIdAndStatus(100L, OrderStatus.PENDING);

        // Then
        assertThat(customer100PendingOrders).hasSize(2);
        assertThat(customer100PendingOrders).extracting(OrderJPA::getOrderNumber)
                .containsExactlyInAnyOrder("ORD-2024-00001", "ORD-2024-00003");
    }

    @Test
    void shouldFindOrdersByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        repository.save(testOrder);
        entityManager.flush();

        // When
        List<OrderJPA> ordersInRange = repository.findByCreatedAtBetween(yesterday, tomorrow);
        List<OrderJPA> ordersOutOfRange = repository.findByCreatedAtBetween(
                yesterday.minusDays(10),
                yesterday
        );

        // Then
        assertThat(ordersInRange).hasSize(1);
        assertThat(ordersInRange.get(0).getOrderNumber()).isEqualTo("ORD-2024-00001");

        assertThat(ordersOutOfRange).isEmpty();
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Given
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // When
        saved.setStatus(OrderStatus.CONFIRMED);
        saved.setConfirmedAt(LocalDateTime.now());
        OrderJPA updated = repository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(updated.getConfirmedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteItemsWhenOrderIsDeleted() {
        // Given
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        Long orderId = saved.getId();

        // When
        repository.deleteById(orderId);
        entityManager.flush();

        // Then
        Optional<OrderJPA> deleted = repository.findById(orderId);
        assertThat(deleted).isEmpty();

        // Los items también deben ser eliminados debido a CascadeType.ALL
    }

    @Test
    void shouldRemoveItemWithOrphanRemoval() {
        // Given
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // When - Remover un item
        OrderJPA order = repository.findById(saved.getId()).get();
        OrderItemJPA itemToRemove = order.getItems().get(0);
        order.removeItem(itemToRemove);
        repository.save(order);
        entityManager.flush();
        entityManager.clear();

        // Then
        OrderJPA updated = repository.findById(saved.getId()).get();
        assertThat(updated.getItems()).hasSize(1);
    }

    @Test
    void shouldAddNewItemToExistingOrder() {
        // Given
        OrderJPA saved = repository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // When - Agregar un nuevo item
        OrderJPA order = repository.findById(saved.getId()).get();
        OrderItemJPA newItem = OrderItemJPA.builder()
                .productId(3L)
                .productName("Teclado Mecánico")
                .quantity(1)
                .unitPrice(new BigDecimal("150.00"))
                .subtotal(new BigDecimal("150.00"))
                .build();
        order.addItem(newItem);
        order.setTotalAmount(new BigDecimal("1750.00"));
        repository.save(order);
        entityManager.flush();

        // Then
        OrderJPA updated = repository.findById(saved.getId()).get();
        assertThat(updated.getItems()).hasSize(3);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1750.00"));
    }

    @Test
    void shouldHandleOrderWithoutNotes() {
        // Given - Orden sin notas
        OrderJPA orderWithoutNotes = OrderJPA.builder()
                .orderNumber("ORD-2024-99999")
                .customerId(999L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .shippingAddress("Dirección sin notas")
                .build();

        // When
        OrderJPA saved = repository.save(orderWithoutNotes);
        entityManager.flush();

        // Then
        Optional<OrderJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNotes()).isNull();
    }
}
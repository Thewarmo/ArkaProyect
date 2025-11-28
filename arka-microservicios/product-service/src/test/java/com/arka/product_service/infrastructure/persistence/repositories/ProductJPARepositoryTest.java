package com.arka.product_service.infrastructure.persistence.repositories;

import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de Repositorio JPA usando Testcontainers
 *
 * Este test demuestra:
 * - Configuración de PostgreSQL con Testcontainers
 * - Uso de @DataJpaTest para tests de repositorio
 * - Operaciones CRUD básicas
 * - Consultas personalizadas
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductJPARepositoryTest {

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
    private ProductJPARepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductJPA testProduct;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        repository.deleteAll();

        // Crear producto de prueba
        testProduct = new ProductJPA();
        testProduct.setName("Laptop HP");
        testProduct.setDescription("Laptop para desarrollo");
        testProduct.setPrice(new BigDecimal("1500.00"));
        testProduct.setStock(10);
        testProduct.setCategoryId(1L);
        testProduct.setBrandId(1L);
    }

    @Test
    void shouldSaveAndFindProductById() {
        // Given
        ProductJPA savedProduct = repository.save(testProduct);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<ProductJPA> found = repository.findById(savedProduct.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop HP");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(found.get().getStock()).isEqualTo(10);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindProductsByNameContaining() {
        // Given
        repository.save(testProduct);

        ProductJPA product2 = new ProductJPA();
        product2.setName("Laptop Dell");
        product2.setDescription("Laptop empresarial");
        product2.setPrice(new BigDecimal("1800.00"));
        product2.setStock(5);
        product2.setCategoryId(1L);
        product2.setBrandId(2L);
        repository.save(product2);

        entityManager.flush();

        // When
        List<ProductJPA> laptops = repository.findByNameContainingIgnoreCase("laptop");

        // Then
        assertThat(laptops).hasSize(2);
        assertThat(laptops).extracting(ProductJPA::getName)
                .containsExactlyInAnyOrder("Laptop HP", "Laptop Dell");
    }

    @Test
    void shouldFindProductsByCategoryId() {
        // Given
        repository.save(testProduct);

        ProductJPA product2 = new ProductJPA();
        product2.setName("Mouse Logitech");
        product2.setDescription("Mouse inalámbrico");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(20);
        product2.setCategoryId(2L); // Categoría diferente
        product2.setBrandId(3L);
        repository.save(product2);

        entityManager.flush();

        // When
        List<ProductJPA> category1Products = repository.findByCategoryId(1L);

        // Then
        assertThat(category1Products).hasSize(1);
        assertThat(category1Products.get(0).getName()).isEqualTo("Laptop HP");
    }

    @Test
    void shouldFindProductsByBrandId() {
        // Given
        repository.save(testProduct);

        ProductJPA product2 = new ProductJPA();
        product2.setName("Desktop HP");
        product2.setDescription("Desktop para oficina");
        product2.setPrice(new BigDecimal("1200.00"));
        product2.setStock(8);
        product2.setCategoryId(1L);
        product2.setBrandId(1L); // Misma marca
        repository.save(product2);

        entityManager.flush();

        // When
        List<ProductJPA> hpProducts = repository.findByBrandId(1L);

        // Then
        assertThat(hpProducts).hasSize(2);
        assertThat(hpProducts).extracting(ProductJPA::getName)
                .containsExactlyInAnyOrder("Laptop HP", "Desktop HP");
    }

    @Test
    void shouldFindProductsWithLowStock() {
        // Given
        testProduct.setStock(5);
        repository.save(testProduct);

        ProductJPA product2 = new ProductJPA();
        product2.setName("Tablet Samsung");
        product2.setDescription("Tablet 10 pulgadas");
        product2.setPrice(new BigDecimal("400.00"));
        product2.setStock(15);
        product2.setCategoryId(3L);
        product2.setBrandId(4L);
        repository.save(product2);

        entityManager.flush();

        // When
        List<ProductJPA> lowStockProducts = repository.findByStockLessThan(10);

        // Then
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getName()).isEqualTo("Laptop HP");
        assertThat(lowStockProducts.get(0).getStock()).isLessThan(10);
    }

    @Test
    void shouldFindAvailableProducts() {
        // Given
        repository.save(testProduct);

        ProductJPA outOfStock = new ProductJPA();
        outOfStock.setName("Producto Agotado");
        outOfStock.setDescription("Sin stock");
        outOfStock.setPrice(new BigDecimal("100.00"));
        outOfStock.setStock(0);
        outOfStock.setCategoryId(1L);
        outOfStock.setBrandId(1L);
        repository.save(outOfStock);

        entityManager.flush();

        // When
        List<ProductJPA> availableProducts = repository.findAvailableProducts();

        // Then
        assertThat(availableProducts).hasSize(1);
        assertThat(availableProducts.get(0).getName()).isEqualTo("Laptop HP");
        assertThat(availableProducts.get(0).getStock()).isGreaterThan(0);
    }

    @Test
    void shouldCheckIfProductExistsByName() {
        // Given
        repository.save(testProduct);
        entityManager.flush();

        // When
        boolean exists = repository.existsByNameIgnoreCase("laptop hp");
        boolean notExists = repository.existsByNameIgnoreCase("iPhone");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldUpdateProduct() {
        // Given
        ProductJPA savedProduct = repository.save(testProduct);
        entityManager.flush();
        entityManager.clear();

        // When
        savedProduct.setStock(5);
        savedProduct.setPrice(new BigDecimal("1400.00"));
        ProductJPA updatedProduct = repository.save(savedProduct);
        entityManager.flush();

        // Then
        assertThat(updatedProduct.getStock()).isEqualTo(5);
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("1400.00"));
        assertThat(updatedProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        ProductJPA savedProduct = repository.save(testProduct);
        entityManager.flush();
        Long productId = savedProduct.getId();

        // When
        repository.deleteById(productId);
        entityManager.flush();

        // Then
        Optional<ProductJPA> deletedProduct = repository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }
}
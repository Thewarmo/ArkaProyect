# Guía de Testing - Microservicios Arka

## Resumen Ejecutivo

Esta guía documenta la estrategia de testing implementada para los 11 microservicios del proyecto Arka, utilizando JUnit 5, Mockito y Testcontainers.

### Objetivos
- **Cobertura mínima**: 70%
- **Enfoque**: Tests de Repositories y Controllers (críticos)
- **Total estimado**: 150-200 tests
- **Integración CI/CD**: Tests automáticos en cada push

---

## Configuración de Gradle

### Servicios ya configurados:
✅ product-service
✅ customer-service

### Servicios pendientes:
- inventory-service
- order-service
- cart-service
- notification-service
- report-service
- auth-server
- api-gateway
- config-server
- eureka-server

### Pasos para configurar cada servicio:

#### 1. Agregar plugin JaCoCo

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.5'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'jacoco'  // AGREGAR ESTA LÍNEA
}
```

#### 2. Agregar versión de Testcontainers

```gradle
ext {
    set('springCloudVersion', "2025.0.0")
    set('testcontainersVersion', "1.19.8")  // AGREGAR
}
```

#### 3. Agregar dependencias de testing

**Para servicios con PostgreSQL** (product, customer, inventory, order, notification, report, auth):
```gradle
dependencies {
    // ... dependencias existentes ...

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation "org.testcontainers:testcontainers:${testcontainersVersion}"
    testImplementation "org.testcontainers:junit-jupiter:${testcontainersVersion}"
    testImplementation "org.testcontainers:postgresql:${testcontainersVersion}"
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

**Para cart-service (MongoDB)**:
```gradle
dependencies {
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation "org.testcontainers:testcontainers:${testcontainersVersion}"
    testImplementation "org.testcontainers:junit-jupiter:${testcontainersVersion}"
    testImplementation "org.testcontainers:mongodb:${testcontainersVersion}"  // MongoDB en lugar de PostgreSQL
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

**Para order-service y notification-service** (agregar también RabbitMQ):
```gradle
dependencies {
    // ... PostgreSQL dependencies ...
    testImplementation "org.testcontainers:rabbitmq:${testcontainersVersion}"  // AGREGAR
}
```

#### 4. Configurar task de test

```gradle
tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport

    // Ejecución paralela para tests más rápidos
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1

    // Logging de tests
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = false
    }
}
```

#### 5. Configurar JaCoCo

```gradle
// Configuración JaCoCo
jacoco {
    toolVersion = "0.8.12"
}

jacocoTestReport {
    dependsOn test

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/*Application.class',
                '**/*Config*.class',
                '**/dto/**',
                '**/model/**',
                '**/infrastructure/persistence/model/**',
                '**/mapper/**'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.70 // 70% cobertura mínima
            }
        }
    }
}

check.dependsOn jacocoTestCoverageVerification
```

---

## Estructura de Directorios de Tests

```
src/test/java/com/arka/{service}/
├── infrastructure/
│   ├── persistence/
│   │   └── {Entity}RepositoryTest.java
│   └── controllers/
│       └── {Controller}Test.java
└── {Service}ApplicationTests.java (ya existe - context load test)
```

---

## Ejemplos de Tests

### 1. Repository Test con Testcontainers (PostgreSQL)

**Archivo**: `ProductRepositoryTest.java`

```java
package com.arka.product_service.infrastructure.persistence;

import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

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
    }

    @Autowired
    private ProductJPARepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFindProductById() {
        // Given
        ProductJPA product = new ProductJPA();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setStock(100);
        product.setCategoryId(1L);

        // When
        ProductJPA saved = repository.save(product);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ProductJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void shouldFindProductsByName() {
        // Given
        ProductJPA product1 = createProduct("Gaming Mouse", BigDecimal.valueOf(49.99));
        ProductJPA product2 = createProduct("Gaming Keyboard", BigDecimal.valueOf(89.99));
        repository.save(product1);
        repository.save(product2);
        entityManager.flush();

        // When
        var results = repository.findByNameContainingIgnoreCase("gaming");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name")
                .containsExactlyInAnyOrder("Gaming Mouse", "Gaming Keyboard");
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        ProductJPA product = createProduct("To Delete", BigDecimal.TEN);
        ProductJPA saved = repository.save(product);
        entityManager.flush();

        // When
        repository.deleteById(saved.getId());
        entityManager.flush();

        // Then
        Optional<ProductJPA> found = repository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    private ProductJPA createProduct(String name, BigDecimal price) {
        ProductJPA product = new ProductJPA();
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setPrice(price);
        product.setStock(50);
        product.setCategoryId(1L);
        return product;
    }
}
```

### 2. Controller Test con @WebMvcTest

**Archivo**: `ProductControllerTest.java`

```java
package com.arka.product_service.infrastructure.controllers;

import com.arka.product_service.application.dto.ProductRequest;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.usecases.CreateProductUseCase;
import com.arka.product_service.application.usecases.GetProductUseCase;
import com.arka.product_service.application.usecases.GetAllProductsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductUseCase createProductUseCase;

    @MockBean
    private GetProductUseCase getProductUseCase;

    @MockBean
    private GetAllProductsUseCase getAllProductsUseCase;

    @Test
    void shouldCreateProduct() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setDescription("Product Description");
        request.setPrice(BigDecimal.valueOf(99.99));
        request.setStock(100);
        request.setCategoryId(1L);

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("New Product");
        response.setPrice(BigDecimal.valueOf(99.99));

        when(createProductUseCase.execute(any(ProductRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void shouldGetProductById() throws Exception {
        // Given
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Test Product");
        response.setPrice(BigDecimal.valueOf(49.99));

        when(getProductUseCase.execute(eq(1L))).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        // Given
        when(getProductUseCase.execute(eq(999L))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        ProductResponse product1 = createProductResponse(1L, "Product 1", BigDecimal.TEN);
        ProductResponse product2 = createProductResponse(2L, "Product 2", BigDecimal.valueOf(20));

        when(getAllProductsUseCase.execute()).thenReturn(Arrays.asList(product1, product2));

        // When & Then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidData() throws Exception {
        // Given
        ProductRequest invalidRequest = new ProductRequest();
        // name is null - should trigger validation error

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private ProductResponse createProductResponse(Long id, String name, BigDecimal price) {
        ProductResponse response = new ProductResponse();
        response.setId(id);
        response.setName(name);
        response.setPrice(price);
        return response;
    }
}
```

### 3. MongoDB Repository Test (Cart Service)

**Archivo**: `CartMongoRepositoryTest.java`

```java
package com.arka.cart_service.infrastructure.persistence;

import com.arka.cart_service.infrastructure.persistence.model.CartDocument;
import com.arka.cart_service.infrastructure.persistence.model.CartItemDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class CartMongoRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CartMongoRepository repository;

    @Test
    void shouldSaveAndFindCartByCustomerId() {
        // Given
        CartDocument cart = createCart(1L);

        // When
        CartDocument saved = repository.save(cart);

        // Then
        Optional<CartDocument> found = repository.findByCustomerId(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(1L);
        assertThat(found.get().getItems()).hasSize(2);
    }

    @Test
    void shouldFindAbandonedCarts() {
        // Given
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        CartDocument recentCart = createCart(1L);
        recentCart.setUpdatedAt(LocalDateTime.now());

        CartDocument abandonedCart = createCart(2L);
        abandonedCart.setUpdatedAt(LocalDateTime.now().minusDays(5));

        repository.save(recentCart);
        repository.save(abandonedCart);

        // When
        List<CartDocument> abandoned = repository.findByUpdatedAtBefore(threshold);

        // Then
        assertThat(abandoned).hasSize(1);
        assertThat(abandoned.get(0).getCustomerId()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteCartByCustomerId() {
        // Given
        CartDocument cart = createCart(1L);
        repository.save(cart);

        // When
        repository.deleteByCustomerId(1L);

        // Then
        Optional<CartDocument> found = repository.findByCustomerId(1L);
        assertThat(found).isEmpty();
    }

    private CartDocument createCart(Long customerId) {
        CartDocument cart = new CartDocument();
        cart.setCustomerId(customerId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        List<CartItemDocument> items = new ArrayList<>();
        items.add(createCartItem(1L, "Product 1", 2, 29.99));
        items.add(createCartItem(2L, "Product 2", 1, 49.99));
        cart.setItems(items);

        return cart;
    }

    private CartItemDocument createCartItem(Long productId, String productName, Integer quantity, Double price) {
        CartItemDocument item = new CartItemDocument();
        item.setProductId(productId);
        item.setProductName(productName);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }
}
```

### 4. RabbitMQ Integration Test (Order → Notification)

**Archivo**: `OrderEventPublisherTest.java` (Order Service)

```java
package com.arka.order_service.infrastructure.messaging;

import com.arka.order_service.domain.entities.Order;
import com.arka.order_service.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
@Testcontainers
class OrderEventPublisherTest {

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureRabbitMq(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getFirstMappedPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private OrderEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishOrderCreatedEvent() {
        // Given
        Order order = createTestOrder();

        // When
        publisher.publishOrderCreated(order);

        // Then - verificar que el mensaje fue publicado
        await().atMost(5, SECONDS).untilAsserted(() -> {
            // Aquí verificarías que el mensaje llegó a la cola
            // En un test real, necesitarías un consumer de prueba
        });
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        return order;
    }
}
```

---

## Comandos Útiles

### Ejecutar tests
```bash
# Ejecutar todos los tests de un servicio
./gradlew test

# Ver reporte de cobertura
./gradlew jacocoTestReport
# Abre: build/reports/jacoco/test/html/index.html

# Verificar cobertura (falla si < 70%)
./gradlew jacocoTestCoverageVerification

# Ejecutar tests + verificar cobertura
./gradlew check
```

### Ejecutar tests de todos los servicios (desde raíz)
```bash
# Desde arka-microservicios/
find . -name "build.gradle" -exec sh -c 'cd $(dirname {}); ./gradlew test' \;
```

---

## Checklist de Implementación

### Configuración (Todos los servicios)
- [ ] product-service ✅
- [ ] customer-service ✅
- [ ] inventory-service
- [ ] order-service
- [ ] cart-service
- [ ] notification-service
- [ ] report-service
- [ ] auth-server
- [ ] api-gateway
- [ ] config-server
- [ ] eureka-server

### Tests Críticos (Product Service - Ejemplo)
- [ ] ProductRepositoryTest (5 tests)
- [ ] ProductControllerTest (10 tests)

### Tests Críticos (Inventory Service)
- [ ] InventoryRepositoryTest con tests de concurrencia
- [ ] InventoryControllerTest

### Tests Críticos (Order Service)
- [ ] OrderRepositoryTest
- [ ] OrderControllerTest
- [ ] OrderEventPublisherTest

### Tests Críticos (Cart Service)
- [ ] CartMongoRepositoryTest
- [ ] CartControllerTest

---

## Próximos Pasos

1. ✅ Configurar build.gradle de product-service y customer-service
2. ⏳ Configurar build.gradle de servicios restantes
3. ⏳ Crear tests ejemplo para product-service
4. ⏳ Implementar tests críticos (inventory, order, cart)
5. ⏳ Configurar CI/CD workflow
6. ⏳ Alcanzar 70% de cobertura global

---

**Última actualización**: 2025-01-07
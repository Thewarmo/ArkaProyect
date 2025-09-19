# 📚 GUÍA COMPLETA - Proyecto Arka Microservicios

**Guía paso a paso para replicar completamente el proyecto de microservicios Arka**

---

## 📋 Índice
1. [Preparación del Entorno](#preparación-del-entorno)
2. [Estructura de Directorios](#estructura-de-directorios)
3. [Config Server](#config-server)
4. [Product Service](#product-service)
5. [API Gateway](#api-gateway)
6. [Configuraciones](#configuraciones)
7. [Docker Services](#docker-services)
8. [Pruebas](#pruebas)

---

## 🔧 Preparación del Entorno

### Requisitos Previos
```bash
- Java 21 JDK
- Docker Desktop
- Git
- IDE (IntelliJ IDEA recomendado)
- Postman/curl para pruebas
```

### Crear Estructura Base
```bash
mkdir ProyectoArkaAceleraTi
cd ProyectoArkaAceleraTi
mkdir arka-microservicios
mkdir arka-config-repo
cd arka-config-repo
git init
```

---

## 📁 Estructura de Directorios

```
ProyectoArkaAceleraTi/
├── arka-microservicios/
│   ├── config-server/
│   ├── api-gateway/
│   ├── product-service/
│   └── docker-compose.yml
└── arka-config-repo/
    ├── application.yml
    ├── product-service.yml
    └── api-gateway.yml
```

---

## ⚙️ Config Server

### 1. Crear Proyecto Config Server

**Via Spring Initializr (https://start.spring.io):**
- Project: Gradle - Groovy
- Language: Java
- Spring Boot: 3.5.5
- Group: com.arka
- Artifact: config-server
- Name: config-server
- Description: Config Server para microservicios Arka
- Package name: com.arka.config_server
- Packaging: Jar
- Java: 21

**Dependencias:**
- Config Server

### 2. Configurar ConfigServerApplication.java

```java
package com.arka.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### 3. application.properties del Config Server

```properties
spring.application.name=config-server
server.port=8889

# Repositorio Git local
spring.cloud.config.server.git.uri=file:///[RUTA_ABSOLUTA]/arka-config-repo
spring.cloud.config.server.git.clone-on-start=true

# Actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

**⚠️ Importante:** Cambiar `[RUTA_ABSOLUTA]` por la ruta completa a tu directorio `arka-config-repo`.

---

## 🏭 Product Service

### 1. Crear Proyecto Product Service

**Via Spring Initializr:**
- Project: Gradle - Groovy
- Language: Java
- Spring Boot: 3.5.5
- Group: com.arka
- Artifact: product-service
- Name: product-service
- Description: Product Service para Arka
- Package name: com.arka.product_service
- Packaging: Jar
- Java: 21

**Dependencias:**
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Config Client
- Spring Boot Actuator
- Lombok
- Validation

### 2. Estructura Clean Architecture

**Crear directorios:**
```
src/main/java/com/arka/product_service/
├── domain/
│   ├── entities/
│   ├── repositories/
│   └── services/
├── application/
│   ├── usecases/
│   ├── dto/
│   └── ports/
├── infrastructure/
│   └── persistence/
│       ├── models/
│       ├── repositories/
│       └── mappers/
└── interfaces/
    └── controllers/
```

### 3. Entidades de Dominio

**Product.java (domain/entities/)**
```java
package com.arka.product_service.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Long brandId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor vacío
    public Product() {}

    // Constructor completo
    public Product(Long id, String name, String description, BigDecimal price, 
                   Integer stock, Long categoryId, Long brandId, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    // ... (todos los getters y setters)
}
```

### 4. Repository Interface (Domain)

**ProductRepository.java (domain/repositories/)**
```java
package com.arka.product_service.domain.repositories;

import com.arka.product_service.domain.entities.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByStockLessThan(Integer threshold);
    boolean existsById(Long id);
}
```

### 5. Domain Services

**ProductDomainServices.java (domain/services/)**
```java
package com.arka.product_service.domain.services;

import com.arka.product_service.domain.entities.Product;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class ProductDomainServices {

    public void validateProduct(Product product) {
        validateName(product.getName());
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateCategoryId(product.getCategoryId());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("El nombre del producto no puede exceder 100 caracteres");
        }
        if (name.length() < 3) {
            throw new IllegalArgumentException("El nombre del producto debe tener al menos 3 caracteres");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
    }

    private void validateStock(Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("El stock es obligatorio");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    private void validateCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }
}
```

### 6. DTOs de Aplicación

**CreateProductRequest.java (application/dto/)**
```java
package com.arka.product_service.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private Long brandId;
}
```

**ProductResponse.java (application/dto/)**
```java
package com.arka.product_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Long brandId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 7. Use Cases

**CreateProductUseCase.java (application/usecases/)**
```java
package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.CreateProductRequest;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import com.arka.product_service.domain.services.ProductDomainServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductDomainServices productDomainServices;
    private final ProductMapper productMapper;

    public ProductResponse execute(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        
        productDomainServices.validateProduct(product);
        
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
}
```

### 8. Mapper

**ProductMapper.java (application/ports/)**
```java
package com.arka.product_service.application.ports;

import com.arka.product_service.application.dto.CreateProductRequest;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.domain.entities.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setCategoryId(product.getCategoryId());
        response.setBrandId(product.getBrandId());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
```

### 9. Entidad JPA

**ProductJPA.java (infrastructure/persistence/models/)**
```java
package com.arka.product_service.infrastructure.persistence.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class ProductJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 10. Repository JPA

**ProductJPARepository.java (infrastructure/persistence/repositories/)**
```java
package com.arka.product_service.infrastructure.persistence.repositories;

import com.arka.product_service.infrastructure.persistence.models.ProductJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJPARepository extends JpaRepository<ProductJPA, Long> {
    List<ProductJPA> findByStockLessThan(Integer threshold);
}
```

### 11. Repository Implementation

**ProductRepositoryImpl.java (infrastructure/persistence/repositories/)**
```java
package com.arka.product_service.infrastructure.persistence.repositories;

import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import com.arka.product_service.infrastructure.persistence.mappers.ProductEntityMapper;
import com.arka.product_service.infrastructure.persistence.models.ProductJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJPARepository productJPARepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public Product save(Product product) {
        ProductJPA productJPA = productEntityMapper.toJPA(product);
        ProductJPA savedProductJPA = productJPARepository.save(productJPA);
        return productEntityMapper.toDomain(savedProductJPA);
    }

    @Override
    public Optional<Product> findById(Long id) {
        Optional<ProductJPA> productJPA = productJPARepository.findById(id);
        return productJPA.map(productEntityMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        List<ProductJPA> productsJPA = productJPARepository.findAll();
        return productsJPA.stream()
                .map(productEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByStockLessThan(Integer threshold) {
        List<ProductJPA> productsJPA = productJPARepository.findByStockLessThan(threshold);
        return productsJPA.stream()
                .map(productEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return productJPARepository.existsById(id);
    }
}
```

### 12. Entity Mapper

**ProductEntityMapper.java (infrastructure/persistence/mappers/)**
```java
package com.arka.product_service.infrastructure.persistence.mappers;

import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.infrastructure.persistence.models.ProductJPA;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

    public ProductJPA toJPA(Product product) {
        ProductJPA productJPA = new ProductJPA();
        productJPA.setId(product.getId());
        productJPA.setName(product.getName());
        productJPA.setDescription(product.getDescription());
        productJPA.setPrice(product.getPrice());
        productJPA.setStock(product.getStock());
        productJPA.setCategoryId(product.getCategoryId());
        productJPA.setBrandId(product.getBrandId());
        productJPA.setCreatedAt(product.getCreatedAt());
        productJPA.setUpdatedAt(product.getUpdatedAt());
        return productJPA;
    }

    public Product toDomain(ProductJPA productJPA) {
        return new Product(
            productJPA.getId(),
            productJPA.getName(),
            productJPA.getDescription(),
            productJPA.getPrice(),
            productJPA.getStock(),
            productJPA.getCategoryId(),
            productJPA.getBrandId(),
            productJPA.getCreatedAt(),
            productJPA.getUpdatedAt()
        );
    }
}
```

### 13. Controller

**ProductController.java (interfaces/controllers/)**
```java
package com.arka.product_service.interfaces.controllers;

import com.arka.product_service.application.dto.CreateProductRequest;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.usecases.CreateProductUseCase;
import com.arka.product_service.application.usecases.GetAllProductsUseCase;
import com.arka.product_service.application.usecases.GetProductByIdUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = createProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = getAllProductsUseCase.execute();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<ProductResponse> product = getProductByIdUseCase.execute(id);
        return product
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 14. Global Exception Handler

**GlobalExceptionHandler.java (interfaces/controllers/)**
```java
package com.arka.product_service.interfaces.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> validationErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            Map<String, String> fieldError = new HashMap<>();
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldError.put("field", fieldName);
            fieldError.put("message", errorMessage);
            validationErrors.add(fieldError);
        });

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", "Error de validación en los datos de entrada");
        response.put("validationErrors", validationErrors);
        response.put("path", "/api/v1/products");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("path", "/api/v1/products");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "Ha ocurrido un error interno. Por favor contacte al administrador.");
        response.put("path", "/api/v1/products");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### 15. application.properties Product Service

```properties
spring.application.name=product-service
spring.config.import=configserver:http://localhost:8889
management.endpoints.web.exposure.include=*
```

---

## 🌐 API Gateway

### 1. Crear Proyecto API Gateway

**Via Spring Initializr:**
- Project: Gradle - Groovy
- Language: Java
- Spring Boot: 3.5.5
- Group: com.arka
- Artifact: api-gateway
- Name: api-gateway
- Description: API Gateway para microservicios Arka
- Package name: com.arka.api_gateway
- Packaging: Jar
- Java: 21

**Dependencias:**
- Gateway (spring-cloud-starter-gateway)
- Config Client (spring-cloud-starter-config)
- Actuator (spring-boot-starter-actuator)

### 2. Verificar build.gradle

**Asegúrate de que la dependencia sea correcta:**
```gradle
implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
```

**NO debe ser:**
```gradle
implementation 'org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc'
```

### 3. ApiGatewayApplication.java

```java
package com.arka.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### 4. application.properties API Gateway

```properties
spring.application.name=api-gateway
spring.config.import=configserver:http://localhost:8889
management.endpoints.web.exposure.include=*
```

---

## ⚙️ Configuraciones

### 1. application.yml (Config Global)

**Archivo:** `arka-config-repo/application.yml`

```yaml
# Configuración compartida por todos los microservicios
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    properties:
      hibernate:
        format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

logging:
  level:
    org.springframework.cloud: DEBUG
```

### 2. product-service.yml

**Archivo:** `arka-config-repo/product-service.yml`

```yaml
# Configuración específica del Product Service
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/arka_products
    username: arka_user
    password: arka_password

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

eureka:
  client:
    enabled: false

logging:
  level:
    com.arka.product_service: DEBUG
```

### 3. api-gateway.yml

**Archivo:** `arka-config-repo/api-gateway.yml`

```yaml
# Configuración del API Gateway
server:
  port: 8090

spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: product-service
              uri: http://localhost:8081
              predicates:
                - Path=/api/v1/products/**
              filters:
                - StripPrefix=0

          globalcors:
            cors-configurations:
              '[/**]':
                allowedOrigins: "*"
                allowedMethods: "*"
                allowedHeaders: "*"

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### 4. Commits en Config Repo

```bash
cd arka-config-repo
git add .
git commit -m "Initial configuration for all services"
```

---

## 🐳 Docker Services

### docker-compose.yml

**Archivo:** `arka-microservicios/docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: arka-postgres
    environment:
      POSTGRES_DB: arka_products
      POSTGRES_USER: arka_user
      POSTGRES_PASSWORD: arka_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:6
    container_name: arka-mongodb
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  adminer:
    image: adminer
    container_name: arka-adminer
    restart: always
    ports:
      - "8080:8080"

volumes:
  postgres_data:
  mongodb_data:
```

### Iniciar Docker Services

```bash
cd arka-microservicios
docker-compose up -d
```

---

## 🧪 Pruebas

### 1. Verificar Servicios

```bash
# Config Server
curl http://localhost:8889/actuator/health

# API Gateway
curl http://localhost:8090/actuator/health

# Product Service (directo)
curl http://localhost:8081/actuator/health
```

### 2. Probar API a través del Gateway

**Crear Producto:**
```bash
curl -X POST http://localhost:8090/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Gaming",
    "description": "Mouse para gaming RGB",
    "price": 89.99,
    "stock": 50,
    "categoryId": 1,
    "brandId": 1
  }'
```

**Listar Productos:**
```bash
curl http://localhost:8090/api/v1/products
```

**Obtener Producto por ID:**
```bash
curl http://localhost:8090/api/v1/products/1
```

---

## 🚀 Orden de Ejecución

### Secuencia de Inicio

1. **Docker Services:**
   ```bash
   cd arka-microservicios
   docker-compose up -d
   ```

2. **Config Server:**
   ```bash
   cd config-server
   ./gradlew bootRun
   ```
   
3. **Product Service:**
   ```bash
   cd product-service
   ./gradlew bootRun
   ```

4. **API Gateway:**
   ```bash
   cd api-gateway
   ./gradlew bootRun
   ```

### Verificar Todo Funciona

```bash
# Test completo
curl -X POST http://localhost:8090/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","description":"Test Description","price":99.99,"stock":10,"categoryId":1,"brandId":1}'

curl http://localhost:8090/api/v1/products
```

---

## 🛠️ Resolución de Problemas Comunes

### 1. Puerto 8080 Ocupado
**Problema:** API Gateway no puede iniciar en puerto 8080
**Solución:** Cambiar puerto a 8090 en `api-gateway.yml`

### 2. Errores de Eureka
**Problema:** Product Service intenta conectar a Eureka
**Solución:** Deshabilitar Eureka en `product-service.yml`

### 3. Config Server No Encuentra Repo
**Problema:** Config Server no puede leer configuraciones
**Solución:** Verificar ruta absoluta en application.properties

### 4. Base de Datos No Conecta
**Problema:** Product Service no conecta a PostgreSQL
**Solución:** Verificar que Docker esté corriendo y configuración de BD

### 5. Errores de Configuración Gateway
**Problema:** Advertencias sobre claves obsoletas
**Solución:** Usar configuración actualizada para Spring Boot 3.x

---

## 📚 Conceptos Aprendidos

### Arquitectura de Microservicios
- Separación de responsabilidades
- Comunicación entre servicios
- Configuración centralizada

### Clean Architecture
- Independencia de frameworks
- Inversión de dependencias
- Separación por capas

### Spring Cloud
- Config Server
- API Gateway
- Service Discovery (preparado)

### Patrones Implementados
- Repository Pattern
- Adapter Pattern
- Use Case Pattern
- DTO Pattern

---

## 🎯 Próximos Pasos Sugeridos

1. **Service Discovery (Eureka)**
2. **Authorization Server (JWT)**
3. **Order Service (siguiente microservicio)**
4. **Monitoring y Observabilidad**
5. **Testing Automatizado**

---

*Esta guía te permite replicar completamente el estado actual del proyecto Arka Microservicios. Cada paso ha sido probado y validado.*
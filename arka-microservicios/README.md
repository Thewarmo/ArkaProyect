# Proyecto Arka - Microservicios con Spring Boot

## 📋 Descripción del Proyecto

Sistema de microservicios para **Arka**, empresa distribuidora de accesorios para PC que busca automatizar:
- Gestión de inventario y abastecimiento
- Procesos de venta y órdenes de compra
- Reportes de ventas y análisis
- Notificaciones y carritos abandonados

## 🏗️ Arquitectura Implementada

### Microservicios Desarrollados
1. **Config Server** (Puerto 8889) - Configuración centralizada ✅ COMPLETO
2. **API Gateway** (Puerto 8090) - Punto de entrada único ✅ COMPLETO
3. **Product Service** (Puerto 8081) - Gestión de productos ✅ COMPLETO

### Tecnologías Utilizadas
- **Java 21** con **Spring Boot 3.5.5**
- **Spring Cloud Config** para configuración centralizada
- **Spring Cloud Gateway** para API Gateway reactivo
- **Spring WebFlux** para programación reactiva
- **PostgreSQL** para datos relacionales
- **MongoDB** para datos NoSQL (preparado)
- **Docker Compose** para servicios de BD
- **Gradle** como build tool
- **MapStruct** para mapeo de objetos
- **Lombok** para reducir boilerplate
- **Arquitectura Limpia (Clean Architecture)**

## 🚀 Estructura del Proyecto

```
arka-microservicios/
├── config-server/           # Servidor de configuración ✅
├── api-gateway/            # API Gateway - Punto de entrada ✅
├── product-service/         # Microservicio de productos ✅
├── auth-server/            # (Pendiente)
├── order-service/          # (Pendiente)
├── inventory-service/      # (Pendiente)
├── customer-service/       # (Pendiente)
├── cart-service/          # (Pendiente)
├── notification-service/   # (Pendiente)
├── report-service/        # (Pendiente)
├── supplier-service/      # (Pendiente)
└── docker-compose.yml     # Servicios de base de datos ✅
```

## 🏛️ Arquitectura Limpia - Product Service

```
📦 com.arka.product_service
├── 🏛️ domain/
│   ├── entities/          # Product, Category, Brand (POJOs puros)
│   ├── repositories/      # ProductRepository (interface)
│   └── services/          # ProductDomainServices (lógica de negocio)
├── 🚪 application/
│   ├── usecases/          # CreateProductUseCase (casos de uso)
│   ├── dto/              # CreateProductRequest, ProductResponse
│   └── ports/            # ProductMapper (interfaces)
├── 🔌 infrastructure/
│   └── persistence/       
│       ├── models/        # ProductJPA (entidades JPA)
│       ├── repositories/  # ProductRepositoryImpl, ProductJPARepository
│       └── mappers/       # ProductEntityMapper
└── 🎯 interfaces/
    └── controllers/       # ProductController (REST API)
```

## 🐳 Servicios de Base de Datos

### Docker Compose
```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: arka-postgres
    ports: ["5432:5432"]
    environment:
      POSTGRES_DB: arka_products
      POSTGRES_USER: arka_user
      POSTGRES_PASSWORD: arka_password

  mongodb:
    image: mongo:6
    container_name: arka-mongodb
    ports: ["27017:27017"]

  adminer:
    image: adminer
    container_name: arka-adminer
    ports: ["8080:8080"]
```

## ⚙️ Configuración

### Config Server (Puerto 8889)
- **Repositorio:** `file:///C:/Users/Andres/Documents/ProyectoArkaAceleraTi/arka-config-repo`
- **Configuraciones centralizadas** para todos los microservicios
- **Git como fuente de verdad** para configuraciones

### Configuraciones Principales
- `application.yml` - Configuración compartida
- `product-service.yml` - Configuración específica del Product Service
- `api-gateway.yml` - Configuración del API Gateway (rutas y CORS)

## 🚀 Cómo Ejecutar

### 1. Iniciar Servicios de Base de Datos
```bash
cd arka-microservicios
docker-compose up -d
```

### 2. Iniciar Config Server
```bash
cd config-server
./gradlew bootRun
```

### 3. Iniciar Product Service
```bash
cd product-service
./gradlew bootRun
```

### 4. Iniciar API Gateway
```bash
cd api-gateway
./gradlew bootRun
```

## 🧪 Testing de la API

### 🌐 Acceso a través del API Gateway (RECOMENDADO)

**URL Base:** `http://localhost:8090`

#### 1. **POST** `http://localhost:8090/api/v1/products` - Crear producto
```json
{
  "name": "Mouse Gaming",
  "description": "Mouse para gaming RGB",
  "price": 89.99,
  "stock": 50,
  "categoryId": 1,
  "brandId": 1
}
```

#### 2. **GET** `http://localhost:8090/api/v1/products` - Listar todos los productos

#### 3. **GET** `http://localhost:8090/api/v1/products/{id}` - Obtener producto por ID

#### 4. **PUT** `http://localhost:8090/api/v1/products/{id}/stock` - Actualizar stock (HU2)
```json
{
  "quantity": 100,
  "operation": "ADD"
}
```

#### 5. **GET** `http://localhost:8090/api/v1/products/low-stock?threshold=10` - Productos con bajo stock (HU3)

#### 6. **GET** `http://localhost:8090/api/v1/products/paginated` - Listado paginado con filtros
- Query params: `page`, `size`, `name`, `categoryId`, `brandId`

### 🔗 Acceso Directo (Solo Desarrollo)
- Los endpoints también están disponibles directamente en `http://localhost:8081`
- **⚠️ En producción, usar siempre el API Gateway (puerto 8090)**

### Response Example:
```json
{
  "id": 1,
  "name": "Mouse Gaming",
  "description": "Mouse para gaming RGB",
  "price": 89.99,
  "stock": 50,
  "categoryId": 1,
  "brandId": 1,
  "createdAt": "2025-08-29T11:30:00",
  "updatedAt": "2025-08-29T11:30:00"
}
```

## 🌐 Flujo de Arquitectura

```
Cliente HTTP → API Gateway (8090) → Product Service (8081) → PostgreSQL
                     ↓
               Config Server (8889) ← Git Config Repo
```

### Características del API Gateway:
- **Punto de entrada único** para todos los microservicios
- **Enrutamiento automático** basado en paths
- **CORS global** configurado para aplicaciones web
- **Reactivo** con Spring WebFlux para alta concurrencia
- **Configuración centralizada** desde Config Server

## 🔒 Principios Aplicados

### SOLID
- **Single Responsibility:** Cada clase tiene una responsabilidad específica
- **Open/Closed:** Abierto para extensión, cerrado para modificación
- **Dependency Inversion:** Dependencias de abstracciones, no implementaciones

### Clean Architecture
- **Independencia de frameworks** en el dominio
- **Separación de responsabilidades** por capas
- **Inversión de dependencias** hacia el dominio

### Microservicios
- **API Gateway Pattern:** Punto de entrada único
- **Configuration Management:** Config Server centralizado
- **Database per Service:** PostgreSQL para productos

### Patrones Implementados
- **Repository Pattern:** Abstracción de persistencia
- **Adapter Pattern:** Implementaciones de repositorios
- **Mapper Pattern:** Conversión entre objetos con MapStruct
- **Use Case Pattern:** Casos de uso específicos
- **Gateway Pattern:** Enrutamiento y proxy reverso

## 📚 Validaciones Implementadas

### Validaciones de Dominio (ProductDomainServices)
- Nombre obligatorio (3-100 caracteres)
- Precio mayor a 0
- Stock no negativo
- Categoría obligatoria

### Validaciones de DTO (Bean Validation)
- `@NotBlank`, `@Size`, `@NotNull`, `@DecimalMin`, `@Min`

## 🗄️ Base de Datos

### Tabla Products
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  price DECIMAL(10,2) NOT NULL,
  stock INTEGER NOT NULL,
  category_id BIGINT NOT NULL,
  brand_id BIGINT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

## 🚧 Próximos Pasos

### Microservicios Pendientes
1. **Service Discovery** (Eureka Server) - Registro automático de servicios
2. **Authorization Server** (Spring Security + JWT) - Autenticación y seguridad
3. **Order Service** - Gestión de pedidos (HU4)
4. **Inventory Service** - Control avanzado de inventario
5. **Customer Service** - Gestión de clientes
6. **Cart Service** (MongoDB) - Carritos de compra
7. **Notification Service** (MongoDB) - Notificaciones y emails
8. **Report Service** - Reportes y analytics (HU5, HU8)
9. **Supplier Service** - Gestión de proveedores

### Funcionalidades por Implementar
- **Service Discovery (Eureka)** - Para balanceador automático de carga
- **Circuit Breaker (Resilience4j)** - Tolerancia a fallos
- **Distributed Tracing (Sleuth/Zipkin)** - Trazabilidad de requests
- **API Documentation (OpenAPI/Swagger)** - Documentación automática
- **Testing (JUnit 5, TestContainers)** - Pruebas automatizadas
- **Monitoring (Actuator, Micrometer)** - Métricas y observabilidad

## 🎯 Historias de Usuario Implementadas

### ✅ HU1 - Registrar productos en el sistema
- ✅ Endpoint POST /api/v1/products (vía API Gateway)
- ✅ Validaciones de negocio en capa de dominio
- ✅ Persistencia en PostgreSQL
- ✅ Respuesta con producto creado

### ✅ HU2 - Actualizar stock de productos
- ✅ Endpoint PUT /api/v1/products/{id}/stock (vía API Gateway)
- ✅ Operaciones ADD/REDUCE para stock
- ✅ Validaciones de stock suficiente
- ✅ Actualización de timestamp automática

### ✅ HU3 - Generar reportes de productos por abastecer
- ✅ Endpoint GET /api/v1/products/low-stock (vía API Gateway)
- ✅ Filtro por threshold de stock mínimo
- ✅ Listado de productos que requieren reabastecimiento

### ✅ Infraestructura de Microservicios
- ✅ **API Gateway** como punto de entrada único
- ✅ **Configuración centralizada** con Config Server
- ✅ **Enrutamiento automático** entre servicios
- ✅ **CORS global** para aplicaciones web

### 🚧 Pendientes
- HU4 - Procesar órdenes de compra
- HU5 - Generar reportes de ventas
- HU6 - Gestión de clientes
- HU7 - Notificaciones de carritos abandonados
- HU8 - Dashboard de análisis y métricas

## 🔧 Herramientas de Desarrollo

- **IDE:** Compatible con IntelliJ IDEA, VS Code, Eclipse
- **Base de Datos:** DBeaver para administración de PostgreSQL
- **Contenedores:** Docker Desktop
- **Testing API:** Postman, Insomnia, o curl
- **Monitoring:** 
  - Adminer (http://localhost:8080) - Base de datos
  - API Gateway Actuator (http://localhost:8090/actuator)
  - Product Service Actuator (http://localhost:8081/actuator)
  - Config Server Actuator (http://localhost:8889/actuator)

## 📞 Soporte

Para dudas o problemas:
1. Revisar logs de aplicación en cada servicio
2. Verificar estado de servicios Docker (`docker-compose ps`)
3. Comprobar conectividad de base de datos
4. Validar configuración en Config Server
5. Verificar que API Gateway esté enrutando correctamente
6. Comprobar endpoints de health: `/actuator/health`

## 🎓 Recursos de Aprendizaje

- **CLAUDE.md** - Checkpoint detallado del estado actual
- **GUIA_COMPLETA_IMPLEMENTACION.md** - Guía paso a paso para replicar el proyecto
- Documentación de Spring Cloud Gateway
- Principios de Clean Architecture
- Patrones de Microservicios

---

## 📊 Estado Actual del Proyecto

```
✅ Config Server (Configuración centralizada)
✅ API Gateway (Punto de entrada único)  
✅ Product Service (3 HU implementadas)
✅ Docker Services (PostgreSQL, MongoDB, Adminer)
🚧 Service Discovery (Próximo)
🚧 Authorization Server (Próximo)
🚧 Order Service (Próximo)
```

**Arquitectura:** Cliente → API Gateway → Product Service → PostgreSQL

---

*Proyecto desarrollado aplicando las mejores prácticas de arquitectura de microservicios, Clean Architecture y principios SOLID. Implementación paso a paso con enfoque educativo.*
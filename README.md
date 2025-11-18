# 🛍️ Arka - Sistema de E-commerce con Microservicios

[![CI Tests](https://github.com/TU_USUARIO/ProyectoArkaAceleraTi/workflows/CI%20-%20Tests%20y%20Cobertura/badge.svg)](https://github.com/TU_USUARIO/ProyectoArkaAceleraTi/actions)
[![Build Images](https://github.com/TU_USUARIO/ProyectoArkaAceleraTi/workflows/CD%20-%20Build%20Docker%20Images/badge.svg)](https://github.com/TU_USUARIO/ProyectoArkaAceleraTi/actions)
[![codecov](https://codecov.io/gh/TU_USUARIO/ProyectoArkaAceleraTi/branch/master/graph/badge.svg)](https://codecov.io/gh/TU_USUARIO/ProyectoArkaAceleraTi)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Sistema de e-commerce construido con arquitectura de microservicios, implementando patrones de Clean Architecture, Event-Driven Architecture y Service Discovery.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Microservicios](#-microservicios)
- [Tecnologías](#-tecnologías)
- [Testing](#-testing)
- [CI/CD](#-cicd)
- [Despliegue](#-despliegue)
- [Inicio Rápido](#-inicio-rápido)
- [Documentación](#-documentación)
- [Contribuir](#-contribuir)

---

## ✨ Características

### Funcionalidades del Sistema

- 🛒 **Gestión de Productos**: CRUD completo con categorías y marcas
- 👥 **Gestión de Clientes**: Perfiles de usuario y direcciones de envío
- 📦 **Inventario Inteligente**: Control de stock con reservas y optimistic locking
- 🛍️ **Órdenes de Compra**: Flujo completo desde creación hasta entrega
- 🛒 **Carrito de Compras**: Gestión de carritos con MongoDB
- 📧 **Notificaciones**: Sistema de emails con RabbitMQ
- 📊 **Reportes**: Análisis de ventas y stock
- 🔐 **Autenticación**: Sistema de autenticación centralizado

### Características Técnicas

- ⚡ **Alta Disponibilidad**: Service Discovery con Eureka
- 🔄 **Event-Driven**: Mensajería asíncrona con RabbitMQ
- 🐳 **Containerizado**: Docker y Docker Compose
- 🧪 **Testing**: 97+ tests con cobertura del 70%
- 🚀 **CI/CD**: GitHub Actions con deploy automático
- 📈 **Observabilidad**: Actuator y métricas con Prometheus
- 🔒 **Seguridad**: Validación de entrada y manejo de errores
- 🌐 **API Gateway**: Enrutamiento centralizado

---

## 🏗️ Arquitectura

### Arquitectura de Microservicios

```
                                    ┌─────────────────┐
                                    │   API Gateway   │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
         ┌──────────▼──────────┐  ┌─────────▼─────────┐  ┌──────────▼──────────┐
         │  Product Service    │  │  Order Service     │  │  Customer Service   │
         │  (PostgreSQL)       │  │  (PostgreSQL)      │  │  (PostgreSQL)       │
         └──────────┬──────────┘  └─────────┬─────────┘  └──────────┬──────────┘
                    │                       │                        │
                    │             ┌─────────▼─────────┐             │
                    └────────────►│ Inventory Service │◄────────────┘
                                  │  (PostgreSQL)     │
                                  └─────────┬─────────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
         ┌──────────▼──────────┐  ┌────────▼────────┐  ┌──────────▼──────────┐
         │   Cart Service      │  │  Report Service  │  │ Notification Service│
         │   (MongoDB)         │  │  (PostgreSQL)    │  │  (PostgreSQL)       │
         └─────────────────────┘  └──────────────────┘  └──────────┬──────────┘
                                                                    │
                                            ┌───────────────────────▼──────────┐
                                            │       RabbitMQ Message Broker     │
                                            └──────────────────────────────────┘

         ┌─────────────────────────────────────────────────────────────────────┐
         │                      Infrastructure Services                         │
         │  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐    │
         │  │ Eureka Server│  │ Config Server│  │    Auth Server        │    │
         │  │  (Discovery) │  │ (Config Mgmt)│  │  (Authentication)     │    │
         │  └──────────────┘  └──────────────┘  └───────────────────────┘    │
         └─────────────────────────────────────────────────────────────────────┘
```

### Patrones Implementados

- **Clean Architecture**: Separación de dominio, aplicación e infraestructura
- **CQRS**: Separación de comandos y consultas
- **Event Sourcing**: Eventos de dominio con RabbitMQ
- **Service Discovery**: Eureka para registro dinámico
- **API Gateway Pattern**: Enrutamiento centralizado
- **Circuit Breaker**: Resiliencia con Resilience4j
- **Optimistic Locking**: Control de concurrencia

---

## 🔧 Microservicios

### Servicios de Dominio

| Servicio | Puerto | Base de Datos | Descripción |
|----------|--------|---------------|-------------|
| **product-service** | 8081 | PostgreSQL | Gestión de productos, categorías y marcas |
| **customer-service** | 8082 | PostgreSQL | Gestión de clientes y direcciones |
| **inventory-service** | 8083 | PostgreSQL | Control de inventario con concurrencia |
| **order-service** | 8084 | PostgreSQL | Procesamiento de órdenes |
| **cart-service** | 8085 | MongoDB | Gestión de carritos de compra |
| **notification-service** | 8086 | PostgreSQL | Envío de notificaciones por email |
| **report-service** | 8087 | PostgreSQL | Generación de reportes |

### Servicios de Infraestructura

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **api-gateway** | 8080 | Gateway centralizado con Spring Cloud Gateway |
| **eureka-server** | 8761 | Service Discovery y registro de servicios |
| **config-server** | 8888 | Configuración centralizada |
| **auth-server** | 9000 | Autenticación y autorización |

---

## 🛠️ Tecnologías

### Backend

- **Java 21**: Lenguaje de programación
- **Spring Boot 3.5.5**: Framework principal
- **Spring Cloud 2025.0.0**: Microservicios
- **Spring Data JPA**: Acceso a datos relacionales
- **Spring Data MongoDB**: Acceso a datos NoSQL
- **Spring AMQP**: Mensajería con RabbitMQ
- **Spring Cloud Gateway**: API Gateway
- **Spring Cloud Netflix Eureka**: Service Discovery
- **Spring Cloud Config**: Configuración centralizada
- **Hibernate**: ORM
- **Lombok**: Reducción de boilerplate
- **MapStruct**: Mapeo de objetos

### Bases de Datos

- **PostgreSQL 15**: Base de datos principal
- **MongoDB 7**: Base de datos para carritos
- **RabbitMQ 3.13**: Message broker

### Testing

- **JUnit 5**: Framework de testing
- **Mockito**: Mocking framework
- **Testcontainers**: Testing con contenedores
- **Spring Boot Test**: Testing de Spring Boot
- **AssertJ**: Assertions fluidas
- **Awaitility**: Testing asíncrono
- **JaCoCo**: Cobertura de código

### DevOps

- **Docker**: Containerización
- **Docker Compose**: Orquestación local
- **GitHub Actions**: CI/CD
- **Gradle**: Build automation
- **Nginx**: Reverse proxy

---

## 🧪 Testing

### Cobertura de Tests

El proyecto cuenta con **97+ tests unitarios e integración** con una cobertura mínima del **70%**.

| Servicio | Tests | Cobertura | Estado |
|----------|-------|-----------|--------|
| product-service | 24 | 70%+ | ✅ |
| inventory-service | 28 | 70%+ | ✅ |
| order-service | 35 | 70%+ | ✅ |
| cart-service | 10 | 70%+ | ✅ |
| **TOTAL** | **97+** | **70%+** | ✅ |

### Tipos de Tests

1. **Repository Tests**: Tests de persistencia con Testcontainers
2. **Controller Tests**: Tests de API REST con MockMvc
3. **Use Case Tests**: Tests de lógica de negocio
4. **Integration Tests**: Tests end-to-end con RabbitMQ

### Ejecutar Tests

```bash
# Test de un servicio específico
cd arka-microservicios/product-service
./gradlew test

# Test con reporte de cobertura
./gradlew test jacocoTestReport

# Ver reporte (abre en navegador)
open build/reports/jacoco/test/html/index.html

# Verificar umbral de cobertura
./gradlew jacocoTestCoverageVerification
```

**Documentación completa**: [TESTING.md](TESTING.md)

---

## 🚀 CI/CD

### Workflows de GitHub Actions

#### 1. **CI - Tests y Cobertura**
- ✅ Ejecución automática en push y PR
- ✅ Tests paralelos por servicio
- ✅ Verificación de cobertura del 70%
- ✅ Reportes con Codecov
- ✅ Detección inteligente de cambios

#### 2. **CD - Build Docker Images**
- ✅ Build automático después de tests exitosos
- ✅ Publicación en GitHub Container Registry
- ✅ Tagging automático (latest, branch, sha)
- ✅ Build paralelo de todas las imágenes

#### 3. **PR - Validaciones**
- ✅ Validación de código
- ✅ Tests de servicios modificados
- ✅ Comentarios automáticos de cobertura
- ✅ Resumen de validaciones

### Pipeline Flow

```
┌──────────────┐
│ Developer    │
│ Push/PR      │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ CI Tests     │
│ (Parallel)   │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ✅ Tests Pass
│ Coverage     │────────────────┐
│ Check (70%)  │                │
└──────┬───────┘                ▼
       │              ┌──────────────────┐
       │              │ Build & Push     │
       │              │ Docker Images    │
       │              └──────────────────┘
       │
       ▼
┌──────────────┐
│ PR Approved  │
│ Ready to     │
│ Merge        │
└──────────────┘
```

**Documentación completa**: [CI-CD.md](CI-CD.md)

---

## 🌐 Despliegue

### Arquitectura de Despliegue en Producción

```
┌────────────────────────────────────────────────────┐
│         AWS EC2 t2.micro (1GB RAM)                 │
│  ┌──────────────────────────────────────────────┐ │
│  │  • Config Server    • API Gateway            │ │
│  │  • Eureka Server    • Auth Server            │ │
│  │  • Product Service  • Order Service          │ │
│  │  • Customer Service • Cart Service           │ │
│  │  • Inventory Service• Notification Service   │ │
│  │  • Report Service                            │ │
│  │  • Prometheus       • Grafana                │ │
│  │  • Nginx (Reverse Proxy)                     │ │
│  └──────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────┘
                     ↓ Conexión a bases de datos
┌────────────────────────────────────────────────────┐
│  Servicios Cloud Gratuitos:                       │
│  • Railway PostgreSQL (gratis)                     │
│  • MongoDB Atlas M0 (gratis)                       │
│  • CloudAMQP RabbitMQ (gratis)                     │
└────────────────────────────────────────────────────┘
```

### Despliegue Rápido (30 minutos)

**Opción 1: Despliegue Manual**
```bash
# 1. Configurar EC2 (script automático)
wget https://raw.githubusercontent.com/TU_USUARIO/ProyectoArkaAceleraTi/main/scripts/aws-setup.sh
chmod +x aws-setup.sh
./aws-setup.sh

# 2. Configurar variables de entorno
nano ~/arka-deployment/.env

# 3. Desplegar servicios
cd ~/arka-deployment
./aws-deploy.sh all
```

**Opción 2: CI/CD Automático**
1. Configurar GitHub Secrets (ver [DEPLOYMENT.md](DEPLOYMENT.md))
2. Push a `main` → Deploy automático a AWS EC2

### Recursos Necesarios

| Recurso | Proveedor | Costo | Límites |
|---------|-----------|-------|---------|
| EC2 t2.micro | AWS | **Gratis** (12 meses) | 1GB RAM, 30GB storage |
| PostgreSQL | Railway | **Gratis** | 500MB, 500 horas/mes |
| MongoDB | Atlas | **Gratis** | 512MB, M0 cluster |
| RabbitMQ | CloudAMQP | **Gratis** | Lemur plan |
| Total | - | **$0-15/mes** | Después del año gratis |

### Scripts de Utilidad

```bash
# Monitoreo en tiempo real
./scripts/aws-monitor.sh

# Troubleshooting
./scripts/aws-troubleshoot.sh [service-name]

# Desplegar servicio específico
./scripts/aws-deploy.sh api-gateway

# Ver logs de servicio
docker-compose -f docker-compose-production.yml logs -f product-service
```

### URLs de Acceso

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **API Gateway** | `http://<EC2_IP>:8080` | Punto de entrada principal |
| **Eureka Dashboard** | `http://<EC2_IP>:8761` | Registro de servicios |
| **Config Server** | `http://<EC2_IP>:8889` | Configuración centralizada |
| **Prometheus** | `http://<EC2_IP>:9090` | Métricas y monitoring |
| **Grafana** | `http://<EC2_IP>:3000` | Dashboards (admin/admin) |

### Documentación de Despliegue

- 📖 **[QUICKSTART.md](QUICKSTART.md)** - Guía rápida paso a paso (30 min)
- 📚 **[DEPLOYMENT.md](DEPLOYMENT.md)** - Guía completa de despliegue
- 🔍 **[OBSERVABILITY.md](OBSERVABILITY.md)** - Monitoreo y métricas
- 🛠️ **Scripts**: `scripts/aws-*.sh` - Automatización

### Optimización para 1GB RAM

El sistema está optimizado para correr en EC2 t2.micro:
- ✅ JVM tuning: `-Xmx64m` a `-Xmx128m` por servicio
- ✅ Swap de 4GB configurado automáticamente
- ✅ Límites de memoria en docker-compose
- ✅ Serial GC para bajo overhead
- ✅ Total: ~950MB + swap

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 21
- Docker y Docker Compose
- Gradle 8.x
- Git

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/TU_USUARIO/ProyectoArkaAceleraTi.git
cd ProyectoArkaAceleraTi

# 2. Iniciar servicios de infraestructura
cd arka-microservicios
docker-compose up -d postgres mongodb rabbitmq

# 3. Iniciar Config Server (primero)
cd config-server
./gradlew bootRun

# 4. Iniciar Eureka Server
cd ../eureka-server
./gradlew bootRun

# 5. Iniciar servicios de negocio
cd ../product-service
./gradlew bootRun

# Repetir para otros servicios...
```

### Docker Compose (Todos los servicios)

```bash
# Iniciar todo el sistema
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener todo
docker-compose down
```

### Verificar Servicios

```bash
# Eureka Dashboard
http://localhost:8761

# API Gateway
http://localhost:8080

# Example endpoint
curl http://localhost:8080/api/v1/products
```

---

## 📚 Documentación

### Documentación Técnica

- 📖 [TESTING.md](TESTING.md) - Guía completa de testing
- 🚀 [CI-CD.md](CI-CD.md) - Configuración de CI/CD
- 🐳 [DEPLOYMENT.md](DEPLOYMENT.md) - Guía de despliegue
- 🌍 [DESPLIEGUE.md](DESPLIEGUE.md) - Guía de despliegue en español

### API Documentation

Cada servicio expone su documentación Swagger en:
```
http://localhost:<PUERTO>/swagger-ui.html
```

Ejemplo:
- Product Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html

### Arquitectura

```
src/
├── domain/              # Lógica de negocio pura
│   ├── entities/        # Entidades de dominio
│   ├── repositories/    # Interfaces de repositorio
│   ├── services/        # Servicios de dominio
│   └── exceptions/      # Excepciones de negocio
├── application/         # Casos de uso
│   ├── usecases/        # Casos de uso específicos
│   ├── dto/             # DTOs de aplicación
│   └── ports/           # Mappers
├── infrastructure/      # Implementaciones técnicas
│   ├── config/          # Configuración
│   ├── persistence/     # Repositorios JPA/MongoDB
│   ├── clients/         # Clientes HTTP
│   └── events/          # Publicadores de eventos
└── interfaces/          # Puntos de entrada
    ├── controllers/     # REST controllers
    ├── dto/             # DTOs de API
    └── exceptions/      # Manejadores de excepciones
```

---

## 🤝 Contribuir

### Proceso de Contribución

1. **Fork** el repositorio
2. Crea una **rama feature** (`git checkout -b feature/nueva-funcionalidad`)
3. **Escribe tests** para tu código
4. Asegúrate de que los **tests pasan** (`./gradlew test`)
5. **Commit** tus cambios (`git commit -m 'feat: agregar nueva funcionalidad'`)
6. **Push** a la rama (`git push origin feature/nueva-funcionalidad`)
7. Abre un **Pull Request**
8. Espera las **validaciones automáticas**

### Estándares de Código

- ✅ Cobertura mínima del 70%
- ✅ Tests para nuevas funcionalidades
- ✅ Clean Architecture
- ✅ Nombres descriptivos en inglés
- ✅ Javadoc para clases públicas
- ✅ Mensajes de commit convencionales

### Conventional Commits

```bash
feat: nueva funcionalidad
fix: corrección de bug
docs: cambios en documentación
test: agregar o modificar tests
refactor: refactorización de código
chore: tareas de mantenimiento
```

---

## 📊 Roadmap

### Implementado ✅

- [x] Arquitectura de microservicios
- [x] Clean Architecture
- [x] Service Discovery (Eureka)
- [x] API Gateway
- [x] Event-Driven con RabbitMQ
- [x] Testing (97+ tests, 70% coverage)
- [x] CI/CD con GitHub Actions
- [x] Docker y Docker Compose
- [x] Documentación completa

### En Progreso 🚧

- [ ] Tests de Auth Server
- [ ] Monitoreo con Prometheus + Grafana
- [ ] Distributed Tracing con Zipkin
- [ ] ELK Stack para logs

### Futuro 🔮

- [ ] Kubernetes deployment
- [ ] GraphQL API
- [ ] Caching con Redis
- [ ] Feature flags
- [ ] A/B Testing

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

---

## 👥 Equipo

Desarrollado como parte del programa AceleraTi.

---

## 📞 Contacto

Para preguntas y soporte:
- 🐛 Issues: [GitHub Issues](https://github.com/TU_USUARIO/ProyectoArkaAceleraTi/issues)
- 📧 Email: tu-email@example.com

---

**⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub!**
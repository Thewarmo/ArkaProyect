# Auth Server - Arka Microservices

Servidor de autenticación y autorización para el sistema Arka. Proporciona funcionalidades de registro, login y validación de tokens JWT.

## Características

- ✅ Registro de usuarios con roles (ADMIN, CUSTOMER)
- ✅ Autenticación con JWT
- ✅ Validación de tokens
- ✅ Gestión de usuarios
- ✅ Clean Architecture
- ✅ PostgreSQL en Railway
- ✅ Spring Security

## Arquitectura

```
interfaces/          - Controladores REST
  └── controllers/   - AuthController
  └── exceptions/    - Manejo global de excepciones
  └── dto/          - ErrorResponse

application/         - Casos de uso
  └── usecases/     - RegisterUserUseCase, LoginUserUseCase, ValidateTokenUseCase, GetUserByIdUseCase
  └── dto/          - RegisterRequest, LoginRequest, AuthResponse, ValidateTokenResponse, UserResponse

domain/             - Lógica de negocio
  └── entities/     - User, Role
  └── repositories/ - UserRepository (interface)
  └── exceptions/   - Excepciones de dominio

infrastructure/     - Implementación técnica
  └── persistence/  - UserJPA, UserJPARepository, UserRepositoryImpl, UserEntityMapper
  └── security/     - JwtUtil, SecurityConfig, PasswordEncoderConfig
```

## Endpoints

### POST /api/v1/auth/register
Registra un nuevo usuario en el sistema.

**Request:**
```json
{
  "username": "admin",
  "email": "admin@arka.com",
  "password": "admin123",
  "firstName": "Admin",
  "lastName": "Arka",
  "role": "ADMIN"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@arka.com",
  "role": "ADMIN",
  "message": "Usuario registrado exitosamente"
}
```

### POST /api/v1/auth/login
Autentica un usuario existente.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@arka.com",
  "role": "ADMIN",
  "message": "Login exitoso"
}
```

### POST /api/v1/auth/validate
Valida un token JWT.

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "valid": true,
  "userId": 1,
  "username": "admin",
  "email": "admin@arka.com",
  "role": "ADMIN",
  "message": "Token válido"
}
```

### GET /api/v1/auth/validate/{token}
Alternativa GET para validar un token.

### GET /api/v1/auth/users/{userId}
Obtiene información de un usuario por ID.

**Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@arka.com",
  "firstName": "Admin",
  "lastName": "Arka",
  "fullName": "Admin Arka",
  "role": "ADMIN",
  "enabled": true,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

## Configuración

### Base de datos (Railway)
```yaml
datasource:
  url: jdbc:postgresql://centerbeam.proxy.rlwy.net:34241/railway
  username: postgres
  password: HMroWPVhjEVmVdDIPhhPABBtEbBKirFd
```

### JWT
```yaml
jwt:
  secret: ArkaSecretKeyForJWTTokenGenerationAndValidation2024!MustBeLongEnoughForHS256Algorithm
  expiration: 86400000 # 24 horas
```

## Ejecutar

### Prerequisitos
1. Config Server debe estar corriendo en puerto 8889
2. Base de datos PostgreSQL en Railway

### Iniciar el servicio
```bash
./gradlew bootRun
```

El servicio estará disponible en: http://localhost:8082

### A través del API Gateway
El Auth Server también es accesible a través del API Gateway en: http://localhost:8090/api/v1/auth/**

## Roles disponibles

- **ADMIN**: Administrador con acceso completo al sistema
- **CUSTOMER**: Cliente con acceso limitado a funciones de compra

## Seguridad

- Contraseñas hasheadas con BCrypt
- Tokens JWT con firma HMAC SHA-256
- Sesiones stateless
- Expiración de tokens configurable (por defecto 24 horas)

## Testing

Ejecutar tests:
```bash
./gradlew test
```

## Tecnologías

- Java 21
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt 0.12.6)
- Lombok
- Gradle

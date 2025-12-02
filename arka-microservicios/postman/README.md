# Arka Microservices - Postman Test Collection

Colección completa de pruebas para los microservicios de Arka.

## Archivos

| Archivo | Descripción |
|---------|-------------|
| `Arka-Microservices-E2E-Tests.postman_collection.json` | Colección principal con todas las pruebas |
| `Arka-Local-Environment.postman_environment.json` | Variables para ambiente local |
| `Arka-EC2-Environment.postman_environment.json` | Variables para AWS EC2 |

## Importar en Postman

1. Abrir Postman
2. Click en **Import** (Ctrl+O)
3. Seleccionar los archivos `.json`
4. Seleccionar el ambiente apropiado (Local o EC2)

## Estructura de la Colección

```
Arka Microservices - E2E Tests
├── 0. Health Checks
│   ├── Config Server Health
│   ├── Eureka Server Health
│   └── API Gateway Health
│
├── 1. Auth Server
│   ├── 1.1 Register User
│   ├── 1.2 Login User
│   ├── 1.3 Validate Token
│   └── 1.4 Get User Details
│
├── 2. Customer Service
│   ├── 2.1 Create Customer
│   ├── 2.2 Get Customer by ID
│   ├── 2.3 Get Customer by User ID
│   ├── 2.4 Add Address to Customer
│   ├── 2.5 Update Customer
│   └── 2.6 Get All Customers
│
├── 3. Product Service
│   ├── 3.1 Create Product
│   ├── 3.2 Get All Products
│   ├── 3.3 Get Product by ID
│   ├── 3.4 Update Product Stock
│   └── 3.5 Get Low Stock Products
│
├── 4. Cart Service
│   ├── 4.1 Get or Create Cart
│   ├── 4.2 Add Item to Cart
│   ├── 4.3 Add Another Item
│   └── 4.4 Get Abandoned Carts
│
├── 5. Inventory Service
│   ├── 5.1 Get Inventory for Product
│   ├── 5.2 Reserve Stock
│   └── 5.3 Release Stock
│
├── 6. Order Service
│   ├── 6.1 Create Order
│   ├── 6.2 Get Order by ID
│   └── 6.3 Cancel Order
│
├── 7. Notification Service
│   ├── 7.1 Send Notification
│   ├── 7.2 Get Notification by ID
│   ├── 7.3 Get All Notifications
│   ├── 7.4 Get Notifications by Status
│   └── 7.5 Get Notifications for Order
│
├── 8. Report Service
│   ├── 8.1 Get Current Week Sales Report
│   ├── 8.2 Get Previous Week Sales Report
│   ├── 8.3 Get Custom Date Range Report
│   ├── 8.4 Export Report as CSV
│   └── 8.5 Export Report as PDF
│
└── 9. E2E Complete Flow
    ├── E2E 1. Register New User
    ├── E2E 2. Create Customer Profile
    ├── E2E 3. Create Product
    ├── E2E 4. Add Product to Cart
    ├── E2E 5. Create Order
    ├── E2E 6. Verify Inventory Reserved
    ├── E2E 7. Check Order Notification
    ├── E2E 8. Generate Sales Report
    └── E2E 9. Cleanup - Clear Cart
```

## Flujo E2E Completo

El folder **9. E2E Complete Flow** simula un ciclo de compra completo:

```
┌─────────────────┐
│  1. Register    │ → Crear cuenta de usuario
└────────┬────────┘
         ▼
┌─────────────────┐
│  2. Customer    │ → Crear perfil de cliente
└────────┬────────┘
         ▼
┌─────────────────┐
│  3. Product     │ → Crear producto para venta
└────────┬────────┘
         ▼
┌─────────────────┐
│  4. Cart        │ → Agregar producto al carrito
└────────┬────────┘
         ▼
┌─────────────────┐
│  5. Order       │ → Crear orden de compra
└────────┬────────┘
         ▼
┌─────────────────┐
│  6. Inventory   │ → Verificar reserva de stock
└────────┬────────┘
         ▼
┌─────────────────┐
│  7. Notification│ → Verificar email de confirmación
└────────┬────────┘
         ▼
┌─────────────────┐
│  8. Report      │ → Generar reporte de ventas
└────────┬────────┘
         ▼
┌─────────────────┐
│  9. Cleanup     │ → Limpiar carrito
└─────────────────┘
```

## Ejecutar Pruebas

### Desde Postman GUI

1. Seleccionar el ambiente (Local o EC2)
2. Abrir **Collection Runner** (Ctrl+Shift+R)
3. Seleccionar la colección o carpeta a ejecutar
4. Click en **Run**

### Desde CLI con Newman

```bash
# Instalar Newman
npm install -g newman

# Ejecutar todas las pruebas (ambiente local)
newman run Arka-Microservices-E2E-Tests.postman_collection.json \
  -e Arka-Local-Environment.postman_environment.json

# Ejecutar solo el flujo E2E
newman run Arka-Microservices-E2E-Tests.postman_collection.json \
  -e Arka-Local-Environment.postman_environment.json \
  --folder "9. E2E Complete Flow"

# Ejecutar con reporte HTML
newman run Arka-Microservices-E2E-Tests.postman_collection.json \
  -e Arka-EC2-Environment.postman_environment.json \
  -r htmlextra \
  --reporter-htmlextra-export ./reports/test-report.html
```

## Configurar Ambiente EC2

Antes de usar el ambiente EC2, actualiza la IP pública:

1. Abrir `Arka-EC2-Environment.postman_environment.json`
2. Reemplazar `YOUR_EC2_PUBLIC_IP` con la IP real de tu instancia EC2
3. Importar el archivo actualizado en Postman

## Variables Automáticas

La colección usa **Pre-request Scripts** y **Tests** para:

- Generar usernames únicos con timestamp
- Guardar tokens de autenticación automáticamente
- Pasar IDs entre requests (user_id → customer_id → order_id)
- Calcular fechas dinámicas para reportes

## Endpoints Cubiertos

| Servicio | Endpoints | Métodos |
|----------|-----------|---------|
| Auth Server | 4 | POST, GET |
| Customer Service | 6 | POST, GET, PUT |
| Product Service | 5 | POST, GET, PUT |
| Cart Service | 4 | POST, GET, DELETE |
| Inventory Service | 3 | POST, GET |
| Order Service | 3 | POST, GET, PUT |
| Notification Service | 5 | POST, GET |
| Report Service | 5 | GET |
| **Total** | **35** | |

## Notas

- Cada request incluye tests automáticos que verifican status codes y estructura de respuesta
- Los tokens JWT se guardan automáticamente después del login
- El flujo E2E genera datos únicos con timestamps para evitar conflictos
- Las pruebas de reportes calculan fechas dinámicamente

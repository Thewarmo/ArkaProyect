# 🧪 Guía de Testing End-to-End - Proyecto Arka

**Fecha**: 2025-01-15
**Versión**: 1.0
**Propósito**: Testing completo del ciclo de vida de órdenes con gestión de inventario

---

## 📋 Tabla de Contenidos

1. [Pre-requisitos](#pre-requisitos)
2. [Configuración del Entorno](#configuración-del-entorno)
3. [Datos de Prueba](#datos-de-prueba)
4. [Casos de Prueba](#casos-de-prueba)
5. [Validaciones](#validaciones)
6. [Casos de Error](#casos-de-error)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 Pre-requisitos

### Servicios Requeridos

Todos los siguientes servicios deben estar ejecutándose:

- ✅ **PostgreSQL** (Railway): `centerbeam.proxy.rlwy.net:34241`
- ✅ **Config Server** (Puerto 8888)
- ✅ **API Gateway** (Puerto 8080)
- ✅ **Auth Server** (Puerto 8082)
- ✅ **Product Service** (Puerto 8081)
- ✅ **Customer Service** (Puerto 8083)
- ✅ **Inventory Service** (Puerto 8084)
- ✅ **Order Service** (Puerto 8085)

### Verificación de Conectividad

```bash
# Verificar PostgreSQL
psql -h centerbeam.proxy.rlwy.net -p 34241 -U postgres -d railway

# Verificar servicios activos
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # Auth Server
curl http://localhost:8081/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Customer Service
curl http://localhost:8084/actuator/health  # Inventory Service
curl http://localhost:8085/actuator/health  # Order Service
```

---

## ⚙️ Configuración del Entorno

### Iniciar Servicios (Orden Recomendado)

```bash
# Terminal 1: Config Server
cd arka-microservicios/config-server
./gradlew bootRun

# Terminal 2: API Gateway (esperar ~30s)
cd arka-microservicios/api-gateway
./gradlew bootRun

# Terminal 3: Auth Server
cd arka-microservicios/auth-server
./gradlew bootRun

# Terminal 4: Product Service
cd arka-microservicios/product-service
./gradlew bootRun

# Terminal 5: Customer Service
cd arka-microservicios/customer-service
./gradlew bootRun

# Terminal 6: Inventory Service
cd arka-microservicios/inventory-service
./gradlew bootRun

# Terminal 7: Order Service
cd arka-microservicios/order-service
./gradlew bootRun
```

**Nota**: En Windows, usar `gradlew.bat` en lugar de `./gradlew`

---

## 📦 Datos de Prueba

### Paso 1: Registrar Usuario

```bash
curl -X POST http://localhost:8082/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"testuser1\",
    \"email\": \"testuser1@email.com\",
    \"password\": \"Test123!\",
    \"firstName\": \"Juan\",
    \"lastName\": \"Pérez\",
    \"role\": \"CUSTOMER\"
  }"
```

**Respuesta Esperada**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "testuser1",
  "email": "testuser1@email.com",
  "role": "CUSTOMER"
}
```

**Guardar**: `userId` para siguientes pasos

---

### Paso 2: Crear Cliente (Customer)

```bash
curl -X POST http://localhost:8083/api/v1/customers \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": 1,
    \"companyName\": \"Tech Solutions SAS\",
    \"taxId\": \"900123456-7\",
    \"contactName\": \"Juan Pérez\",
    \"phone\": \"+57 300 1234567\",
    \"email\": \"juan@techsolutions.com\",
    \"country\": \"COLOMBIA\"
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "userId": 1,
  "companyName": "Tech Solutions SAS",
  "taxId": "900123456-7",
  "contactName": "Juan Pérez",
  "phone": "+57 300 1234567",
  "email": "juan@techsolutions.com",
  "country": "COLOMBIA",
  "active": true,
  "defaultAddress": null
}
```

**Guardar**: `customerId` = 1

---

### Paso 3: Agregar Dirección al Cliente

```bash
curl -X POST http://localhost:8083/api/v1/customers/1/addresses \
  -H "Content-Type: application/json" \
  -d "{
    \"street\": \"Calle 123 #45-67\",
    \"city\": \"Bogotá\",
    \"state\": \"Cundinamarca\",
    \"postalCode\": \"110111\",
    \"country\": \"COLOMBIA\",
    \"isDefault\": true
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "userId": 1,
  "companyName": "Tech Solutions SAS",
  "defaultAddress": {
    "id": 1,
    "street": "Calle 123 #45-67",
    "city": "Bogotá",
    "state": "Cundinamarca",
    "postalCode": "110111",
    "country": "COLOMBIA",
    "isDefault": true
  }
}
```

---

### Paso 4: Crear Categoría (Product Service)

```bash
curl -X POST http://localhost:8081/api/v1/categories \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Periféricos\",
    \"description\": \"Teclados, mouse, audífonos\"
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "name": "Periféricos",
  "description": "Teclados, mouse, audífonos",
  "active": true
}
```

**Guardar**: `categoryId` = 1

---

### Paso 5: Crear Marca (Product Service)

```bash
curl -X POST http://localhost:8081/api/v1/brands \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Logitech\",
    \"description\": \"Periféricos de alta calidad\"
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "name": "Logitech",
  "description": "Periféricos de alta calidad",
  "active": true
}
```

**Guardar**: `brandId` = 1

---

### Paso 6: Crear Producto

```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Mouse Gamer RGB G502\",
    \"description\": \"Mouse gamer con iluminación RGB y 11 botones programables\",
    \"price\": 189900.00,
    \"stock\": 100,
    \"categoryId\": 1,
    \"brandId\": 1
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "name": "Mouse Gamer RGB G502",
  "description": "Mouse gamer con iluminación RGB y 11 botones programables",
  "price": 189900.00,
  "stock": 100,
  "categoryId": 1,
  "brandId": 1,
  "active": true
}
```

**Guardar**: `productId` = 1

---

### Paso 7: Inicializar Inventario

**Nota**: Esto debe hacerse automáticamente al crear el producto. Si no existe, crear manualmente:

```bash
curl -X POST http://localhost:8084/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d "{
    \"productId\": 1,
    \"availableStock\": 100,
    \"reservedStock\": 0,
    \"minStockLevel\": 10
  }"
```

**Verificar inventario inicial**:
```bash
curl http://localhost:8084/api/v1/inventory/product/1
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "productId": 1,
  "availableStock": 100,
  "reservedStock": 0,
  "totalStock": 100,
  "minStockLevel": 10,
  "isLowStock": false
}
```

---

## ✅ Casos de Prueba

### Caso 1: Crear Orden (Happy Path)

**Objetivo**: Crear orden exitosamente con reserva automática de stock

```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 1,
    \"shippingAddress\": \"Calle 123 #45-67, Bogotá, Colombia\",
    \"items\": [
      {
        \"productId\": 1,
        \"quantity\": 5,
        \"unitPrice\": 189900.00
      }
    ],
    \"notes\": \"Entrega urgente - Prueba E2E\"
  }"
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "orderNumber": "ORD-20250115-000001",
  "customerId": 1,
  "status": "PENDING",
  "totalAmount": 949500.00,
  "shippingAddress": "Calle 123 #45-67, Bogotá, Colombia",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Mouse Gamer RGB G502",
      "quantity": 5,
      "unitPrice": 189900.00,
      "subtotal": 949500.00
    }
  ],
  "notes": "Entrega urgente - Prueba E2E",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**Guardar**: `orderId` = 1

---

### Validación 1.1: Verificar Stock Reservado

```bash
curl http://localhost:8084/api/v1/inventory/product/1
```

**Resultado Esperado**:
```json
{
  "id": 1,
  "productId": 1,
  "availableStock": 95,    // ⬅️ Reducido de 100 a 95
  "reservedStock": 5,       // ⬅️ Incrementado de 0 a 5
  "totalStock": 100,        // ⬅️ Sin cambios
  "minStockLevel": 10,
  "isLowStock": false
}
```

✅ **Verificación**:
- availableStock = 100 - 5 = 95 ✅
- reservedStock = 0 + 5 = 5 ✅
- totalStock = 100 (sin cambios) ✅

---

### Validación 1.2: Verificar Movimiento de Stock

```bash
# Consultar base de datos directamente
psql -h centerbeam.proxy.rlwy.net -p 34241 -U postgres -d railway -c \
  "SELECT * FROM stock_movements WHERE product_id = 1 ORDER BY created_at DESC LIMIT 1;"
```

**Resultado Esperado**:
```
 id | inventory_id | product_id | movement_type | quantity | previous_stock | new_stock |           reason           |     created_at
----+--------------+------------+---------------+----------+----------------+-----------+---------------------------+---------------------
  1 |            1 |          1 | RESERVED      |        5 |            100 |        95 | Reserva de stock para orden | 2025-01-15 10:30:00
```

✅ **Verificación**:
- movement_type = RESERVED ✅
- quantity = 5 ✅
- previous_stock = 100 ✅
- new_stock = 95 ✅

---

### Caso 2: Cancelar Orden (Happy Path)

**Objetivo**: Cancelar orden y verificar liberación automática de stock

```bash
curl -X PUT http://localhost:8085/api/v1/orders/1/cancel
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "orderNumber": "ORD-20250115-000001",
  "customerId": 1,
  "status": "CANCELLED",   // ⬅️ Cambió de PENDING a CANCELLED
  "totalAmount": 949500.00,
  "items": [...],
  "updatedAt": "2025-01-15T10:35:00"
}
```

---

### Validación 2.1: Verificar Stock Liberado

```bash
curl http://localhost:8084/api/v1/inventory/product/1
```

**Resultado Esperado**:
```json
{
  "id": 1,
  "productId": 1,
  "availableStock": 100,   // ⬅️ Restaurado a 100
  "reservedStock": 0,      // ⬅️ Vuelve a 0
  "totalStock": 100,
  "minStockLevel": 10,
  "isLowStock": false
}
```

✅ **Verificación**:
- availableStock = 95 + 5 = 100 ✅
- reservedStock = 5 - 5 = 0 ✅
- Stock totalmente restaurado ✅

---

### Validación 2.2: Verificar Movimiento de Liberación

```bash
psql -h centerbeam.proxy.rlwy.net -p 34241 -U postgres -d railway -c \
  "SELECT * FROM stock_movements WHERE product_id = 1 ORDER BY created_at DESC LIMIT 2;"
```

**Resultado Esperado**:
```
 id | inventory_id | product_id | movement_type | quantity | previous_stock | new_stock |              reason              |     created_at
----+--------------+------------+---------------+----------+----------------+-----------+----------------------------------+---------------------
  2 |            1 |          1 | RELEASED      |        5 |             95 |       100 | Liberación de stock reservado    | 2025-01-15 10:35:00
  1 |            1 |          1 | RESERVED      |        5 |            100 |        95 | Reserva de stock para orden      | 2025-01-15 10:30:00
```

✅ **Verificación**:
- Dos movimientos registrados ✅
- Segundo movimiento es RELEASED ✅
- Stock restaurado de 95 a 100 ✅

---

### Caso 3: Orden con Múltiples Productos

**Objetivo**: Verificar reserva de stock para múltiples productos

**Preparación**: Crear segundo producto
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Teclado Mecánico RGB K70\",
    \"description\": \"Teclado mecánico con switches Cherry MX\",
    \"price\": 459900.00,
    \"stock\": 50,
    \"categoryId\": 1,
    \"brandId\": 1
  }"
```

**Crear Orden**:
```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 1,
    \"shippingAddress\": \"Calle 123 #45-67, Bogotá, Colombia\",
    \"items\": [
      {
        \"productId\": 1,
        \"quantity\": 3,
        \"unitPrice\": 189900.00
      },
      {
        \"productId\": 2,
        \"quantity\": 2,
        \"unitPrice\": 459900.00
      }
    ],
    \"notes\": \"Combo gamer completo\"
  }"
```

**Validaciones**:
```bash
# Producto 1: Mouse
curl http://localhost:8084/api/v1/inventory/product/1
# Esperado: availableStock=97, reservedStock=3

# Producto 2: Teclado
curl http://localhost:8084/api/v1/inventory/product/2
# Esperado: availableStock=48, reservedStock=2
```

---

### Caso 4: Consultar Orden Existente

```bash
curl http://localhost:8085/api/v1/orders/1
```

**Respuesta Esperada**:
```json
{
  "id": 1,
  "orderNumber": "ORD-20250115-000001",
  "customerId": 1,
  "status": "CANCELLED",
  "totalAmount": 949500.00,
  "items": [...]
}
```

---

## ❌ Casos de Error

### Error 1: Cliente No Existe

```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 999,
    \"shippingAddress\": \"Test\",
    \"items\": [{\"productId\": 1, \"quantity\": 1, \"unitPrice\": 100}]
  }"
```

**Respuesta Esperada**: `404 Not Found`
```json
{
  "message": "Cliente no encontrado con ID: 999",
  "timestamp": "2025-01-15T10:40:00"
}
```

---

### Error 2: Cliente Inactivo

**Preparación**: Desactivar cliente
```bash
# Usar endpoint de actualización para marcar como inactivo
# (implementar si no existe)
```

**Resultado Esperado**: `400 Bad Request`
```json
{
  "message": "El cliente no puede realizar órdenes",
  "timestamp": "2025-01-15T10:41:00"
}
```

---

### Error 3: Cliente Sin Dirección

**Preparación**: Crear cliente sin dirección por defecto

**Resultado Esperado**: `400 Bad Request`
```json
{
  "message": "El cliente no tiene dirección configurada",
  "timestamp": "2025-01-15T10:42:00"
}
```

---

### Error 4: Producto No Existe

```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 1,
    \"shippingAddress\": \"Test\",
    \"items\": [{\"productId\": 999, \"quantity\": 1, \"unitPrice\": 100}]
  }"
```

**Respuesta Esperada**: `404 Not Found`
```json
{
  "message": "Producto no encontrado con ID: 999",
  "timestamp": "2025-01-15T10:43:00"
}
```

---

### Error 5: Stock Insuficiente

```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 1,
    \"shippingAddress\": \"Test\",
    \"items\": [{\"productId\": 1, \"quantity\": 500, \"unitPrice\": 189900}]
  }"
```

**Respuesta Esperada**: `400 Bad Request`
```json
{
  "message": "Stock insuficiente para el producto 1. Disponible: 100, Solicitado: 500",
  "timestamp": "2025-01-15T10:44:00"
}
```

---

### Error 6: Cancelar Orden Ya Cancelada

```bash
# Intentar cancelar la misma orden dos veces
curl -X PUT http://localhost:8085/api/v1/orders/1/cancel
```

**Respuesta Esperada**: `400 Bad Request`
```json
{
  "message": "La orden no puede ser modificada en estado: CANCELLED",
  "timestamp": "2025-01-15T10:45:00"
}
```

---

### Error 7: Orden No Existe

```bash
curl -X PUT http://localhost:8085/api/v1/orders/999/cancel
```

**Respuesta Esperada**: `404 Not Found`
```json
{
  "message": "Orden no encontrada con ID: 999",
  "timestamp": "2025-01-15T10:46:00"
}
```

---

## 🔍 Validaciones Adicionales

### Validación de Logs

**Order Service logs** deben mostrar:
```
INFO  - Iniciando creación de orden para cliente: 1
INFO  - Validando cliente: 1
INFO  - Cliente validado exitosamente: Tech Solutions SAS
INFO  - Validando productos y actualizando precios
INFO  - Producto validado: Mouse Gamer RGB G502
INFO  - Validando disponibilidad de stock
INFO  - Stock disponible confirmado para producto 1: cantidad 5
INFO  - Orden guardada exitosamente: ORD-20250115-000001
INFO  - Reservando stock para la orden: ORD-20250115-000001
INFO  - Stock reservado exitosamente: Producto=1, Cantidad=5
INFO  - Orden creada exitosamente: ORD-20250115-000001
```

**Inventory Service logs** deben mostrar:
```
INFO  - Iniciando reserva de stock: productId=1, quantity=5
DEBUG - Stock antes de reservar: available=100, reserved=0
INFO  - Stock reservado exitosamente: productId=1, newAvailable=95, newReserved=5
INFO  - Movimiento de stock registrado: type=RESERVED, productId=1, quantity=5
```

---

### Validación de Base de Datos

**Consultar orden completa**:
```sql
SELECT
  o.id,
  o.order_number,
  o.customer_id,
  o.status,
  o.total_amount,
  oi.product_id,
  oi.quantity,
  oi.unit_price
FROM orders o
INNER JOIN order_items oi ON o.id = oi.order_id
WHERE o.id = 1;
```

**Consultar inventario completo**:
```sql
SELECT
  i.id,
  i.product_id,
  i.available_stock,
  i.reserved_stock,
  i.total_stock,
  COUNT(sm.id) as total_movements
FROM inventory i
LEFT JOIN stock_movements sm ON i.id = sm.inventory_id
WHERE i.product_id = 1
GROUP BY i.id;
```

---

## 🐛 Troubleshooting

### Problema: Servicio no responde

**Síntomas**: `Connection refused` o timeout

**Solución**:
1. Verificar que el servicio está corriendo: `jps -l` (Java)
2. Verificar puerto no esté ocupado: `netstat -ano | findstr :8085`
3. Revisar logs del servicio por errores de inicio
4. Verificar conexión a PostgreSQL
5. Reiniciar servicio

---

### Problema: Stock no se reserva

**Síntomas**: Orden creada pero stock sin cambios

**Causas Posibles**:
1. Inventory Service no está corriendo
2. URL incorrecta en configuración
3. Error en WebClient silenciado

**Solución**:
1. Verificar logs de Order Service por errores de WebClient
2. Confirmar URL en `order-service.yml`: `services.inventory-service.url`
3. Probar endpoint manualmente: `curl http://localhost:8084/api/v1/inventory/product/1`

---

### Problema: Stock no se libera al cancelar

**Síntomas**: Orden cancelada pero stock sigue reservado

**Causas Posibles**:
1. Endpoint `/release` no implementado
2. Error en liberación capturado y silenciado
3. Stock reservado insuficiente (modificado manualmente)

**Solución**:
1. Revisar logs de Order Service: buscar "Error al liberar stock"
2. Verificar endpoint existe: `curl -X POST http://localhost:8084/api/v1/inventory/release -H "Content-Type: application/json" -d '{"productId":1,"quantity":5}'`
3. Reconciliación manual si es necesario

---

### Problema: Errores de validación

**Síntomas**: `400 Bad Request` con mensaje de validación

**Causas Comunes**:
1. Datos obligatorios faltantes
2. Formato incorrecto (e.g., cantidad negativa)
3. Relaciones rotas (cliente/producto no existe)

**Solución**:
1. Revisar mensaje de error en respuesta
2. Validar JSON request con schema
3. Verificar que datos de prueba existen

---

## �� Métricas de Testing

### Cobertura de Casos

- ✅ Happy path: Crear orden
- ✅ Happy path: Cancelar orden
- ✅ Múltiples productos
- ✅ Validación de cliente
- ✅ Validación de productos
- ✅ Validación de stock
- ✅ Reserva de stock
- ✅ Liberación de stock
- ✅ Movimientos de stock registrados
- ✅ Manejo de errores

**Total**: 10/10 casos cubiertos (100%)

---

## 🎯 Checklist de Validación

### Pre-ejecución
- [ ] Todos los servicios están corriendo
- [ ] PostgreSQL accesible
- [ ] Config Server configurado
- [ ] Datos de prueba creados

### Funcionalidad Core
- [ ] Crear orden exitosamente
- [ ] Stock se reserva automáticamente
- [ ] Precios se actualizan desde Product Service
- [ ] Validaciones de cliente funcionan
- [ ] Movimiento RESERVED registrado

### Cancelación de Orden
- [ ] Cancelar orden exitosamente
- [ ] Stock se libera automáticamente
- [ ] Movimiento RELEASED registrado
- [ ] Orden cambia a estado CANCELLED

### Casos de Error
- [ ] Cliente no existe → 404
- [ ] Producto no existe → 404
- [ ] Stock insuficiente → 400
- [ ] Cancelar orden inválida → 400
- [ ] Orden no existe → 404

### Base de Datos
- [ ] Orden guardada correctamente
- [ ] Items de orden guardados
- [ ] Inventario actualizado
- [ ] Movimientos registrados

### Logs
- [ ] Order Service logs coherentes
- [ ] Inventory Service logs coherentes
- [ ] Sin errores inesperados
- [ ] Warnings documentados

---

## 📝 Reporte de Testing

### Plantilla de Reporte

```markdown
# Reporte de Testing - [Fecha]

## Ambiente
- PostgreSQL: ✅/❌
- Config Server: ✅/❌
- Order Service: ✅/❌
- Inventory Service: ✅/❌
- [Otros servicios...]

## Casos Ejecutados
| Caso | Resultado | Notas |
|------|-----------|-------|
| Crear orden | ✅/❌ | |
| Reservar stock | ✅/❌ | |
| Cancelar orden | ✅/❌ | |
| Liberar stock | ✅/❌ | |
| [Otros...] | ✅/❌ | |

## Defectos Encontrados
1. [Descripción del defecto]
   - Severidad: Alta/Media/Baja
   - Pasos para reproducir: ...
   - Comportamiento esperado: ...
   - Comportamiento actual: ...

## Conclusión
✅ **APROBADO** / ❌ **NO APROBADO**

Comentarios adicionales: ...
```

---

## 🚀 Próximos Pasos

Después de completar este testing:

1. **Si todo funciona**: Pasar a implementar Notification Service
2. **Si hay errores**: Crear issues y priorizar correcciones
3. **Optimizaciones**: Agregar tests unitarios y de integración automatizados
4. **Documentación**: Actualizar README con ejemplos de uso

---

**Versión**: 1.0
**Última actualización**: 2025-01-15
**Autor**: Claude Code
**Estado**: ✅ Listo para testing

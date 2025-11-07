# 📧 Notification Service

Servicio de notificaciones por email para el proyecto Arka. Envía notificaciones automáticas cuando se crean o actualizan órdenes.

---

## 🎯 Funcionalidades

- ✉️ Envío de emails con Gmail SMTP
- 📝 Historial de notificaciones enviadas
- 🔄 Sistema de reintentos automáticos
- 📊 Tracking de estado (PENDING, SENT, FAILED, RETRY)
- 🎨 Templates HTML personalizados para emails
- 📦 Notificación automática al crear orden
- 🔔 Notificación de cambios de estado

---

## 🏗️ Arquitectura

### Clean Architecture
```
notification-service/
├── domain/
│   ├── entities/          # Notification, NotificationType, NotificationStatus
│   ├── repositories/      # NotificationRepository (interface)
│   └── exceptions/        # EmailSendingException, NotificationNotFoundException
├── application/
│   ├── usecases/         # SendNotificationUseCase, SendOrderCreatedNotificationUseCase
│   └── dto/              # NotificationResponse, SendNotificationRequest
├── infrastructure/
│   ├── persistence/      # JPA, Mappers, Repository Implementation
│   └── config/           # EmailService, EmailConfig
└── interfaces/
    ├── controllers/      # NotificationController
    └── exceptions/       # GlobalExceptionHandler
```

---

## ⚙️ Configuración

### 1. Configurar Gmail para SMTP

**Paso 1**: Habilitar "Verificación en 2 pasos" en tu cuenta de Gmail
1. Ve a https://myaccount.google.com/security
2. Habilita "Verificación en 2 pasos"

**Paso 2**: Generar contraseña de aplicación
1. Ve a https://myaccount.google.com/apppasswords
2. Selecciona "Correo" y "Otro (nombre personalizado)"
3. Ingresa "Arka Notification Service"
4. Copia la contraseña generada (16 caracteres)

**Paso 3**: Configurar variables de entorno

**En Windows (PowerShell)**:
```powershell
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="tu-contraseña-de-app"
```

**En Linux/Mac**:
```bash
export MAIL_USERNAME="tu-email@gmail.com"
export MAIL_PASSWORD="tu-contraseña-de-app"
```

**Alternativa**: Editar `arka-config-repo/notification-service.yml`:
```yaml
spring:
  mail:
    username: tu-email@gmail.com
    password: tu-contraseña-de-app
```

⚠️ **IMPORTANTE**: No commitear credenciales reales a Git. Usa variables de entorno en producción.

---

## 🚀 Inicio Rápido

### 1. Iniciar Servicios Requeridos

```bash
# 1. Config Server (8889)
cd config-server && ./gradlew bootRun

# 2. PostgreSQL debe estar accesible (Railway)
```

### 2. Iniciar Notification Service

```bash
cd notification-service
./gradlew bootRun
```

El servicio estará disponible en: `http://localhost:8087`

---

## 📡 Endpoints

### POST /api/v1/notifications
Enviar notificación genérica

**Request**:
```json
{
  "type": "ORDER_CREATED",
  "recipientEmail": "cliente@email.com",
  "recipientName": "Juan Pérez",
  "subject": "¡Orden Confirmada!",
  "content": "<html><body><h1>Gracias por tu compra</h1></body></html>",
  "orderId": 1,
  "orderNumber": "ORD-20250115-000001"
}
```

**Response**:
```json
{
  "id": 1,
  "type": "ORDER_CREATED",
  "recipientEmail": "cliente@email.com",
  "recipientName": "Juan Pérez",
  "subject": "¡Orden Confirmada!",
  "status": "SENT",
  "orderId": 1,
  "orderNumber": "ORD-20250115-000001",
  "createdAt": "2025-01-15T10:30:00",
  "sentAt": "2025-01-15T10:30:05"
}
```

---

### POST /api/v1/notifications/order-created
Enviar notificación de orden creada (con template HTML)

**Request**:
```json
{
  "orderId": 1,
  "orderNumber": "ORD-20250115-000001",
  "customerEmail": "cliente@email.com",
  "customerName": "Juan Pérez",
  "totalAmount": 949500.00,
  "shippingAddress": "Calle 123 #45-67, Bogotá, Colombia"
}
```

**Response**: Igual al endpoint anterior

---

### GET /api/v1/notifications/{id}
Obtener notificación por ID

### GET /api/v1/notifications
Listar todas las notificaciones

### GET /api/v1/notifications/status/{status}
Listar notificaciones por estado
- Estados válidos: `PENDING`, `SENT`, `FAILED`, `RETRY`

### GET /api/v1/notifications/order/{orderId}
Listar notificaciones de una orden específica

---

## 🧪 Testing

### Prueba Manual

```bash
# 1. Enviar notificación de prueba
curl -X POST http://localhost:8087/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ORDER_CREATED",
    "recipientEmail": "tu-email@gmail.com",
    "recipientName": "Test User",
    "subject": "Prueba de Notificación",
    "content": "<html><body><h1>¡Hola!</h1><p>Este es un email de prueba.</p></body></html>"
  }'

# 2. Verificar estado
curl http://localhost:8087/api/v1/notifications/1

# 3. Listar notificaciones enviadas
curl http://localhost:8087/api/v1/notifications/status/SENT
```

---

## 📊 Base de Datos

### Tabla: notifications

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | PK, auto-increment |
| type | VARCHAR | ORDER_CREATED, ORDER_CONFIRMED, etc. |
| recipient_email | VARCHAR | Email del destinatario |
| recipient_name | VARCHAR | Nombre del destinatario |
| subject | VARCHAR | Asunto del email |
| content | TEXT | Contenido HTML del email |
| status | VARCHAR | PENDING, SENT, FAILED, RETRY |
| error_message | TEXT | Mensaje de error si falla |
| retry_count | INTEGER | Contador de reintentos |
| order_id | BIGINT | Referencia a orden |
| order_number | VARCHAR | Número de orden |
| created_at | TIMESTAMP | Fecha de creación |
| sent_at | TIMESTAMP | Fecha de envío exitoso |
| updated_at | TIMESTAMP | Última actualización |

---

## 🔧 Configuración Avanzada

### Deshabilitar envío de emails (modo prueba)

En `arka-config-repo/notification-service.yml`:
```yaml
notification:
  enabled: false
```

Con esto, las notificaciones se registran en BD pero NO se envían emails reales.

### Cambiar timeout de SMTP

```yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          connectiontimeout: 10000  # 10 segundos
          timeout: 10000
          writetimeout: 10000
```

### Configurar reintentos

Actualmente configurado en código:
- Máximo 3 reintentos por notificación
- Lógica en `Notification.canRetry()`

---

## 🐛 Troubleshooting

### Error: "Failed to authenticate"
**Causa**: Credenciales incorrectas o no se usa contraseña de aplicación
**Solución**:
- Verifica que usas contraseña de aplicación (no la contraseña normal de Gmail)
- Verifica variables de entorno: `echo $MAIL_USERNAME`

### Error: "Connection timeout"
**Causa**: Firewall o puerto 587 bloqueado
**Solución**:
- Verifica conectividad: `telnet smtp.gmail.com 587`
- Intenta con puerto 465 (SSL) en lugar de 587 (TLS)

### Error: "Invalid email address"
**Causa**: Formato de email incorrecto
**Solución**: Verifica que el email tenga formato válido (contains @)

### Emails no se reciben
**Causa Común**: Emails van a spam
**Solución**:
- Revisa carpeta de spam
- Agrega noreply@arka.com a contactos
- Usa dominio verificado en producción

---

## 📈 Métricas

Query para estadísticas:
```sql
-- Notificaciones por estado
SELECT status, COUNT(*) as count
FROM notifications
GROUP BY status;

-- Notificaciones fallidas recientes
SELECT * FROM notifications
WHERE status = 'FAILED'
ORDER BY created_at DESC
LIMIT 10;

-- Tasa de éxito
SELECT
    ROUND(100.0 * SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) / COUNT(*), 2) as success_rate
FROM notifications;
```

---

## 🔗 Integración con Order Service

Para integrar con Order Service, agregar cliente HTTP en `order-service`:

```java
@Component
public class NotificationServiceClient {

    @Qualifier("notificationWebClient")
    private final WebClient notificationWebClient;

    public void sendOrderCreatedNotification(OrderResponse order, CustomerResponse customer) {
        OrderCreatedNotificationRequest request = OrderCreatedNotificationRequest.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerEmail(customer.getEmail())
            .customerName(customer.getContactName())
            .totalAmount(order.getTotalAmount())
            .shippingAddress(order.getShippingAddress())
            .build();

        notificationWebClient.post()
            .uri("/api/v1/notifications/order-created")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
```

---

## ✅ Checklist de Deployment

- [ ] Variables de entorno configuradas (MAIL_USERNAME, MAIL_PASSWORD)
- [ ] Config Server corriendo
- [ ] PostgreSQL accesible
- [ ] Puerto 8087 disponible
- [ ] Verificación en 2 pasos habilitada en Gmail
- [ ] Contraseña de aplicación generada
- [ ] Prueba de envío exitosa

---

## 📝 Notas Importantes

1. **Límites de Gmail**:
   - Máximo 500 emails/día para cuentas gratuitas
   - Considerar SendGrid/Mailgun para producción

2. **Seguridad**:
   - Nunca commitear credenciales a Git
   - Usar variables de entorno en todos los ambientes
   - Rotar contraseñas de aplicación periódicamente

3. **Performance**:
   - Emails se envían síncronamente (bloquean request)
   - Para producción, considerar queue asíncrona (RabbitMQ, SQS)

4. **Monitoreo**:
   - Revisar logs regularmente
   - Monitorear tasa de emails fallidos
   - Alertar si tasa de fallo > 5%

---

**Puerto**: 8087
**Base de Datos**: PostgreSQL Railway (compartida)
**Estado**: ✅ FUNCIONAL

**Documentación adicional**: Ver [NOTIFICATION_SERVICE_IMPLEMENTATION.md](../NOTIFICATION_SERVICE_IMPLEMENTATION.md) para detalles técnicos completos.

# 🚀 Quick Start: Notificaciones Asíncronas con RabbitMQ

Guía rápida para probar el sistema completo de notificaciones asíncronas.

---

## 📋 Pre-requisitos

- Docker Desktop instalado
- Java 21
- Gradle
- PostgreSQL Railway accesible
- Gmail con contraseña de aplicación (para emails)

---

## ⚡ Inicio Rápido (5 minutos)

### 1. Iniciar RabbitMQ

```bash
cd arka-microservicios
docker-compose up -d rabbitmq
```

**Verificar**: http://localhost:15672
- Usuario: `arka_user`
- Password: `arka_password`

### 2. Iniciar Config Server

```bash
cd config-server
./gradlew bootRun
```

Espera que arranque (~30 segundos)

### 3. Iniciar Order Service

```bash
cd ../order-service
./gradlew bootRun
```

### 4. Iniciar Notification Service

```bash
cd ../notification-service

# Configurar Gmail (Windows PowerShell)
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="tu-app-password-16-caracteres"

./gradlew bootRun
```

### 5. Crear Orden y Ver Magia 🎩✨

```bash
curl -X POST http://localhost:8085/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "shippingAddress": "Calle 123, Bogotá",
    "items": [{
      "productId": 1,
      "quantity": 2,
      "unitPrice": 100000
    }]
  }'
```

**¿Qué sucede?**

1. ⚡ **Respuesta inmediata** (100-200ms)
2. 📨 **Email enviado en background** (3-5 segundos después)
3. 📊 **Evento visible en RabbitMQ UI**

---

## 🔍 Verificación

### Ver Cola en RabbitMQ

1. Abrir: http://localhost:15672
2. Ir a: **Queues** tab
3. Buscar: `order.created.queue`
4. Ver: Mensajes procesados

### Ver Logs

**Order Service** (publica evento):
```
INFO - Publishing order created event: orderNumber=ORD-...
INFO - Order created event published successfully
```

**Notification Service** (consume evento):
```
INFO - Received order created event: orderNumber=ORD-...
INFO - Preparing order created notification
INFO - Email sent successfully to: cliente@email.com
INFO - Order created notification processed successfully
```

### Ver Email

Revisa tu bandeja de entrada (o spam) - deberías recibir un email HTML con:
- ✅ Número de orden
- ✅ Total a pagar
- ✅ Dirección de entrega
- ✅ Diseño profesional con CSS

---

## 🎯 Flujo Completo

```
Usuario crea orden
    ↓ POST /api/v1/orders
┌─────────────────────┐
│   Order Service     │
│   - Valida cliente  │
│   - Valida stock    │
│   - Guarda orden    │
│   - Reserva stock   │
│   - Publica evento ⚡│ ← NO BLOQUEA (2ms)
└─────────────────────┘
    ↓
Usuario recibe HTTP 200 OK (150ms total) ✅

[En paralelo, asíncrono]

┌─────────────────────┐
│     RabbitMQ        │
│  order.created.queue│
└─────────┬───────────┘
          │
          ↓ Listener activo
┌─────────────────────┐
│ Notification Service│
│ - Recibe evento     │
│ - Genera HTML       │
│ - Envía email       │
└─────────────────────┘
    ↓
📧 Email enviado (3-5 seg después) ✅
```

---

## 🧪 Casos de Prueba

### Caso 1: Email Exitoso
```bash
# Orden válida con datos completos
curl -X POST http://localhost:8085/api/v1/orders ...
# Resultado: HTTP 200 + Email recibido
```

### Caso 2: RabbitMQ Caído
```bash
# Detener RabbitMQ
docker-compose stop rabbitmq

# Crear orden
curl -X POST http://localhost:8085/api/v1/orders ...
# Resultado: HTTP 200 (orden creada)
# Email: NO enviado (evento no publicado)
# Orden: ✅ Creada correctamente

# Reiniciar RabbitMQ
docker-compose start rabbitmq
# Eventos perdidos NO se recuperan
```

### Caso 3: Notification Service Caído
```bash
# Detener Notification Service (Ctrl+C)

# Crear orden
curl -X POST http://localhost:8085/api/v1/orders ...
# Resultado: HTTP 200 (orden creada)
# RabbitMQ: ✅ Evento en cola esperando
# Email: ⏳ Pendiente

# Reiniciar Notification Service
./gradlew bootRun
# Email: ✅ Se envía automáticamente al iniciar
```

### Caso 4: Gmail Mal Configurado
```bash
# Credenciales incorrectas
$env:MAIL_PASSWORD="contraseña-incorrecta"
./gradlew bootRun

# Crear orden
curl -X POST http://localhost:8085/api/v1/orders ...
# Resultado: HTTP 200 (orden creada)
# Email: ❌ Falla al enviar
# Notification: Status=FAILED en BD
# RabbitMQ: Reintenta automáticamente
```

---

## 📊 Monitoreo

### RabbitMQ Management UI

**Queues**: http://localhost:15672/#/queues
- Total mensajes
- Rate de procesamiento
- Consumidores activos

**Exchanges**: http://localhost:15672/#/exchanges
- `order.exchange` (Topic)
- Bindings configurados

### Base de Datos

```sql
-- Ver notificaciones recientes
SELECT
    id,
    type,
    recipient_email,
    status,
    created_at,
    sent_at
FROM notifications
ORDER BY created_at DESC
LIMIT 10;

-- Tasa de éxito
SELECT
    status,
    COUNT(*) as count,
    ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER(), 2) as percentage
FROM notifications
GROUP BY status;
```

---

## 🛠️ Troubleshooting

### Problema: "Connection refused" en RabbitMQ

**Síntomas**:
```
Failed to publish order created event
ConnectException: Connection refused
```

**Solución**:
```bash
# Verificar que RabbitMQ está corriendo
docker ps | grep rabbitmq

# Si no está, iniciar
docker-compose up -d rabbitmq

# Esperar 10 segundos
sleep 10
```

### Problema: Email no se envía

**Diagnóstico**:
```bash
# Ver logs de Notification Service
# Buscar: "Email sent successfully" o "Failed to send email"
```

**Causas comunes**:
1. ❌ Credenciales Gmail incorrectas
   - Solución: Regenerar app password

2. ❌ Puerto 587 bloqueado
   - Solución: Verificar firewall

3. ❌ Email en spam
   - Solución: Revisar carpeta spam

### Problema: Evento no llega a Notification Service

**Diagnóstico**:
```bash
# 1. Ver RabbitMQ UI
http://localhost:15672/#/queues

# 2. Verificar orden creada
curl http://localhost:8085/api/v1/orders/1

# 3. Ver si hay consumidor activo
# En RabbitMQ UI: Queues → order.created.queue → Consumers
```

**Solución**: Reiniciar Notification Service

---

## 🎓 Conceptos Clave

### Publisher (Order Service)
```java
// Publica y olvida (fire and forget)
orderEventPublisher.publishOrderCreatedEvent(event);
// Continúa inmediatamente - no espera
```

### Consumer (Notification Service)
```java
@RabbitListener(queues = "order.created.queue")
public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    // Procesa evento asíncronamente
    // Si falla, RabbitMQ reintenta automáticamente
}
```

### Ventajas
- ⚡ **Performance**: Usuario no espera
- 🛡️ **Fault Tolerance**: Eventos en cola persistente
- 📈 **Scalability**: Múltiples workers
- 🔌 **Decoupling**: Servicios independientes

### Desventajas
- ⚠️ **Eventual Consistency**: Email llega después
- 🔧 **Complexity**: Más componentes que gestionar
- 💾 **Infrastructure**: Requiere RabbitMQ server

---

## 📚 Referencias

- [RabbitMQ Docs](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP](https://spring.io/projects/spring-amqp)
- [Gmail SMTP](https://support.google.com/mail/answer/7126229)

---

## ✅ Checklist de Validación

- [ ] RabbitMQ Management UI accesible (localhost:15672)
- [ ] Config Server corriendo (puerto 8889)
- [ ] Order Service corriendo (puerto 8085)
- [ ] Notification Service corriendo (puerto 8087)
- [ ] Gmail configurado con app password
- [ ] Crear orden retorna HTTP 200 rápidamente
- [ ] Email recibido en bandeja (revisar spam)
- [ ] Evento visible en RabbitMQ UI
- [ ] Notificación en BD con status=SENT

---

**Tiempo total de setup**: ~5 minutos
**Tiempo de respuesta**: 100-200ms
**Email delivery**: 3-5 segundos

🎉 **¡Sistema asíncrono funcionando!**

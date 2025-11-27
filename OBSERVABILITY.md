# Observabilidad en Arka Microservicios

Este documento explica cómo monitorear y observar el estado de los microservicios de Arka usando **Prometheus** y **Grafana**.

## 📊 Stack de Observabilidad

- **Prometheus**: Sistema de monitoreo y base de datos de series temporales
- **Grafana**: Plataforma de visualización y análisis de métricas
- **Spring Boot Actuator**: Expone métricas de las aplicaciones
- **Micrometer**: Biblioteca de métricas para aplicaciones Java

## 🚀 Inicio Rápido

### Desarrollo Local

1. **Iniciar infraestructura de monitoreo**:
```bash
cd arka-microservicios
docker-compose up -d prometheus grafana
```

2. **Iniciar microservicios** (en otra terminal):
```bash
# Config Server
cd config-server && ./gradlew bootRun

# Eureka Server
cd eureka-server && ./gradlew bootRun

# Servicios de negocio
cd product-service && ./gradlew bootRun
cd inventory-service && ./gradlew bootRun
cd order-service && ./gradlew bootRun
# ... otros servicios
```

3. **Acceder a las interfaces**:
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Dashboard principal**: Se carga automáticamente

### Producción (AWS/Railway)

En producción, Prometheus está configurado para scrappear métricas de:
- **AWS EC2**: Config Server, Eureka Server
- **Railway**: Todos los servicios de negocio (HTTPS)

Ver [prometheus.yml](arka-microservicios/prometheus/prometheus.yml) para configuración de producción.

## 📈 Métricas Disponibles

### Métricas de Infraestructura

Cada microservicio expone métricas en `/actuator/prometheus`:

#### HTTP/REST
- `http_server_requests_seconds_count`: Total de requests HTTP
- `http_server_requests_seconds_sum`: Tiempo total de procesamiento
- `http_server_requests_seconds_max`: Tiempo máximo de respuesta
- **Labels**: `method`, `uri`, `status`, `outcome`

#### JVM
- `jvm_memory_used_bytes`: Memoria JVM usada (heap/non-heap)
- `jvm_memory_max_bytes`: Memoria JVM máxima
- `jvm_gc_pause_seconds`: Tiempo de pausa de GC
- `jvm_threads_live`: Threads activos
- `process_cpu_usage`: Uso de CPU del proceso

#### Database
- `hikaricp_connections_active`: Conexiones DB activas
- `hikaricp_connections_idle`: Conexiones DB idle
- `hikaricp_connections_max`: Conexiones DB máximas
- `hikaricp_connections_pending`: Conexiones DB pendientes

#### Sistema
- `system_cpu_usage`: Uso de CPU del sistema
- `system_load_average_1m`: Load average 1 minuto
- `process_uptime_seconds`: Tiempo de ejecución

### Métricas de Negocio (Custom)

Puedes agregar métricas personalizadas usando Micrometer:

```java
@Service
public class OrderService {
    private final Counter ordersCreated;
    private final Timer orderProcessingTime;

    public OrderService(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(registry);

        this.orderProcessingTime = Timer.builder("orders.processing.time")
            .description("Order processing time")
            .register(registry);
    }

    public void createOrder(OrderRequest request) {
        orderProcessingTime.record(() -> {
            // Lógica de creación
            ordersCreated.increment();
        });
    }
}
```

## 📊 Dashboards de Grafana

### Dashboard Principal: "Arka Microservices - Overview"

**UID**: `arka-microservices-overview`

El dashboard se carga automáticamente y contiene 9 paneles:

1. **Total Services**: Número total de microservicios
2. **Services UP**: Servicios funcionando correctamente
3. **Services DOWN**: Servicios caídos o no disponibles
4. **Request Rate by Service**: Requests por segundo (RPS) por servicio
5. **Average Response Time**: Tiempo promedio de respuesta (ms)
6. **CPU Usage by Service**: Uso de CPU por servicio (%)
7. **JVM Memory Usage (Heap)**: Memoria heap usada por servicio
8. **Database Connection Pool**: Conexiones activas vs idle
9. **Service Status Table**: Tabla con estado de cada servicio

### Queries PromQL Útiles

**Request Rate (últimos 5 minutos)**:
```promql
rate(http_server_requests_seconds_count{job=~".*-service"}[5m])
```

**Tiempo promedio de respuesta**:
```promql
rate(http_server_requests_seconds_sum{job=~".*-service"}[5m])
/
rate(http_server_requests_seconds_count{job=~".*-service"}[5m])
* 1000
```

**Errores HTTP (5xx)**:
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

**Uso de memoria heap (%)**:
```promql
100 * jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

**Tasa de éxito (%)**:
```promql
100 * (
  sum(rate(http_server_requests_seconds_count{status!~"5.."}[5m]))
  /
  sum(rate(http_server_requests_seconds_count[5m]))
)
```

## 🔍 Verificación de Salud

### Health Checks de Spring Boot Actuator

Cada servicio expone `/actuator/health`:

```bash
curl http://localhost:8082/actuator/health
```

**Respuesta típica**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 200107862016,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Endpoints de Actuator Disponibles

- `/actuator/health`: Estado de salud del servicio
- `/actuator/info`: Información del servicio
- `/actuator/prometheus`: Métricas en formato Prometheus
- `/actuator/metrics`: Lista de métricas disponibles
- `/actuator/metrics/{metricName}`: Detalle de una métrica específica

**Ejemplo**:
```bash
# Ver métrica específica
curl http://localhost:8082/actuator/metrics/jvm.memory.used

# Respuesta
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 157286400
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    }
  ]
}
```

## 🏗️ Arquitectura de Monitoreo

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Services   │────▶│ Prometheus  │────▶│   Grafana   │
│  (8080-88)  │     │   (9090)    │     │   (3000)    │
└─────────────┘     └─────────────┘     └─────────────┘
      │                    │
      │                    │
      ▼                    ▼
/actuator/prometheus   Scraping cada 15s
                       (local) / 30s (prod)
```

### Flujo de Datos

1. **Servicios** exponen métricas en `/actuator/prometheus`
2. **Prometheus** scrapea métricas cada 15-30 segundos
3. **Prometheus** almacena métricas en base de datos de series temporales
4. **Grafana** consulta Prometheus y visualiza en dashboards
5. **Alertas** (opcional) se envían cuando se cumplen condiciones

## 🔧 Configuración

### Prometheus

**Local** ([prometheus-local.yml](arka-microservicios/prometheus/prometheus-local.yml)):
- Scraping cada 15 segundos
- Targets: `host.docker.internal:808x`
- Cluster: `arka-local`

**Producción** ([prometheus.yml](arka-microservicios/prometheus/prometheus.yml)):
- Scraping cada 30 segundos
- Targets: URLs de Railway (HTTPS) y AWS EC2
- Cluster: `arka-production`
- Variables de entorno: `${API_GATEWAY_URL}`, etc.

### Grafana

**Provisioning automático**:
- **Datasource**: [datasources/prometheus.yml](arka-microservicios/grafana/provisioning/datasources/prometheus.yml)
- **Dashboards**: [dashboards/dashboard-config.yml](arka-microservicios/grafana/provisioning/dashboards/dashboard-config.yml)
- **Dashboard JSON**: [arka-microservices-overview.json](arka-microservicios/grafana/dashboards/arka-microservices-overview.json)

**Credenciales por defecto**:
- Usuario: `admin`
- Password: `admin`

## 🎯 Casos de Uso

### 1. Detectar Servicio Caído

**Prometheus Query**:
```promql
up{job=~".*-service"} == 0
```

**Dashboard**: Panel "Services DOWN" (debe ser 0 en estado saludable)

### 2. Identificar Cuellos de Botella

**Grafana**: Panel "Average Response Time"
- Si un servicio tiene tiempos >500ms, revisar:
  - Queries de base de datos
  - Llamadas a servicios externos
  - Garbage Collection

### 3. Monitorear Uso de Recursos

**CPU alto (>80%)**:
```promql
process_cpu_usage{job=~".*-service"} * 100 > 80
```

**Memoria heap alta (>85%)**:
```promql
100 * jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 85
```

### 4. Analizar Patrones de Tráfico

**Requests por minuto**:
```promql
sum(rate(http_server_requests_seconds_count[1m])) * 60
```

**Top 5 endpoints más lentos**:
```promql
topk(5,
  rate(http_server_requests_seconds_sum[5m])
  /
  rate(http_server_requests_seconds_count[5m])
)
```

## 🚨 Alertas (Próximamente)

Para implementar alertas, agregar en `prometheus.yml`:

```yaml
alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

rule_files:
  - "alert_rules.yml"
```

**Ejemplo de regla de alerta** (`alert_rules.yml`):

```yaml
groups:
  - name: arka_alerts
    interval: 30s
    rules:
      - alert: ServiceDown
        expr: up{job=~".*-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.job }} is down"

      - alert: HighResponseTime
        expr: |
          rate(http_server_requests_seconds_sum[5m])
          /
          rate(http_server_requests_seconds_count[5m])
          > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time on {{ $labels.service }}"

      - alert: HighMemoryUsage
        expr: |
          100 * jvm_memory_used_bytes{area="heap"}
          /
          jvm_memory_max_bytes{area="heap"}
          > 90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.service }}"
```

## 📚 Recursos Adicionales

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [PromQL Tutorial](https://prometheus.io/docs/prometheus/latest/querying/basics/)

## 🔐 Seguridad

### Producción

En producción, considerar:

1. **Autenticación en Grafana**:
   - Cambiar password por defecto
   - Integrar con OAuth/LDAP
   - Configurar roles y permisos

2. **Seguridad en Prometheus**:
   - Exponer solo internamente (no public)
   - Usar autenticación básica o proxy reverso
   - Limitar acceso por IP

3. **Actuator Endpoints**:
   - Restringir acceso a `/actuator/*`
   - Usar Spring Security para proteger endpoints
   - Configurar `management.endpoints.web.exposure.include`

**Ejemplo** (`application.yml`):
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

## 🛠️ Troubleshooting

### Prometheus no muestra servicios

1. **Verificar targets**: http://localhost:9090/targets
2. **Estado debe ser**: `UP`
3. **Si está DOWN**:
   - Verificar que el servicio esté corriendo
   - Verificar `/actuator/prometheus` sea accesible
   - Revisar configuración de red (firewall, docker network)

### Grafana no muestra datos

1. **Verificar datasource**: Configuration → Data Sources → Prometheus
2. **Test connection** debe ser exitoso
3. **Verificar dashboard queries**: Edit panel → Query inspector
4. **Revisar rango de tiempo**: Ajustar time range en dashboard

### Métricas faltantes

1. **Verificar dependencias** en `build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
runtimeOnly 'io.micrometer:micrometer-registry-prometheus'
```

2. **Verificar configuración** en `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'
  metrics:
    export:
      prometheus:
        enabled: true
```

3. **Reiniciar servicio** y verificar logs

---

**Última actualización**: 2025-11-18
**Versión**: 1.0.0
**Autor**: Equipo Arka
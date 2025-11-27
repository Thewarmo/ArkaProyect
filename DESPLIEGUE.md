# Arka Microservicios - Guía de Despliegue

## Tabla de Contenidos
1. [Resumen de Arquitectura](#resumen-de-arquitectura)
2. [Requisitos Previos](#requisitos-previos)
3. [Fase 1: Configuración de Servicios Externos](#fase-1-configuración-de-servicios-externos)
4. [Fase 2: Configuración de AWS EC2](#fase-2-configuración-de-aws-ec2)
5. [Fase 3: Despliegue en Railway](#fase-3-despliegue-en-railway)
6. [Fase 4: Configuración de CI/CD](#fase-4-configuración-de-cicd)
7. [Monitoreo y Operaciones](#monitoreo-y-operaciones)
8. [Resolución de Problemas](#resolución-de-problemas)

---

## Resumen de Arquitectura

### Estrategia de Despliegue Híbrido

**AWS Free Tier (EC2 t2.micro - 1GB RAM)**:
- Config Server (puerto 8889)
- Eureka Server (puerto 8761)
- Prometheus (puerto 9090)
- Nginx Reverse Proxy (puerto 80/443)

**Plataforma Railway**:
- API Gateway (puerto 8090)
- Auth Server (puerto 8082)
- Product Service (puerto 8081)
- Customer Service (puerto 8083)
- Inventory Service (puerto 8084)
- Order Service (puerto 8085)
- Cart Service (puerto 8086)
- Notification Service (puerto 8087)
- Report Service (puerto 8088)
- Base de Datos PostgreSQL (ya configurada)

**Servicios Externos Gratuitos**:
- MongoDB Atlas M0 (512MB capa gratuita)
- CloudAMQP Lemur (RabbitMQ gratuito)
- Grafana Cloud (monitoreo gratuito)

### Flujo de Comunicación de Servicios

```
Usuario → Railway API Gateway → Auth Server (JWT)
                                → Servicios de Negocio

Servicios de Negocio → AWS Eureka (descubrimiento de servicios)
                      → AWS Config Server (configuración)
                      → Railway PostgreSQL (datos)
                      → MongoDB Atlas (datos del carrito)
                      → CloudAMQP (mensajería asíncrona)
```

---

## Requisitos Previos

### Cuentas Requeridas
- [ ] Cuenta AWS (elegible para capa gratuita)
- [ ] Cuenta Railway
- [ ] Cuenta MongoDB Atlas
- [ ] Cuenta CloudAMQP
- [ ] Cuenta Docker Hub
- [ ] Cuenta GitHub
- [ ] Cuenta Grafana Cloud (opcional)

### Herramientas Locales
- [ ] Docker Desktop instalado
- [ ] Git instalado
- [ ] AWS CLI instalado (opcional)
- [ ] Railway CLI: `npm install -g @railway/cli`

---

## Fase 1: Configuración de Servicios Externos

### 1.1 Configuración de MongoDB Atlas

1. **Crear Cuenta en MongoDB Atlas**: https://www.mongodb.com/cloud/atlas/register

2. **Crear Cluster M0 Gratuito**:
   ```
   - Proveedor: AWS
   - Región: Elegir la más cercana a Railway/AWS (ej. us-east-1)
   - Tier del Cluster: M0 Sandbox (Gratis para Siempre)
   - Nombre del Cluster: arka-cluster
   ```

3. **Crear Usuario de Base de Datos**:
   ```
   - Usuario: arka_admin
   - Contraseña: <generar-contraseña-fuerte>
   - Rol: Lectura y escritura en cualquier base de datos
   ```

4. **Configurar Acceso de Red**:
   ```
   - Agregar Dirección IP: 0.0.0.0/0 (permitir desde cualquier lugar)
   - Comentario: Acceso Railway + AWS
   ```

5. **Obtener Cadena de Conexión**:
   ```
   mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts?retryWrites=true&w=majority
   ```

6. **Guardar para más tarde**: Necesitarás esto como variable de entorno `MONGODB_URI`

### 1.2 Configuración de CloudAMQP

1. **Crear Cuenta CloudAMQP**: https://customer.cloudamqp.com/signup

2. **Crear Instancia Lemur**:
   ```
   - Plan: Lemur (Gratis)
   - Nombre: arka-rabbitmq
   - Región: Elegir la más cercana a Railway/AWS
   - Etiquetas: production
   ```

3. **Obtener URL AMQP**:
   ```
   - Ir a detalles de la instancia
   - Copiar URL AMQP: amqps://user:pass@host/vhost
   ```

4. **Guardar para más tarde**: Necesitarás esto como variable de entorno `CLOUDAMQP_URL`

### 1.3 Configuración de Grafana Cloud (Opcional)

1. **Crear Cuenta Grafana Cloud**: https://grafana.com/auth/sign-up/create-user

2. **Crear Stack Gratuito**:
   ```
   - Nombre del stack: arka-monitoring
   - Región: Elegir la región más cercana
   ```

3. **Configurar Data Source de Prometheus**:
   ```
   - Ir a Connections → Data Sources
   - Agregar data source Prometheus
   - URL: http://<AWS_EC2_PUBLIC_IP>:9090
   - Guardar y Probar
   ```

4. **Importar Dashboard de Spring Boot**:
   ```
   - Dashboard ID: 4701 (JVM Micrometer)
   - O ID: 12900 (Spring Boot 2.1 Statistics)
   ```

---

## Fase 2: Configuración de AWS EC2

### 2.1 Lanzar Instancia EC2

1. **Iniciar Sesión en AWS Console**: https://console.aws.amazon.com/

2. **Lanzar Instancia**:
   ```
   - AMI: Amazon Linux 2023
   - Tipo de Instancia: t2.micro (Elegible para capa gratuita)
   - Par de Claves: Crear nuevo o usar existente
   - Configuración de Red:
     - Auto-asignar IP Pública: Habilitar
     - Grupo de Seguridad: Crear nuevo
   - Almacenamiento: 30GB gp3 (elegible capa gratuita)
   - Etiquetas: Name=arka-infrastructure
   ```

3. **Configurar Grupo de Seguridad**:
   ```
   Reglas de Entrada:
   - SSH (22): Mi IP (tu IP actual)
   - HTTP (80): 0.0.0.0/0
   - HTTPS (443): 0.0.0.0/0
   - TCP Personalizado (8761): 0.0.0.0/0 (Eureka)
   - TCP Personalizado (8889): 0.0.0.0/0 (Config Server)
   - TCP Personalizado (9090): Mi IP (Prometheus - restringido)

   Reglas de Salida:
   - Todo el tráfico: 0.0.0.0/0
   ```

4. **Asignar IP Elástica** (opcional pero recomendado):
   ```
   - EC2 → IPs Elásticas → Asignar IP Elástica
   - Asociar con tu instancia
   - Nota: Gratis si está adjunta a una instancia en ejecución
   ```

### 2.2 Conectar a la Instancia EC2

```bash
# Establecer permisos en el archivo de clave
chmod 400 tu-clave.pem

# Conectar vía SSH
ssh -i tu-clave.pem ec2-user@<IP_PUBLICA>
```

### 2.3 Instalar Docker en EC2

```bash
# Actualizar sistema
sudo yum update -y

# Instalar Docker
sudo yum install docker -y

# Iniciar servicio Docker
sudo systemctl start docker
sudo systemctl enable docker

# Agregar ec2-user al grupo docker
sudo usermod -a -G docker ec2-user

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker --version
docker-compose --version

# Salir y volver a entrar para que los cambios de grupo surtan efecto
exit
```

### 2.4 Clonar Repositorio de Infraestructura

```bash
# Reconectar a EC2
ssh -i tu-clave.pem ec2-user@<IP_PUBLICA>

# Instalar Git
sudo yum install git -y

# Clonar repositorio
mkdir -p ~/arka-infrastructure
cd ~/arka-infrastructure

# Clonar tu repositorio (actualizar URL)
git clone https://github.com/tuusuario/ProyectoArkaAceleraTi.git .
```

### 2.5 Crear Archivo de Entorno

```bash
cd ~/arka-infrastructure/arka-microservicios

# Crear archivo .env para Docker Compose
cat > .env << 'EOF'
DOCKER_REGISTRY=tuusuariodockerhub
TAG=latest
SPRING_PROFILES_ACTIVE=prod
CONFIG_GIT_URI=https://github.com/tuusuario/arka-config-repo
CONFIG_GIT_BRANCH=main

# URLs de servicios Railway (actualizar después del despliegue en Railway)
API_GATEWAY_URL=api-gateway-production.up.railway.app
AUTH_SERVER_URL=auth-server-production.up.railway.app
PRODUCT_SERVICE_URL=product-service-production.up.railway.app
CUSTOMER_SERVICE_URL=customer-service-production.up.railway.app
INVENTORY_SERVICE_URL=inventory-service-production.up.railway.app
ORDER_SERVICE_URL=order-service-production.up.railway.app
CART_SERVICE_URL=cart-service-production.up.railway.app
NOTIFICATION_SERVICE_URL=notification-service-production.up.railway.app
REPORT_SERVICE_URL=report-service-production.up.railway.app
EOF
```

### 2.6 Actualizar Configuración de Prometheus

```bash
# Actualizar prometheus/prometheus.yml con las URLs reales de Railway
cd ~/arka-infrastructure/arka-microservicios/prometheus
nano prometheus.yml

# Reemplazar los placeholders ${API_GATEWAY_URL} con las URLs reales de Railway del .env
```

### 2.7 Desplegar Servicios de Infraestructura

```bash
cd ~/arka-infrastructure/arka-microservicios

# Descargar imágenes Docker
docker-compose -f docker-compose-aws.yml pull

# Iniciar servicios
docker-compose -f docker-compose-aws.yml up -d

# Ver logs
docker-compose -f docker-compose-aws.yml logs -f

# Verificar que los servicios estén ejecutándose
docker-compose -f docker-compose-aws.yml ps

# Verificar salud
curl http://localhost:8889/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Eureka Server
```

### 2.8 Verificar Dashboard de Eureka

Abrir navegador: `http://<IP_PUBLICA_EC2>:8761`

Deberías ver el dashboard de Eureka Server (aún sin servicios registrados).

---

## Fase 3: Despliegue en Railway

### 3.1 Instalar Railway CLI

```bash
# En tu máquina local
npm install -g @railway/cli

# Iniciar sesión en Railway
railway login
```

### 3.2 Crear Proyecto en Railway

```bash
# Crear nuevo proyecto
railway init

# Vincular a proyecto existente (si ya fue creado)
railway link
```

### 3.3 Desplegar Cada Servicio

Para cada servicio (api-gateway, auth-server, product-service, etc.):

```bash
cd arka-microservicios/<nombre-servicio>

# Crear servicio en Railway
railway service create <nombre-servicio>

# Establecer variables de entorno
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set EUREKA_SERVER_URL=http://<IP_PUBLICA_EC2>:8761/eureka/
railway variables set SPRING_CLOUD_CONFIG_URI=http://<IP_PUBLICA_EC2>:8889

# Para servicios PostgreSQL (todos excepto cart-service)
railway variables set SPRING_DATASOURCE_URL=jdbc:postgresql://centerbeam.proxy.rlwy.net:34241/railway?sslmode=require
railway variables set SPRING_DATASOURCE_USERNAME=postgres
railway variables set SPRING_DATASOURCE_PASSWORD=HMroWPVhjEVmVdDIPhhPABBtEbBKirFd

# Solo para cart-service
railway variables set MONGODB_URI=mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts

# Para order-service y notification-service
railway variables set CLOUDAMQP_URL=amqps://user:pass@host/vhost

# Para notification-service email
railway variables set MAIL_USERNAME=tu-email@gmail.com
railway variables set MAIL_PASSWORD=tu-contraseña-app

# Desplegar servicio
railway up --service <nombre-servicio>

# Obtener URL del servicio
railway domain
```

### 3.4 Configurar Dominios de Railway

Después del despliegue, Railway asigna dominios. Guárdalos para monitoreo:

```
api-gateway: https://api-gateway-production.up.railway.app
auth-server: https://auth-server-production.up.railway.app
product-service: https://product-service-production.up.railway.app
...
```

### 3.5 Verificar Registro de Servicios

Espera 1-2 minutos, luego verifica el dashboard de Eureka:

```
http://<IP_PUBLICA_EC2>:8761
```

Los 9 servicios de Railway deberían aparecer como "UP" en Eureka.

---

## Fase 4: Configuración de CI/CD

### 4.1 Configuración de Secrets de GitHub

Ir a Repositorio GitHub → Settings → Secrets and variables → Actions

Agregar los siguientes secrets:

```
DOCKER_USERNAME=tuusuariodockerhub
DOCKER_PASSWORD=tucontraseñadocker

AWS_EC2_HOST=<IP_PUBLICA_EC2>
AWS_EC2_USERNAME=ec2-user
AWS_EC2_SSH_KEY=<pegar-contenido-clave-privada>

RAILWAY_TOKEN=<obtener-de-railway-cli>
RAILWAY_API_GATEWAY_URL=https://api-gateway-production.up.railway.app
```

Para obtener el token de Railway:
```bash
railway whoami
# Copiar el token de ~/.railway/config.json
```

### 4.2 Probar Pipeline CI/CD

```bash
# Hacer un pequeño cambio
echo "# Test" >> README.md

# Hacer commit y push
git add .
git commit -m "test: activar pipeline CI/CD"
git push origin master

# Monitorear GitHub Actions
# Ir a: https://github.com/tuusuario/repo/actions
```

---

## Monitoreo y Operaciones

### Puntos de Acceso

| Servicio | URL | Notas |
|---------|-----|-------|
| Dashboard Eureka | `http://<IP_EC2>:8761` | Registro de servicios |
| Config Server | `http://<IP_EC2>:8889` | Configuración |
| Prometheus | `http://<IP_EC2>:9090` | Métricas |
| API Gateway | `https://<dominio-railway>` | API Pública |
| Grafana Cloud | `https://<tu-stack>.grafana.net` | Dashboards |

### Comandos Útiles

**Verificar Estado de Servicios EC2**:
```bash
ssh -i tu-clave.pem ec2-user@<IP_EC2>
cd ~/arka-infrastructure/arka-microservicios
docker-compose -f docker-compose-aws.yml ps
docker-compose -f docker-compose-aws.yml logs -f config-server
```

**Verificar Logs de Servicios Railway**:
```bash
railway logs --service api-gateway
railway logs --service product-service --tail 100
```

**Reiniciar Servicios**:
```bash
# AWS EC2
docker-compose -f docker-compose-aws.yml restart config-server

# Railway
railway service restart api-gateway
```

### Monitorear Métricas

**Consultas Prometheus**:
```promql
# Tasa de peticiones por servicio
rate(http_server_requests_seconds_count[5m])

# Tasa de errores
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Uso de memoria
jvm_memory_used_bytes / jvm_memory_max_bytes

# Uso de CPU
process_cpu_usage
```

---

## Resolución de Problemas

### Los Servicios No se Registran en Eureka

**Problema**: Los servicios desplegados en Railway no aparecen en el dashboard de Eureka.

**Soluciones**:
1. Verificar variable de entorno:
   ```bash
   railway variables get EUREKA_SERVER_URL
   # Debería ser: http://<IP_PUBLICA_EC2>:8761/eureka/
   ```

2. Verificar que el grupo de seguridad EC2 permita entrada en el puerto 8761 desde 0.0.0.0/0

3. Verificar logs del servicio:
   ```bash
   railway logs --service product-service | grep -i eureka
   ```

### Fallos de Conexión a MongoDB

**Problema**: El servicio de carrito no puede conectarse a MongoDB Atlas.

**Soluciones**:
1. Verificar que el acceso de red de MongoDB Atlas permite 0.0.0.0/0

2. Verificar cadena de conexión:
   ```bash
   railway variables get MONGODB_URI
   ```

3. Probar conexión manualmente:
   ```bash
   mongosh "mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts"
   ```

### Problemas de Conexión a RabbitMQ

**Problema**: Los servicios Order/Notification no pueden conectarse a CloudAMQP.

**Soluciones**:
1. Verificar formato de URL CloudAMQP:
   ```
   amqps:// (con 's' para SSL)
   ```

2. Verificar variable de entorno:
   ```bash
   railway variables get CLOUDAMQP_URL
   ```

### Uso Alto de Memoria en EC2

**Problema**: La instancia EC2 se queda sin memoria.

**Soluciones**:
1. Verificar estadísticas de Docker:
   ```bash
   docker stats
   ```

2. Reducir tamaños de heap JVM en docker-compose-aws.yml:
   ```yaml
   environment:
     JAVA_OPTS: "-Xmx150m -Xms100m"  # Reducir de 200m
   ```

3. Detener Prometheus si no es necesario:
   ```bash
   docker-compose -f docker-compose-aws.yml stop prometheus
   ```

4. Agregar espacio swap:
   ```bash
   sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   ```

---

## Gestión de Costos

### Estimación de Costos Mensuales

**AWS Free Tier (Año 1)**:
- EC2 t2.micro: $0
- EBS 30GB: $0
- Transferencia de datos: $0 (dentro de límites)
- **Total**: $0/mes

**Railway**:
- Uso estimado: $20-30/mes
- Configurar alertas de uso en $25

**Servicios Externos**:
- MongoDB Atlas M0: $0
- CloudAMQP Lemur: $0
- Grafana Cloud Free: $0

**Costo Mensual Total**: $20-30/mes

### Consejos de Optimización de Costos

1. **Usar red privada de Railway** (sin cargos de salida):
   ```
   nombre-servicio.railway.internal
   ```

2. **Establecer límites estrictos de memoria**:
   ```bash
   railway service update --memory 512
   ```

3. **Monitorear uso semanalmente**:
   ```bash
   railway metrics
   ```

4. **Reducir escala de servicios no productivos**

5. **Usar AWS Free Tier eficientemente** (expira después de 12 meses)

---

## Recuperación ante Desastres

### Estrategia de Respaldos

**PostgreSQL (Railway)**:
- Respaldos automáticos de Railway: 7 días de retención
- Respaldo manual:
  ```bash
  railway db backup create
  ```

**MongoDB Atlas**:
- Respaldos en la nube habilitados por defecto (tier M0)
- Exportar manualmente si es necesario:
  ```bash
  mongodump --uri="mongodb+srv://..."
  ```

**Repositorio de Configuración**:
- Ya está en Git (control de versiones)
- Crear rama de respaldo mensualmente

**Snapshots de EC2**:
```bash
# Crear snapshot EBS
aws ec2 create-snapshot \
  --volume-id vol-xxxxx \
  --description "arka-infrastructure-backup-$(date +%Y%m%d)"
```

---

## Lista de Verificación de Seguridad

- [ ] Autenticación SSH solo con clave (sin contraseña)
- [ ] Grupos de seguridad restringen puertos innecesarios
- [ ] Contraseñas de base de datos almacenadas en variables de entorno
- [ ] SSL habilitado para conexiones PostgreSQL
- [ ] Variables de entorno de Railway encriptadas
- [ ] Acceso de red MongoDB Atlas configurado
- [ ] Actualizaciones de seguridad regulares en EC2: `sudo yum update`
- [ ] Imágenes Docker usan usuarios no root
- [ ] Secrets no comprometidos en Git

---

**Última Actualización**: 2025-01-07

# Arka Microservices - Guía Rápida de Despliegue

Esta guía te llevará desde cero hasta tener todos los microservicios corriendo en AWS EC2 en menos de 30 minutos.

## 📋 Prerequisitos

- Cuenta AWS (free tier)
- Cuenta Railway (PostgreSQL gratis)
- Cuenta MongoDB Atlas (gratis)
- Cuenta CloudAMQP (RabbitMQ gratis)
- Cuenta GitHub
- Cuenta Gmail (para notificaciones)

---

## 🚀 Paso 1: Configurar Bases de Datos Externas (10 minutos)

### Railway PostgreSQL

1. Ir a [railway.app](https://railway.app)
2. Crear nuevo proyecto → Add PostgreSQL
3. Copiar la URL de conexión:
   ```
   postgresql://postgres:password@host:port/railway
   ```
4. Guardar para después

### MongoDB Atlas

1. Ir a [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
2. Crear cluster M0 (gratis)
3. Database Access → Add user (username: `arka_admin`)
4. Network Access → Add IP `0.0.0.0/0`
5. Connect → Get connection string:
   ```
   mongodb+srv://arka_admin:PASSWORD@cluster.mongodb.net/arka_carts
   ```

### CloudAMQP

1. Ir a [cloudamqp.com](https://www.cloudamqp.com)
2. Create instance → Lemur (free)
3. Copiar AMQP URL:
   ```
   amqps://user:pass@host/vhost
   ```

---

## 🖥️ Paso 2: Lanzar AWS EC2 Instance (5 minutos)

### 2.1 Crear Instancia

1. AWS Console → EC2 → Launch Instance
2. **AMI**: Amazon Linux 2023
3. **Instance Type**: t2.micro (free tier)
4. **Key Pair**: Create new (descargar .pem)
5. **Security Group**: Crear con estos puertos:

   | Tipo | Puerto | Source | Descripción |
   |------|--------|--------|-------------|
   | SSH | 22 | Mi IP | SSH access |
   | HTTP | 80 | 0.0.0.0/0 | Web traffic |
   | Custom TCP | 8761 | 0.0.0.0/0 | Eureka |
   | Custom TCP | 8080 | 0.0.0.0/0 | API Gateway |
   | Custom TCP | 9090 | Mi IP | Prometheus |
   | Custom TCP | 3000 | 0.0.0.0/0 | Grafana |

6. **Storage**: 30GB
7. Launch!

### 2.2 Conectar por SSH

```bash
chmod 400 your-key.pem
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>
```

---

## ⚙️ Paso 3: Setup Automático en EC2 (5 minutos)

```bash
# Descargar y ejecutar script de setup
wget https://raw.githubusercontent.com/TUUSUARIO/ProyectoArkaAceleraTi/main/scripts/aws-setup.sh
chmod +x aws-setup.sh
./aws-setup.sh
```

El script instalará:
- ✅ Docker
- ✅ Docker Compose
- ✅ 4GB de Swap
- ✅ Git y utilidades

**IMPORTANTE**: Después del script, logout y vuelve a conectar:
```bash
exit
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>
```

---

## 📝 Paso 4: Configurar Variables de Entorno (5 minutos)

```bash
cd ~/arka-deployment
nano .env
```

Edita el archivo con tus credenciales reales:

```env
# Docker Registry
DOCKER_REGISTRY=ghcr.io/TUUSUARIO
TAG=latest

# Git Config
CONFIG_GIT_URI=https://github.com/TUUSUARIO/arka-config-repo
CONFIG_GIT_BRANCH=main

# Railway PostgreSQL (copiar de Railway)
RAILWAY_POSTGRES_URL=jdbc:postgresql://centerbeam.proxy.rlwy.net:34241/railway?sslmode=require
RAILWAY_POSTGRES_USER=postgres
RAILWAY_POSTGRES_PASSWORD=TU_PASSWORD

# MongoDB Atlas (copiar de MongoDB Atlas)
MONGODB_ATLAS_URI=mongodb+srv://arka_admin:PASSWORD@cluster.mongodb.net/arka_carts?retryWrites=true&w=majority

# CloudAMQP (copiar de CloudAMQP)
CLOUDAMQP_URL=amqps://user:pass@host/vhost

# Email (Gmail App Password)
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

# JWT Secret (dejar como está o generar uno nuevo)
JWT_SECRET=ArkaSecretKeyForJWTTokenGenerationAndValidation2024!MustBeLongEnoughForHS256Algorithm

# Eureka (automático, no cambiar)
EUREKA_HOSTNAME=$(curl -s http://169.254.169.254/latest/meta-data/public-hostname)

# Grafana
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

Guardar: `Ctrl + O`, `Enter`, `Ctrl + X`

---

## 📦 Paso 5: Descargar Archivos de Despliegue (2 minutos)

```bash
cd ~/arka-deployment

# Clonar repositorio temporalmente
git clone https://github.com/TUUSUARIO/ProyectoArkaAceleraTi.git temp

# Copiar archivos necesarios
cp temp/arka-microservicios/docker-compose-production.yml .
cp -r temp/arka-microservicios/prometheus .
cp -r temp/arka-microservicios/grafana .
cp -r temp/arka-microservicios/nginx .

# Limpiar
rm -rf temp
```

---

## 🚢 Paso 6: Desplegar Servicios (3 minutos)

```bash
cd ~/arka-deployment

# Pull de imágenes Docker (primera vez puede tardar)
docker-compose -f docker-compose-production.yml pull

# Iniciar todos los servicios
docker-compose -f docker-compose-production.yml up -d

# Ver logs en tiempo real
docker-compose -f docker-compose-production.yml logs -f
```

Espera a ver mensajes como:
```
config-server    | Started ConfigServerApplication
eureka-server    | Started EurekaServerApplication
api-gateway      | Started ApiGatewayApplication
```

Presiona `Ctrl + C` para salir de los logs.

---

## ✅ Paso 7: Verificar Despliegue (1 minuto)

### Ver Estado de Servicios

```bash
cd ~/arka-deployment
docker-compose -f docker-compose-production.yml ps
```

Todos deben estar "Up (healthy)".

### Verificar URLs

Abre en tu navegador (reemplaza `<EC2_PUBLIC_IP>` con tu IP):

| Servicio | URL | Esperado |
|----------|-----|----------|
| **Eureka Dashboard** | `http://<EC2_IP>:8761` | Ver 9 servicios registrados |
| **API Gateway** | `http://<EC2_IP>:8080/actuator/health` | `{"status":"UP"}` |
| **Prometheus** | `http://<EC2_IP>:9090/targets` | Todos los targets UP |
| **Grafana** | `http://<EC2_IP>:3000` | Login (admin/admin) |

---

## 🛠️ Comandos Útiles

### Ver Logs de un Servicio

```bash
docker-compose -f docker-compose-production.yml logs -f [service-name]
```

### Reiniciar un Servicio

```bash
docker-compose -f docker-compose-production.yml restart [service-name]
```

### Ver Uso de Recursos

```bash
docker stats
```

### Monitoreo en Tiempo Real

```bash
~/scripts/aws-monitor.sh
```

### Troubleshooting

```bash
~/scripts/aws-troubleshoot.sh
```

---

## 🔧 Solución de Problemas Comunes

### Servicio no arranca

```bash
# Ver logs detallados
docker-compose -f docker-compose-production.yml logs [service-name]

# Verificar variables de entorno
cat .env

# Reiniciar servicio
docker-compose -f docker-compose-production.yml restart [service-name]
```

### Out of Memory

```bash
# Ver memoria
free -h

# Reiniciar servicios pesados
docker-compose -f docker-compose-production.yml restart prometheus grafana

# Última opción: reiniciar todo
docker-compose -f docker-compose-production.yml restart
```

### Servicios no aparecen en Eureka

```bash
# Esperar 30-60 segundos
# Verificar que Eureka esté UP primero
curl http://localhost:8761/actuator/health

# Ver logs de un servicio específico
docker-compose -f docker-compose-production.yml logs product-service | grep -i eureka
```

---

## 📊 Configurar CI/CD (Opcional)

### 1. GitHub Secrets

Repository → Settings → Secrets → Actions → New secret:

```
DOCKER_REGISTRY=ghcr.io/TUUSUARIO
AWS_EC2_HOST=<TU_EC2_IP>
AWS_EC2_USER=ec2-user
AWS_EC2_SSH_KEY=<contenido-de-tu-key.pem>

RAILWAY_POSTGRES_URL=jdbc:postgresql://...
RAILWAY_POSTGRES_USER=postgres
RAILWAY_POSTGRES_PASSWORD=...

MONGODB_ATLAS_URI=mongodb+srv://...
CLOUDAMQP_URL=amqps://...

MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

JWT_SECRET=tu-secret

CONFIG_GIT_URI=https://github.com/TUUSUARIO/arka-config-repo
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

### 2. Activar Workflows

Los workflows en `.github/workflows/` se ejecutarán automáticamente en cada push a `main`.

---

## 🎉 ¡Listo!

Tu sistema de microservicios está corriendo. Accede a:

- **API**: `http://<EC2_IP>:8080`
- **Dashboard**: `http://<EC2_IP>:8761`
- **Monitoreo**: `http://<EC2_IP>:3000`

### Próximos Pasos

1. ✅ Probar endpoints del API Gateway
2. ✅ Importar dashboard de Grafana
3. ✅ Configurar alertas en Prometheus
4. ✅ Agregar un dominio personalizado
5. ✅ Configurar HTTPS con Let's Encrypt

---

## 📚 Más Información

- [DEPLOYMENT.md](DEPLOYMENT.md) - Guía completa de despliegue
- [OBSERVABILITY.md](OBSERVABILITY.md) - Monitoreo y métricas
- [TESTING.md](TESTING.md) - Guía de testing
- [CI-CD.md](CI-CD.md) - Integración continua

---

**¿Problemas?** Abre un issue en GitHub o revisa la documentación completa.

**¡Happy coding! 🚀**
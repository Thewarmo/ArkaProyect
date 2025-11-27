# Arka Microservices - Deployment Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Phase 1: External Services Setup](#phase-1-external-services-setup)
4. [Phase 2: AWS EC2 Setup](#phase-2-aws-ec2-setup)
5. [Phase 3: Railway Deployment](#phase-3-railway-deployment)
6. [Phase 4: CI/CD Configuration](#phase-4-cicd-configuration)
7. [Monitoring & Operations](#monitoring--operations)
8. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### Hybrid Deployment Strategy

**AWS Free Tier (EC2 t2.micro - 1GB RAM)**:
- Config Server (port 8889)
- Eureka Server (port 8761)
- Prometheus (port 9090)
- Nginx Reverse Proxy (port 80/443)

**Railway Platform**:
- API Gateway (port 8090)
- Auth Server (port 8082)
- Product Service (port 8081)
- Customer Service (port 8083)
- Inventory Service (port 8084)
- Order Service (port 8085)
- Cart Service (port 8086)
- Notification Service (port 8087)
- Report Service (port 8088)
- PostgreSQL Database (already configured)

**External Free Services**:
- MongoDB Atlas M0 (512MB free tier)
- CloudAMQP Lemur (free RabbitMQ)
- Grafana Cloud (free monitoring)

### Service Communication Flow

```
User → Railway API Gateway → Auth Server (JWT)
                            → Business Services

Business Services → AWS Eureka (service discovery)
                  → AWS Config Server (configuration)
                  → Railway PostgreSQL (data)
                  → MongoDB Atlas (cart data)
                  → CloudAMQP (async messaging)
```

---

## Prerequisites

### Required Accounts
- [ ] AWS Account (free tier eligible)
- [ ] Railway Account
- [ ] MongoDB Atlas Account
- [ ] CloudAMQP Account
- [ ] Docker Hub Account
- [ ] GitHub Account
- [ ] Grafana Cloud Account (optional)

### Local Tools
- [ ] Docker Desktop installed
- [ ] Git installed
- [ ] AWS CLI installed (optional)
- [ ] Railway CLI: `npm install -g @railway/cli`

---

## Phase 1: External Services Setup

### 1.1 MongoDB Atlas Setup

1. **Create MongoDB Atlas Account**: https://www.mongodb.com/cloud/atlas/register

2. **Create M0 Free Cluster**:
   ```
   - Select Provider: AWS
   - Region: Choose closest to Railway/AWS (e.g., us-east-1)
   - Cluster Tier: M0 Sandbox (Free Forever)
   - Cluster Name: arka-cluster
   ```

3. **Create Database User**:
   ```
   - Username: arka_admin
   - Password: <generate-strong-password>
   - Role: Read and write to any database
   ```

4. **Configure Network Access**:
   ```
   - Add IP Address: 0.0.0.0/0 (allow from anywhere)
   - Comment: Railway + AWS access
   ```

5. **Get Connection String**:
   ```
   mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts?retryWrites=true&w=majority
   ```

6. **Save for later**: You'll need this as `MONGODB_URI` environment variable

### 1.2 CloudAMQP Setup

1. **Create CloudAMQP Account**: https://customer.cloudamqp.com/signup

2. **Create Lemur Instance**:
   ```
   - Plan: Lemur (Free)
   - Name: arka-rabbitmq
   - Region: Choose closest to Railway/AWS
   - Tags: production
   ```

3. **Get AMQP URL**:
   ```
   - Go to instance details
   - Copy AMQP URL: amqps://user:pass@host/vhost
   ```

4. **Save for later**: You'll need this as `CLOUDAMQP_URL` environment variable

### 1.3 Grafana Cloud Setup (Optional)

1. **Create Grafana Cloud Account**: https://grafana.com/auth/sign-up/create-user

2. **Create Free Stack**:
   ```
   - Stack name: arka-monitoring
   - Region: Choose closest region
   ```

3. **Configure Prometheus Data Source**:
   ```
   - Go to Connections → Data Sources
   - Add Prometheus data source
   - URL: http://<AWS_EC2_PUBLIC_IP>:9090
   - Save & Test
   ```

4. **Import Spring Boot Dashboard**:
   ```
   - Dashboard ID: 4701 (JVM Micrometer)
   - Or ID: 12900 (Spring Boot 2.1 Statistics)
   ```

---

## Phase 2: AWS EC2 Setup

### 2.1 Launch EC2 Instance

1. **Sign in to AWS Console**: https://console.aws.amazon.com/

2. **Launch Instance**:
   ```
   - AMI: Amazon Linux 2023
   - Instance Type: t2.micro (Free tier eligible)
   - Key Pair: Create new or use existing
   - Network Settings:
     - Auto-assign Public IP: Enable
     - Security Group: Create new
   - Storage: 30GB gp3 (free tier eligible)
   - Tags: Name=arka-infrastructure
   ```

3. **Configure Security Group**:
   ```
   Inbound Rules:
   - SSH (22): My IP (your current IP)
   - HTTP (80): 0.0.0.0/0
   - HTTPS (443): 0.0.0.0/0
   - Custom TCP (8761): 0.0.0.0/0 (Eureka)
   - Custom TCP (8889): 0.0.0.0/0 (Config Server)
   - Custom TCP (9090): My IP (Prometheus - restricted)

   Outbound Rules:
   - All traffic: 0.0.0.0/0
   ```

4. **Allocate Elastic IP** (optional but recommended):
   ```
   - EC2 → Elastic IPs → Allocate Elastic IP
   - Associate with your instance
   - Note: Free if attached to running instance
   ```

### 2.2 Connect to EC2 Instance

```bash
# Set permissions on your key file
chmod 400 your-key.pem

# Connect via SSH
ssh -i your-key.pem ec2-user@<PUBLIC_IP>
```

### 2.3 Install Docker on EC2

```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install docker -y

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add ec2-user to docker group
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version

# Logout and login again for group changes
exit
```

### 2.4 Clone Infrastructure Repository

```bash
# Reconnect to EC2
ssh -i your-key.pem ec2-user@<PUBLIC_IP>

# Install Git
sudo yum install git -y

# Clone repository
mkdir -p ~/arka-infrastructure
cd ~/arka-infrastructure

# Clone your config repo (update URL)
git clone https://github.com/yourusername/ProyectoArkaAceleraTi.git .
```

### 2.5 Create Environment File

```bash
cd ~/arka-infrastructure/arka-microservicios

# Create .env file for Docker Compose
cat > .env << 'EOF'
DOCKER_REGISTRY=yourdockerhubusername
TAG=latest
SPRING_PROFILES_ACTIVE=prod
CONFIG_GIT_URI=https://github.com/yourusername/arka-config-repo
CONFIG_GIT_BRANCH=main

# Railway service URLs (update after Railway deployment)
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

### 2.6 Update Prometheus Configuration

```bash
# Update prometheus/prometheus.yml with actual Railway URLs
cd ~/arka-infrastructure/arka-microservicios/prometheus
nano prometheus.yml

# Replace ${API_GATEWAY_URL} placeholders with actual Railway URLs from .env
```

### 2.7 Deploy Infrastructure Services

```bash
cd ~/arka-infrastructure/arka-microservicios

# Pull Docker images
docker-compose -f docker-compose-aws.yml pull

# Start services
docker-compose -f docker-compose-aws.yml up -d

# Check logs
docker-compose -f docker-compose-aws.yml logs -f

# Verify services are running
docker-compose -f docker-compose-aws.yml ps

# Check health
curl http://localhost:8889/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Eureka Server
```

### 2.8 Verify Eureka Dashboard

Open browser: `http://<EC2_PUBLIC_IP>:8761`

You should see Eureka Server dashboard (no services registered yet).

---

## Phase 3: Railway Deployment

### 3.1 Install Railway CLI

```bash
# On your local machine
npm install -g @railway/cli

# Login to Railway
railway login
```

### 3.2 Create Railway Project

```bash
# Create new project
railway init

# Link to existing project (if already created)
railway link
```

### 3.3 Deploy Each Service

For each service (api-gateway, auth-server, product-service, etc.):

```bash
cd arka-microservicios/<service-name>

# Create service in Railway
railway service create <service-name>

# Set environment variables
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set EUREKA_SERVER_URL=http://<EC2_PUBLIC_IP>:8761/eureka/
railway variables set SPRING_CLOUD_CONFIG_URI=http://<EC2_PUBLIC_IP>:8889

# For PostgreSQL services (all except cart-service)
railway variables set SPRING_DATASOURCE_URL=jdbc:postgresql://centerbeam.proxy.rlwy.net:34241/railway?sslmode=require
railway variables set SPRING_DATASOURCE_USERNAME=postgres
railway variables set SPRING_DATASOURCE_PASSWORD=HMroWPVhjEVmVdDIPhhPABBtEbBKirFd

# For cart-service only
railway variables set MONGODB_URI=mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts

# For order-service and notification-service
railway variables set CLOUDAMQP_URL=amqps://user:pass@host/vhost

# For notification-service email
railway variables set MAIL_USERNAME=your-email@gmail.com
railway variables set MAIL_PASSWORD=your-app-password

# Deploy service
railway up --service <service-name>

# Get service URL
railway domain
```

### 3.4 Configure Railway Domains

After deployment, Railway assigns domains. Save these for monitoring:

```
api-gateway: https://api-gateway-production.up.railway.app
auth-server: https://auth-server-production.up.railway.app
product-service: https://product-service-production.up.railway.app
...
```

### 3.5 Verify Service Registration

Wait 1-2 minutes, then check Eureka dashboard:

```
http://<EC2_PUBLIC_IP>:8761
```

All 9 Railway services should appear as "UP" in Eureka.

---

## Phase 4: CI/CD Configuration

### 4.1 GitHub Secrets Setup

Go to GitHub Repository → Settings → Secrets and variables → Actions

Add the following secrets:

```
DOCKER_USERNAME=yourdockerhubusername
DOCKER_PASSWORD=yourdockerpassword

AWS_EC2_HOST=<EC2_PUBLIC_IP>
AWS_EC2_USERNAME=ec2-user
AWS_EC2_SSH_KEY=<paste-private-key-content>

RAILWAY_TOKEN=<get-from-railway-cli>
RAILWAY_API_GATEWAY_URL=https://api-gateway-production.up.railway.app
```

To get Railway token:
```bash
railway whoami
# Copy the token from ~/.railway/config.json
```

### 4.2 Test CI/CD Pipeline

```bash
# Make a small change
echo "# Test" >> README.md

# Commit and push
git add .
git commit -m "test: trigger CI/CD pipeline"
git push origin master

# Monitor GitHub Actions
# Go to: https://github.com/yourusername/repo/actions
```

---

## Monitoring & Operations

### Access Points

| Service | URL | Notes |
|---------|-----|-------|
| Eureka Dashboard | `http://<EC2_IP>:8761` | Service registry |
| Config Server | `http://<EC2_IP>:8889` | Configuration |
| Prometheus | `http://<EC2_IP>:9090` | Metrics |
| API Gateway | `https://<railway-domain>` | Public API |
| Grafana Cloud | `https://<your-stack>.grafana.net` | Dashboards |

### Useful Commands

**Check EC2 Service Status**:
```bash
ssh -i your-key.pem ec2-user@<EC2_IP>
cd ~/arka-infrastructure/arka-microservicios
docker-compose -f docker-compose-aws.yml ps
docker-compose -f docker-compose-aws.yml logs -f config-server
```

**Check Railway Service Logs**:
```bash
railway logs --service api-gateway
railway logs --service product-service --tail 100
```

**Restart Services**:
```bash
# AWS EC2
docker-compose -f docker-compose-aws.yml restart config-server

# Railway
railway service restart api-gateway
```

**Scale Railway Services** (if needed):
```bash
railway service scale --replicas 2 api-gateway
```

### Monitoring Metrics

**Prometheus Queries**:
```promql
# Request rate per service
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Memory usage
jvm_memory_used_bytes / jvm_memory_max_bytes

# CPU usage
process_cpu_usage
```

### Setting Up Alerts

**CloudWatch Alarms** (AWS Free Tier: 10 alarms):
```bash
# Create CPU alarm
aws cloudwatch put-metric-alarm \
  --alarm-name arka-ec2-cpu-high \
  --alarm-description "Alert when CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2
```

---

## Troubleshooting

### Services Not Registering with Eureka

**Problem**: Services deployed to Railway don't appear in Eureka dashboard.

**Solutions**:
1. Check environment variable:
   ```bash
   railway variables get EUREKA_SERVER_URL
   # Should be: http://<EC2_PUBLIC_IP>:8761/eureka/
   ```

2. Verify EC2 security group allows inbound on port 8761 from 0.0.0.0/0

3. Check service logs:
   ```bash
   railway logs --service product-service | grep -i eureka
   ```

4. Verify Eureka server is accessible from Railway:
   ```bash
   # Create temporary Railway service
   railway run curl http://<EC2_IP>:8761/eureka/apps
   ```

### MongoDB Connection Failures

**Problem**: Cart service can't connect to MongoDB Atlas.

**Solutions**:
1. Verify MongoDB Atlas network access allows 0.0.0.0/0

2. Check connection string:
   ```bash
   railway variables get MONGODB_URI
   ```

3. Test connection manually:
   ```bash
   mongosh "mongodb+srv://arka_admin:<password>@arka-cluster.xxxxx.mongodb.net/arka_carts"
   ```

4. Verify MongoDB user has read/write permissions

### RabbitMQ Connection Issues

**Problem**: Order/Notification services can't connect to CloudAMQP.

**Solutions**:
1. Verify CloudAMQP URL format:
   ```
   amqps:// (with 's' for SSL)
   ```

2. Check environment variable:
   ```bash
   railway variables get CLOUDAMQP_URL
   ```

3. Test connection:
   ```bash
   # Using amqp-tools
   amqp-declare-queue --url="$CLOUDAMQP_URL" -q test-queue
   ```

### High Memory Usage on EC2

**Problem**: EC2 instance running out of memory.

**Solutions**:
1. Check Docker stats:
   ```bash
   docker stats
   ```

2. Reduce JVM heap sizes in docker-compose-aws.yml:
   ```yaml
   environment:
     JAVA_OPTS: "-Xmx150m -Xms100m"  # Reduce from 200m
   ```

3. Stop Prometheus if not needed:
   ```bash
   docker-compose -f docker-compose-aws.yml stop prometheus
   ```

4. Add swap space:
   ```bash
   sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   ```

### Railway Costs Exceeding Budget

**Problem**: Railway bill higher than expected.

**Solutions**:
1. Check resource usage:
   ```bash
   railway metrics
   ```

2. Set memory limits for all services (512MB):
   ```bash
   railway service update --memory 512
   ```

3. Review logs for excessive restarts:
   ```bash
   railway logs --service <service> | grep -i restart
   ```

4. Consider moving some services to EC2 if feasible

### Config Server Not Serving Configurations

**Problem**: Services can't fetch configuration from Config Server.

**Solutions**:
1. Verify Config Server is running:
   ```bash
   curl http://<EC2_IP>:8889/product-service/prod
   ```

2. Check Git repository access:
   ```bash
   docker-compose -f docker-compose-aws.yml logs config-server | grep -i git
   ```

3. If using private repo, add GitHub token:
   ```yaml
   environment:
     SPRING_CLOUD_CONFIG_SERVER_GIT_URI: https://<token>@github.com/user/repo
   ```

### Database Connection Pool Exhausted

**Problem**: "Connection pool exhausted" errors in logs.

**Solutions**:
1. Increase connection pool size in application.yml:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20  # Increase from 10
   ```

2. Check for connection leaks in code

3. Monitor active connections in Railway PostgreSQL dashboard

---

## Cost Management

### Monthly Cost Estimate

**AWS Free Tier (Year 1)**:
- EC2 t2.micro: $0
- EBS 30GB: $0
- Data transfer: $0 (within limits)
- **Total**: $0/month

**Railway**:
- Estimated usage: $20-30/month
- Set usage alerts at $25

**External Services**:
- MongoDB Atlas M0: $0
- CloudAMQP Lemur: $0
- Grafana Cloud Free: $0

**Total Monthly Cost**: $20-30/month

### Cost Optimization Tips

1. **Use Railway private networking** (no egress charges):
   ```
   service-name.railway.internal
   ```

2. **Set strict memory limits**:
   ```bash
   railway service update --memory 512
   ```

3. **Monitor usage weekly**:
   ```bash
   railway metrics
   ```

4. **Scale down non-production services**

5. **Use AWS Free Tier efficiently** (expires after 12 months)

---

## Disaster Recovery

### Backup Strategy

**PostgreSQL (Railway)**:
- Railway auto-backups: 7 days retention
- Manual backup:
  ```bash
  railway db backup create
  ```

**MongoDB Atlas**:
- Cloud backups enabled by default (M0 tier)
- Export manually if needed:
  ```bash
  mongodump --uri="mongodb+srv://..."
  ```

**Configuration Repository**:
- Already in Git (version controlled)
- Create backup branch monthly

**EC2 Snapshots**:
```bash
# Create EBS snapshot
aws ec2 create-snapshot \
  --volume-id vol-xxxxx \
  --description "arka-infrastructure-backup-$(date +%Y%m%d)"
```

### Recovery Procedures

**If EC2 instance fails**:
1. Launch new t2.micro instance
2. Attach Elastic IP
3. Follow Phase 2.2-2.7 to redeploy

**If Railway service crashes**:
```bash
railway service restart <service-name>
# Or redeploy
railway up --service <service-name>
```

**If entire stack needs rebuild**:
1. Restore EC2 from snapshot
2. Redeploy Railway services using GitHub Actions

---

## Security Checklist

- [ ] SSH key-based authentication only (no password)
- [ ] Security groups restrict unnecessary ports
- [ ] Database passwords stored in environment variables (not config repo)
- [ ] SSL enabled for PostgreSQL connections
- [ ] Railway environment variables encrypted
- [ ] MongoDB Atlas network access configured
- [ ] Regular security updates on EC2: `sudo yum update`
- [ ] Docker images use non-root users
- [ ] Secrets not committed to Git

---

## Next Steps

After successful deployment:

1. **Load Testing**: Use tools like Apache JMeter or k6
2. **Add Logging**: Integrate with Loki or CloudWatch Logs
3. **Implement Distributed Tracing**: Add Spring Cloud Sleuth + Zipkin
4. **Add API Documentation**: Deploy Swagger UI
5. **Set Up Custom Domain**: Configure Route 53 + CloudFront
6. **Implement Rate Limiting**: Add Redis + Spring Cloud Gateway filters
7. **Add End-to-End Tests**: Postman collections or REST Assured

---

## Support

For issues or questions:
- GitHub Issues: https://github.com/yourusername/ProyectoArkaAceleraTi/issues
- Railway Discord: https://discord.gg/railway
- AWS Forums: https://forums.aws.amazon.com/

**Last Updated**: 2025-01-07

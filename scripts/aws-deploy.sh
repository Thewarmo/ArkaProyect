#!/bin/bash

################################################################################
# AWS EC2 Deployment Script for Arka Microservices
################################################################################
# This script deploys/updates Arka microservices on AWS EC2
# Can be run manually or via CI/CD (GitHub Actions)
#
# Usage:
#   ./aws-deploy.sh [service-name]
#   ./aws-deploy.sh all          # Deploy all services
#   ./aws-deploy.sh api-gateway  # Deploy only api-gateway
################################################################################

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
DEPLOYMENT_DIR=~/arka-deployment
COMPOSE_FILE=docker-compose-production.yml
SERVICE=${1:-all}

echo -e "${BLUE}======================================"
echo "Arka Microservices - Deployment"
echo "======================================${NC}"
echo ""

# Change to deployment directory
cd $DEPLOYMENT_DIR || {
    echo -e "${RED}Error: Deployment directory not found!${NC}"
    echo "Run aws-setup.sh first"
    exit 1
}

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo "Create .env file with required environment variables"
    exit 1
fi

# Check if docker-compose file exists
if [ ! -f $COMPOSE_FILE ]; then
    echo -e "${RED}Error: $COMPOSE_FILE not found!${NC}"
    exit 1
fi

echo -e "${GREEN}Step 1/6: Creating backup of current deployment...${NC}"
if [ -f $COMPOSE_FILE ]; then
    cp $COMPOSE_FILE ${COMPOSE_FILE}.backup
    echo -e "${GREEN}✓ Backup created${NC}"
fi

echo ""
echo -e "${GREEN}Step 2/6: Pulling latest Docker images...${NC}"
if [ "$SERVICE" = "all" ]; then
    docker-compose -f $COMPOSE_FILE pull
else
    docker-compose -f $COMPOSE_FILE pull $SERVICE
fi

echo ""
echo -e "${GREEN}Step 3/6: Stopping services...${NC}"
if [ "$SERVICE" = "all" ]; then
    docker-compose -f $COMPOSE_FILE down
else
    docker-compose -f $COMPOSE_FILE stop $SERVICE
fi

echo ""
echo -e "${GREEN}Step 4/6: Starting services...${NC}"
if [ "$SERVICE" = "all" ]; then
    docker-compose -f $COMPOSE_FILE up -d
else
    docker-compose -f $COMPOSE_FILE up -d --no-deps $SERVICE
fi

echo ""
echo -e "${GREEN}Step 5/6: Waiting for services to be healthy...${NC}"

timeout=300  # 5 minutes
elapsed=0

while [ $elapsed -lt $timeout ]; do
    unhealthy=$(docker-compose -f $COMPOSE_FILE ps | grep -c "unhealthy" || true)
    starting=$(docker-compose -f $COMPOSE_FILE ps | grep -c "health: starting" || true)

    if [ $unhealthy -eq 0 ] && [ $starting -eq 0 ]; then
        echo -e "${GREEN}✓ All services are healthy!${NC}"
        break
    fi

    echo "Waiting... ($elapsed/$timeout seconds) - Unhealthy: $unhealthy, Starting: $starting"
    sleep 10
    elapsed=$((elapsed + 10))
done

if [ $elapsed -ge $timeout ]; then
    echo -e "${RED}⚠️ Timeout waiting for services to be healthy${NC}"
    echo -e "${YELLOW}Current status:${NC}"
    docker-compose -f $COMPOSE_FILE ps

    echo ""
    echo -e "${YELLOW}Checking logs for failed services:${NC}"
    docker-compose -f $COMPOSE_FILE logs --tail=50

    exit 1
fi

echo ""
echo -e "${GREEN}Step 6/6: Verifying deployment...${NC}"

# Display service status
echo ""
echo -e "${BLUE}📊 Service Status:${NC}"
docker-compose -f $COMPOSE_FILE ps

# Display resource usage
echo ""
echo -e "${BLUE}💾 Resource Usage:${NC}"
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.CPUPerc}}"

# Check health endpoints
echo ""
echo -e "${BLUE}🔍 Health Checks:${NC}"

check_health() {
    local service=$1
    local port=$2
    local url="http://localhost:$port/actuator/health"

    response=$(curl -s -o /dev/null -w "%{http_code}" $url 2>/dev/null || echo "000")

    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓${NC} $service ($port): ${GREEN}UP${NC}"
    else
        echo -e "${RED}✗${NC} $service ($port): ${RED}DOWN (HTTP $response)${NC}"
    fi
}

check_health "config-server" "8889"
check_health "eureka-server" "8761"
check_health "api-gateway" "8080"
check_health "auth-server" "8081"
check_health "product-service" "8082"
check_health "customer-service" "8083"
check_health "inventory-service" "8084"
check_health "order-service" "8085"
check_health "cart-service" "8086"
check_health "notification-service" "8087"
check_health "report-service" "8088"

# Cleanup old images
echo ""
echo -e "${GREEN}🧹 Cleaning up old Docker images...${NC}"
docker image prune -af --filter "until=24h"

# Display access URLs
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 || echo "unknown")

echo ""
echo -e "${GREEN}======================================"
echo "✓ Deployment Complete!"
echo "======================================${NC}"
echo ""
echo -e "${BLUE}🌐 Access URLs:${NC}"
echo "  - Eureka Dashboard: http://$PUBLIC_IP:8761"
echo "  - API Gateway: http://$PUBLIC_IP:8080"
echo "  - Config Server: http://$PUBLIC_IP:8889"
echo "  - Prometheus: http://$PUBLIC_IP:9090"
echo "  - Grafana: http://$PUBLIC_IP:3000 (admin/admin)"
echo ""
echo -e "${BLUE}📊 Monitor logs:${NC}"
echo "  docker-compose -f $COMPOSE_FILE logs -f [service-name]"
echo ""
echo -e "${BLUE}🔄 Restart a service:${NC}"
echo "  docker-compose -f $COMPOSE_FILE restart [service-name]"
echo ""
echo -e "${BLUE}📈 View metrics:${NC}"
echo "  docker stats"
echo ""
echo -e "${GREEN}Happy monitoring! 🚀${NC}"
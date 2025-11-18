#!/bin/bash

################################################################################
# AWS EC2 Troubleshooting Script for Arka Microservices
################################################################################
# This script helps diagnose common deployment issues
#
# Usage: ./aws-troubleshoot.sh [service-name]
################################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

DEPLOYMENT_DIR=~/arka-deployment
COMPOSE_FILE=docker-compose-production.yml
SERVICE=${1:-all}

echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Arka Microservices - Troubleshooting Tool      ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""

cd $DEPLOYMENT_DIR || {
    echo -e "${RED}Error: Deployment directory not found!${NC}"
    exit 1
}

# Function to print section header
print_section() {
    echo ""
    echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
    echo -e "${GREEN} $1${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
    echo ""
}

# Check 1: System Resources
print_section "1. System Resources"

echo "Memory Usage:"
free -h
echo ""

echo "Disk Usage:"
df -h /
echo ""

echo "Swap Usage:"
swapon --show
echo ""

memory_percent=$(free | grep Mem | awk '{print ($3/$2) * 100.0}')
if (( $(echo "$memory_percent > 90" | bc -l) )); then
    echo -e "${RED}⚠️  WARNING: Memory usage is above 90%!${NC}"
    echo "Consider:"
    echo "  1. Reducing JVM heap sizes in docker-compose-production.yml"
    echo "  2. Stopping non-critical services (Prometheus/Grafana)"
    echo "  3. Adding more swap space"
fi

# Check 2: Docker Status
print_section "2. Docker Status"

echo "Docker Version:"
docker --version
docker-compose --version
echo ""

echo "Docker Service Status:"
sudo systemctl status docker --no-pager | head -5
echo ""

echo "Running Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Check 3: Service Logs
print_section "3. Recent Errors in Logs"

if [ "$SERVICE" = "all" ]; then
    echo "Checking all services for errors..."
    docker-compose -f $COMPOSE_FILE logs --tail=20 | grep -i "error\|exception\|failed" | tail -20
else
    echo "Checking $SERVICE for errors..."
    docker-compose -f $COMPOSE_FILE logs --tail=50 $SERVICE | grep -i "error\|exception\|failed"
fi
echo ""

# Check 4: Health Checks
print_section "4. Service Health Checks"

check_health_detailed() {
    local name=$1
    local port=$2

    echo -n "Testing $name (port $port)... "

    # Check if port is listening
    if ! nc -z localhost $port 2>/dev/null; then
        echo -e "${RED}FAIL - Port not listening${NC}"
        return 1
    fi

    # Check health endpoint
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null || echo "000")

    if [ "$response" = "200" ]; then
        echo -e "${GREEN}OK${NC}"

        # Get detailed health info
        health_json=$(curl -s http://localhost:$port/actuator/health 2>/dev/null)
        status=$(echo $health_json | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        echo "  Status: $status"

        # Check for specific components
        if echo $health_json | grep -q "db"; then
            db_status=$(echo $health_json | grep -o '"db":{[^}]*"status":"[^"]*"' | grep -o 'status":"[^"]*"' | cut -d'"' -f3)
            echo "  Database: $db_status"
        fi
    else
        echo -e "${RED}FAIL - HTTP $response${NC}"

        # Try to get more info
        echo "  Attempting to fetch health details..."
        curl -v http://localhost:$port/actuator/health 2>&1 | head -10
    fi

    echo ""
}

check_health_detailed "Config Server" 8889
check_health_detailed "Eureka Server" 8761
check_health_detailed "API Gateway" 8080
check_health_detailed "Auth Server" 8081

if [ "$SERVICE" != "all" ]; then
    case $SERVICE in
        product-service) check_health_detailed "Product Service" 8082 ;;
        customer-service) check_health_detailed "Customer Service" 8083 ;;
        inventory-service) check_health_detailed "Inventory Service" 8084 ;;
        order-service) check_health_detailed "Order Service" 8085 ;;
        cart-service) check_health_detailed "Cart Service" 8086 ;;
        notification-service) check_health_detailed "Notification Service" 8087 ;;
        report-service) check_health_detailed "Report Service" 8088 ;;
    esac
fi

# Check 5: Database Connectivity
print_section "5. External Services Connectivity"

echo "Testing Railway PostgreSQL connection..."
if grep -q "RAILWAY_POSTGRES_URL" .env; then
    POSTGRES_URL=$(grep "RAILWAY_POSTGRES_URL" .env | cut -d'=' -f2 | sed 's/jdbc://')
    POSTGRES_HOST=$(echo $POSTGRES_URL | sed 's|postgresql://||' | cut -d':' -f1)
    POSTGRES_PORT=$(echo $POSTGRES_URL | cut -d':' -f2 | cut -d'/' -f1)

    if nc -z $POSTGRES_HOST $POSTGRES_PORT 2>/dev/null; then
        echo -e "${GREEN}✓ Railway PostgreSQL is reachable${NC}"
    else
        echo -e "${RED}✗ Cannot connect to Railway PostgreSQL${NC}"
        echo "  Check your Security Group / Network settings"
    fi
else
    echo -e "${YELLOW}⚠ RAILWAY_POSTGRES_URL not found in .env${NC}"
fi
echo ""

echo "Testing MongoDB Atlas connection..."
if grep -q "MONGODB_ATLAS_URI" .env; then
    MONGO_HOST=$(grep "MONGODB_ATLAS_URI" .env | cut -d'@' -f2 | cut -d'/' -f1 | cut -d':' -f1)

    if nc -z $MONGO_HOST 27017 2>/dev/null; then
        echo -e "${GREEN}✓ MongoDB Atlas is reachable${NC}"
    else
        echo -e "${YELLOW}⚠ Cannot verify MongoDB Atlas connectivity (may use non-standard port)${NC}"
    fi
else
    echo -e "${YELLOW}⚠ MONGODB_ATLAS_URI not found in .env${NC}"
fi
echo ""

echo "Testing CloudAMQP RabbitMQ connection..."
if grep -q "CLOUDAMQP_URL" .env; then
    AMQP_HOST=$(grep "CLOUDAMQP_URL" .env | cut -d'@' -f2 | cut -d'/' -f1 | cut -d':' -f1)

    if nc -z $AMQP_HOST 5672 2>/dev/null; then
        echo -e "${GREEN}✓ CloudAMQP is reachable${NC}"
    else
        echo -e "${YELLOW}⚠ Cannot verify CloudAMQP connectivity (may use SSL port 5671)${NC}"
    fi
else
    echo -e "${YELLOW}⚠ CLOUDAMQP_URL not found in .env${NC}"
fi

# Check 6: Eureka Registration
print_section "6. Eureka Service Registration"

echo "Services registered in Eureka:"
eureka_apps=$(curl -s http://localhost:8761/eureka/apps 2>/dev/null || echo "")

if [ -n "$eureka_apps" ]; then
    echo "$eureka_apps" | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort -u
    echo ""

    registered_count=$(echo "$eureka_apps" | grep -o '<name>' | wc -l)
    echo "Total registered services: $registered_count"

    if [ $registered_count -lt 8 ]; then
        echo -e "${YELLOW}⚠️  WARNING: Expected at least 8 services, but only $registered_count are registered${NC}"
    fi
else
    echo -e "${RED}✗ Cannot fetch Eureka registry${NC}"
    echo "Eureka may not be running or accessible"
fi

# Check 7: Docker Image Versions
print_section "7. Docker Image Versions"

docker-compose -f $COMPOSE_FILE images

# Check 8: Container Resource Limits
print_section "8. Container Resource Usage vs Limits"

docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.CPUPerc}}"

# Check 9: Network Connectivity
print_section "9. Network Configuration"

echo "Docker Networks:"
docker network ls
echo ""

echo "Arka Network Details:"
docker network inspect arka-network 2>/dev/null | grep -A 5 "Containers" || echo "Network not found"

# Summary
print_section "TROUBLESHOOTING SUMMARY"

echo -e "${YELLOW}Common Issues and Solutions:${NC}"
echo ""
echo "1. Service won't start:"
echo "   - Check logs: docker-compose -f $COMPOSE_FILE logs [service-name]"
echo "   - Verify .env file has all required variables"
echo "   - Check if port is already in use: netstat -tulpn | grep [port]"
echo ""
echo "2. Service not registering with Eureka:"
echo "   - Verify EUREKA_CLIENT_SERVICEURL_DEFAULTZONE is correct"
echo "   - Check if Eureka Server is UP first"
echo "   - Wait 30-60 seconds for registration"
echo ""
echo "3. Database connection errors:"
echo "   - Verify RAILWAY_POSTGRES_URL, USERNAME, PASSWORD in .env"
echo "   - Check AWS Security Group allows outbound connections"
echo "   - Test connection: telnet [host] [port]"
echo ""
echo "4. Out of Memory errors:"
echo "   - Check: free -h"
echo "   - Reduce JVM heap: Edit JAVA_OPTS in docker-compose-production.yml"
echo "   - Increase swap: sudo fallocate -l 4G /swapfile2"
echo ""
echo "5. Performance issues:"
echo "   - Monitor: htop or ./aws-monitor.sh"
echo "   - Check metrics: http://localhost:9090 (Prometheus)"
echo "   - Disable non-critical services temporarily"
echo ""

echo -e "${GREEN}For more help, check:${NC}"
echo "  - DEPLOYMENT.md"
echo "  - OBSERVABILITY.md"
echo "  - GitHub Issues: https://github.com/yourusername/ProyectoArkaAceleraTi/issues"
echo ""
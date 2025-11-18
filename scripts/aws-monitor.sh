#!/bin/bash

################################################################################
# AWS EC2 Monitoring Script for Arka Microservices
################################################################################
# This script provides real-time monitoring of all services
#
# Usage: ./aws-monitor.sh
################################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

DEPLOYMENT_DIR=~/arka-deployment
COMPOSE_FILE=docker-compose-production.yml

clear

echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Arka Microservices - Real-time Monitoring      ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""

cd $DEPLOYMENT_DIR || exit 1

while true; do
    clear
    echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}   Arka Microservices - System Monitor${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
    echo ""

    # System Resources
    echo -e "${GREEN}📊 System Resources:${NC}"
    echo "$(free -h | head -2)"
    echo ""
    echo "Swap:"
    echo "$(free -h | grep Swap)"
    echo ""

    # Docker Stats
    echo -e "${GREEN}🐳 Docker Containers:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | head -15
    echo ""

    # Service Status
    echo -e "${GREEN}🔍 Service Status:${NC}"
    docker-compose -f $COMPOSE_FILE ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}" | head -15
    echo ""

    # Health Checks
    echo -e "${GREEN}❤️  Health Checks:${NC}"

    check_endpoint() {
        local name=$1
        local port=$2
        response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null || echo "000")
        if [ "$response" = "200" ]; then
            echo -e "  ${GREEN}✓${NC} $name"
        else
            echo -e "  ${RED}✗${NC} $name (HTTP $response)"
        fi
    }

    check_endpoint "Config Server    " 8889
    check_endpoint "Eureka Server    " 8761
    check_endpoint "API Gateway      " 8080
    check_endpoint "Auth Server      " 8081
    check_endpoint "Product Service  " 8082
    check_endpoint "Customer Service " 8083
    check_endpoint "Inventory Service" 8084
    check_endpoint "Order Service    " 8085
    check_endpoint "Cart Service     " 8086
    check_endpoint "Notification Svc " 8087
    check_endpoint "Report Service   " 8088

    echo ""
    echo -e "${YELLOW}Press Ctrl+C to exit${NC}"
    echo -e "${BLUE}Refreshing in 10 seconds...${NC}"

    sleep 10
done
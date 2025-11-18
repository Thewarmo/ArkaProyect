#!/bin/bash

################################################################################
# AWS EC2 Initial Setup Script for Arka Microservices
################################################################################
# This script configures a fresh AWS EC2 t2.micro instance for running
# all Arka microservices with Docker Compose
#
# Usage:
#   1. SSH into your EC2 instance
#   2. wget https://raw.githubusercontent.com/yourusername/ProyectoArkaAceleraTi/main/scripts/aws-setup.sh
#   3. chmod +x aws-setup.sh
#   4. ./aws-setup.sh
################################################################################

set -e  # Exit on error

echo "======================================"
echo "Arka Microservices - AWS EC2 Setup"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running as ec2-user
if [ "$USER" != "ec2-user" ]; then
    echo -e "${RED}Error: This script must be run as ec2-user${NC}"
    exit 1
fi

echo -e "${GREEN}Step 1/8: Updating system packages...${NC}"
sudo yum update -y

echo ""
echo -e "${GREEN}Step 2/8: Installing Docker...${NC}"
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

echo ""
echo -e "${GREEN}Step 3/8: Installing Docker Compose...${NC}"
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version

echo ""
echo -e "${GREEN}Step 4/8: Configuring swap space (4GB)...${NC}"
# Check if swap already exists
if [ ! -f /swapfile ]; then
    sudo dd if=/dev/zero of=/swapfile bs=1M count=4096
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile

    # Make swap permanent
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

    echo -e "${GREEN}✓ Swap configured successfully${NC}"
else
    echo -e "${YELLOW}⚠ Swap file already exists, skipping...${NC}"
fi

# Display current memory
free -h

echo ""
echo -e "${GREEN}Step 5/8: Installing Git and other utilities...${NC}"
# Note: curl-minimal is already installed on Amazon Linux 2023, skip curl to avoid conflicts
sudo yum install git wget htop -y

echo ""
echo -e "${GREEN}Step 6/8: Creating deployment directory...${NC}"
mkdir -p ~/arka-deployment
cd ~/arka-deployment

echo ""
echo -e "${GREEN}Step 7/8: Configuring firewall (Security Group)...${NC}"
echo -e "${YELLOW}Make sure your AWS Security Group allows these inbound ports:${NC}"
echo "  - 22 (SSH)"
echo "  - 80 (HTTP)"
echo "  - 443 (HTTPS)"
echo "  - 8761 (Eureka)"
echo "  - 8889 (Config Server)"
echo "  - 8080 (API Gateway)"
echo "  - 9090 (Prometheus)"
echo "  - 3000 (Grafana)"
echo ""
read -p "Press Enter once you've verified the Security Group configuration..."

echo ""
echo -e "${GREEN}Step 8/8: Creating environment file template...${NC}"

cat > ~/arka-deployment/.env << 'EOF'
# =============================================================================
# Arka Microservices - Production Environment Variables
# =============================================================================
# IMPORTANT: Fill in these values with your actual credentials
# =============================================================================

# Docker Configuration
DOCKER_REGISTRY=ghcr.io/yourusername
TAG=latest
SPRING_PROFILES_ACTIVE=prod

# Git Configuration
CONFIG_GIT_URI=https://github.com/yourusername/arka-config-repo
CONFIG_GIT_BRANCH=main

# Railway PostgreSQL
RAILWAY_POSTGRES_URL=jdbc:postgresql://yourhost.proxy.rlwy.net:port/railway?sslmode=require
RAILWAY_POSTGRES_USER=postgres
RAILWAY_POSTGRES_PASSWORD=your-password

# MongoDB Atlas
MONGODB_ATLAS_URI=mongodb+srv://user:pass@cluster.mongodb.net/arka_carts

# CloudAMQP RabbitMQ
CLOUDAMQP_URL=amqps://user:pass@host/vhost

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT Configuration
JWT_SECRET=ArkaSecretKeyForJWTTokenGenerationAndValidation2024!MustBeLongEnoughForHS256Algorithm

# Eureka Configuration
EUREKA_HOSTNAME=$(curl -s http://169.254.169.254/latest/meta-data/public-hostname || echo "localhost")

# Grafana Configuration
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
GRAFANA_ROOT_URL=http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000
EOF

echo ""
echo -e "${GREEN}======================================"
echo "✓ AWS EC2 Setup Complete!"
echo "======================================${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo ""
echo "1. ${GREEN}IMPORTANT:${NC} You must logout and login again for Docker permissions:"
echo "   ${YELLOW}exit${NC}"
echo "   ${YELLOW}ssh -i your-key.pem ec2-user@$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)${NC}"
echo ""
echo "2. Edit the .env file with your actual credentials:"
echo "   ${YELLOW}nano ~/arka-deployment/.env${NC}"
echo ""
echo "3. Clone the deployment files from your repository:"
echo "   ${YELLOW}cd ~/arka-deployment${NC}"
echo "   ${YELLOW}git clone https://github.com/yourusername/ProyectoArkaAceleraTi.git temp${NC}"
echo "   ${YELLOW}cp temp/arka-microservicios/docker-compose-production.yml .${NC}"
echo "   ${YELLOW}cp -r temp/arka-microservicios/prometheus .${NC}"
echo "   ${YELLOW}cp -r temp/arka-microservicios/grafana .${NC}"
echo "   ${YELLOW}cp -r temp/arka-microservicios/nginx .${NC}"
echo "   ${YELLOW}rm -rf temp${NC}"
echo ""
echo "4. Start the services:"
echo "   ${YELLOW}docker-compose -f docker-compose-production.yml up -d${NC}"
echo ""
echo "5. Monitor the deployment:"
echo "   ${YELLOW}docker-compose -f docker-compose-production.yml logs -f${NC}"
echo ""
echo "6. Access your services:"
echo "   - Eureka: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8761"
echo "   - API Gateway: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "   - Prometheus: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):9090"
echo "   - Grafana: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000"
echo ""
echo -e "${GREEN}======================================"
echo "Happy deploying! 🚀"
echo "======================================${NC}"
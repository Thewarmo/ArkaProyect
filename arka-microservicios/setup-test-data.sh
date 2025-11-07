#!/bin/bash

# 🧪 Script de Configuración de Datos de Prueba - Proyecto Arka
# Versión: 1.0
# Fecha: 2025-01-15

set -e  # Exit on error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# URLs de servicios
AUTH_URL="http://localhost:8082"
PRODUCT_URL="http://localhost:8081"
CUSTOMER_URL="http://localhost:8083"
INVENTORY_URL="http://localhost:8084"
ORDER_URL="http://localhost:8085"

# Variables globales para IDs
USER_ID=""
CUSTOMER_ID=""
CATEGORY_ID=""
BRAND_ID=""
PRODUCT_ID_1=""
PRODUCT_ID_2=""
ORDER_ID=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Setup de Datos de Prueba - Arka${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Función para verificar servicios
check_service() {
    local url=$1
    local name=$2

    echo -n "Verificando $name... "

    if curl -s -f "$url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC}"
        return 0
    else
        echo -e "${RED}✗${NC}"
        echo -e "${RED}Error: $name no está disponible en $url${NC}"
        return 1
    fi
}

# Verificar todos los servicios
echo -e "${YELLOW}1. Verificando servicios...${NC}"
check_service "$AUTH_URL" "Auth Server" || exit 1
check_service "$PRODUCT_URL" "Product Service" || exit 1
check_service "$CUSTOMER_URL" "Customer Service" || exit 1
check_service "$INVENTORY_URL" "Inventory Service" || exit 1
check_service "$ORDER_URL" "Order Service" || exit 1
echo ""

# Paso 1: Registrar usuario
echo -e "${YELLOW}2. Registrando usuario de prueba...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$AUTH_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "testuser1@email.com",
    "password": "Test123!",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CUSTOMER"
  }')

USER_ID=$(echo "$REGISTER_RESPONSE" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*')

if [ -z "$USER_ID" ]; then
    echo -e "${RED}Error: No se pudo registrar el usuario${NC}"
    echo "Respuesta: $REGISTER_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Usuario registrado con ID: $USER_ID${NC}"
echo ""

# Paso 2: Crear cliente
echo -e "${YELLOW}3. Creando cliente...${NC}"
CUSTOMER_RESPONSE=$(curl -s -X POST "$CUSTOMER_URL/api/v1/customers" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"companyName\": \"Tech Solutions SAS\",
    \"taxId\": \"900123456-7\",
    \"contactName\": \"Juan Pérez\",
    \"phone\": \"+57 300 1234567\",
    \"email\": \"juan@techsolutions.com\",
    \"country\": \"COLOMBIA\"
  }")

CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}Error: No se pudo crear el cliente${NC}"
    echo "Respuesta: $CUSTOMER_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Cliente creado con ID: $CUSTOMER_ID${NC}"
echo ""

# Paso 3: Agregar dirección
echo -e "${YELLOW}4. Agregando dirección al cliente...${NC}"
ADDRESS_RESPONSE=$(curl -s -X POST "$CUSTOMER_URL/api/v1/customers/$CUSTOMER_ID/addresses" \
  -H "Content-Type: application/json" \
  -d '{
    "street": "Calle 123 #45-67",
    "city": "Bogotá",
    "state": "Cundinamarca",
    "postalCode": "110111",
    "country": "COLOMBIA",
    "isDefault": true
  }')

if echo "$ADDRESS_RESPONSE" | grep -q "defaultAddress"; then
    echo -e "${GREEN}✓ Dirección agregada exitosamente${NC}"
else
    echo -e "${RED}Error: No se pudo agregar la dirección${NC}"
    echo "Respuesta: $ADDRESS_RESPONSE"
    exit 1
fi
echo ""

# Paso 4: Crear categoría
echo -e "${YELLOW}5. Creando categoría...${NC}"
CATEGORY_RESPONSE=$(curl -s -X POST "$PRODUCT_URL/api/v1/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Periféricos",
    "description": "Teclados, mouse, audífonos"
  }')

CATEGORY_ID=$(echo "$CATEGORY_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$CATEGORY_ID" ]; then
    echo -e "${RED}Error: No se pudo crear la categoría${NC}"
    echo "Respuesta: $CATEGORY_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Categoría creada con ID: $CATEGORY_ID${NC}"
echo ""

# Paso 5: Crear marca
echo -e "${YELLOW}6. Creando marca...${NC}"
BRAND_RESPONSE=$(curl -s -X POST "$PRODUCT_URL/api/v1/brands" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Logitech",
    "description": "Periféricos de alta calidad"
  }')

BRAND_ID=$(echo "$BRAND_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$BRAND_ID" ]; then
    echo -e "${RED}Error: No se pudo crear la marca${NC}"
    echo "Respuesta: $BRAND_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Marca creada con ID: $BRAND_ID${NC}"
echo ""

# Paso 6: Crear producto 1 (Mouse)
echo -e "${YELLOW}7. Creando productos...${NC}"
PRODUCT_1_RESPONSE=$(curl -s -X POST "$PRODUCT_URL/api/v1/products" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Mouse Gamer RGB G502\",
    \"description\": \"Mouse gamer con iluminación RGB y 11 botones programables\",
    \"price\": 189900.00,
    \"stock\": 100,
    \"categoryId\": $CATEGORY_ID,
    \"brandId\": $BRAND_ID
  }")

PRODUCT_ID_1=$(echo "$PRODUCT_1_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$PRODUCT_ID_1" ]; then
    echo -e "${RED}Error: No se pudo crear el producto 1${NC}"
    echo "Respuesta: $PRODUCT_1_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Producto 1 (Mouse) creado con ID: $PRODUCT_ID_1${NC}"

# Paso 7: Crear producto 2 (Teclado)
PRODUCT_2_RESPONSE=$(curl -s -X POST "$PRODUCT_URL/api/v1/products" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Teclado Mecánico RGB K70\",
    \"description\": \"Teclado mecánico con switches Cherry MX Red\",
    \"price\": 459900.00,
    \"stock\": 50,
    \"categoryId\": $CATEGORY_ID,
    \"brandId\": $BRAND_ID
  }")

PRODUCT_ID_2=$(echo "$PRODUCT_2_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$PRODUCT_ID_2" ]; then
    echo -e "${RED}Error: No se pudo crear el producto 2${NC}"
    echo "Respuesta: $PRODUCT_2_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Producto 2 (Teclado) creado con ID: $PRODUCT_ID_2${NC}"
echo ""

# Verificar inventarios
echo -e "${YELLOW}8. Verificando inventarios...${NC}"
sleep 2  # Dar tiempo para que se creen los inventarios automáticamente

INVENTORY_1=$(curl -s "$INVENTORY_URL/api/v1/inventory/product/$PRODUCT_ID_1")
INVENTORY_2=$(curl -s "$INVENTORY_URL/api/v1/inventory/product/$PRODUCT_ID_2")

if echo "$INVENTORY_1" | grep -q "availableStock"; then
    echo -e "${GREEN}✓ Inventario producto 1 (Mouse): 100 unidades disponibles${NC}"
else
    echo -e "${YELLOW}⚠ Inventario producto 1 no encontrado (puede crearse al primer uso)${NC}"
fi

if echo "$INVENTORY_2" | grep -q "availableStock"; then
    echo -e "${GREEN}✓ Inventario producto 2 (Teclado): 50 unidades disponibles${NC}"
else
    echo -e "${YELLOW}⚠ Inventario producto 2 no encontrado (puede crearse al primer uso)${NC}"
fi
echo ""

# Resumen
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  ✅ Setup Completado Exitosamente${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}Datos creados:${NC}"
echo -e "  • Usuario ID: ${YELLOW}$USER_ID${NC}"
echo -e "  • Cliente ID: ${YELLOW}$CUSTOMER_ID${NC}"
echo -e "  • Categoría ID: ${YELLOW}$CATEGORY_ID${NC}"
echo -e "  • Marca ID: ${YELLOW}$BRAND_ID${NC}"
echo -e "  • Producto 1 (Mouse) ID: ${YELLOW}$PRODUCT_ID_1${NC}"
echo -e "  • Producto 2 (Teclado) ID: ${YELLOW}$PRODUCT_ID_2${NC}"
echo ""
echo -e "${GREEN}Puedes usar estos IDs para probar el flujo de órdenes:${NC}"
echo ""
echo -e "${BLUE}Ejemplo - Crear orden:${NC}"
echo "curl -X POST $ORDER_URL/api/v1/orders \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{"
echo "    \"customerId\": $CUSTOMER_ID,"
echo "    \"shippingAddress\": \"Calle 123 #45-67, Bogotá, Colombia\","
echo "    \"items\": ["
echo "      {"
echo "        \"productId\": $PRODUCT_ID_1,"
echo "        \"quantity\": 5,"
echo "        \"unitPrice\": 189900.00"
echo "      }"
echo "    ],"
echo "    \"notes\": \"Prueba E2E\""
echo "  }'"
echo ""
echo -e "${GREEN}Para más ejemplos, consulta: TESTING_GUIDE.md${NC}"
echo ""

# Guardar IDs en archivo para referencia
cat > test-data-ids.txt <<EOF
# Test Data IDs - Generado el $(date)
USER_ID=$USER_ID
CUSTOMER_ID=$CUSTOMER_ID
CATEGORY_ID=$CATEGORY_ID
BRAND_ID=$BRAND_ID
PRODUCT_ID_1=$PRODUCT_ID_1
PRODUCT_ID_2=$PRODUCT_ID_2
EOF

echo -e "${GREEN}✓ IDs guardados en: test-data-ids.txt${NC}"
echo ""

# 🧪 Script de Configuración de Datos de Prueba - Proyecto Arka (PowerShell)
# Versión: 1.0
# Fecha: 2025-01-15

# URLs de servicios
$AuthUrl = "http://localhost:8082"
$ProductUrl = "http://localhost:8081"
$CustomerUrl = "http://localhost:8083"
$InventoryUrl = "http://localhost:8084"
$OrderUrl = "http://localhost:8085"

# Variables para IDs
$UserId = 0
$CustomerId = 0
$CategoryId = 0
$BrandId = 0
$ProductId1 = 0
$ProductId2 = 0

Write-Host "========================================" -ForegroundColor Blue
Write-Host "  Setup de Datos de Prueba - Arka" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue
Write-Host ""

# Función para verificar servicios
function Test-Service {
    param (
        [string]$Url,
        [string]$Name
    )

    Write-Host "Verificando $Name... " -NoNewline

    try {
        $response = Invoke-WebRequest -Uri "$Url/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✓" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host "✗" -ForegroundColor Red
        Write-Host "Error: $Name no está disponible en $Url" -ForegroundColor Red
        return $false
    }
}

# Verificar todos los servicios
Write-Host "1. Verificando servicios..." -ForegroundColor Yellow
if (-not (Test-Service -Url $AuthUrl -Name "Auth Server")) { exit 1 }
if (-not (Test-Service -Url $ProductUrl -Name "Product Service")) { exit 1 }
if (-not (Test-Service -Url $CustomerUrl -Name "Customer Service")) { exit 1 }
if (-not (Test-Service -Url $InventoryUrl -Name "Inventory Service")) { exit 1 }
if (-not (Test-Service -Url $OrderUrl -Name "Order Service")) { exit 1 }
Write-Host ""

# Paso 1: Registrar usuario
Write-Host "2. Registrando usuario de prueba..." -ForegroundColor Yellow
$registerBody = @{
    username = "testuser1"
    email = "testuser1@email.com"
    password = "Test123!"
    firstName = "Juan"
    lastName = "Pérez"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$AuthUrl/api/v1/auth/register" -Method Post `
        -ContentType "application/json" -Body $registerBody -ErrorAction Stop

    $UserId = $registerResponse.userId
    Write-Host "✓ Usuario registrado con ID: $UserId" -ForegroundColor Green
}
catch {
    Write-Host "Error al registrar usuario: $_" -ForegroundColor Red
    Write-Host "Respuesta: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 2: Crear cliente
Write-Host "3. Creando cliente..." -ForegroundColor Yellow
$customerBody = @{
    userId = $UserId
    companyName = "Tech Solutions SAS"
    taxId = "900123456-7"
    contactName = "Juan Pérez"
    phone = "+57 300 1234567"
    email = "juan@techsolutions.com"
    country = "COLOMBIA"
} | ConvertTo-Json

try {
    $customerResponse = Invoke-RestMethod -Uri "$CustomerUrl/api/v1/customers" -Method Post `
        -ContentType "application/json" -Body $customerBody -ErrorAction Stop

    $CustomerId = $customerResponse.id
    Write-Host "✓ Cliente creado con ID: $CustomerId" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear cliente: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 3: Agregar dirección
Write-Host "4. Agregando dirección al cliente..." -ForegroundColor Yellow
$addressBody = @{
    street = "Calle 123 #45-67"
    city = "Bogotá"
    state = "Cundinamarca"
    postalCode = "110111"
    country = "COLOMBIA"
    isDefault = $true
} | ConvertTo-Json

try {
    $addressResponse = Invoke-RestMethod -Uri "$CustomerUrl/api/v1/customers/$CustomerId/addresses" -Method Post `
        -ContentType "application/json" -Body $addressBody -ErrorAction Stop

    Write-Host "✓ Dirección agregada exitosamente" -ForegroundColor Green
}
catch {
    Write-Host "Error al agregar dirección: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 4: Crear categoría
Write-Host "5. Creando categoría..." -ForegroundColor Yellow
$categoryBody = @{
    name = "Periféricos"
    description = "Teclados, mouse, audífonos"
} | ConvertTo-Json

try {
    $categoryResponse = Invoke-RestMethod -Uri "$ProductUrl/api/v1/categories" -Method Post `
        -ContentType "application/json" -Body $categoryBody -ErrorAction Stop

    $CategoryId = $categoryResponse.id
    Write-Host "✓ Categoría creada con ID: $CategoryId" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear categoría: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 5: Crear marca
Write-Host "6. Creando marca..." -ForegroundColor Yellow
$brandBody = @{
    name = "Logitech"
    description = "Periféricos de alta calidad"
} | ConvertTo-Json

try {
    $brandResponse = Invoke-RestMethod -Uri "$ProductUrl/api/v1/brands" -Method Post `
        -ContentType "application/json" -Body $brandBody -ErrorAction Stop

    $BrandId = $brandResponse.id
    Write-Host "✓ Marca creada con ID: $BrandId" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear marca: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 6: Crear producto 1 (Mouse)
Write-Host "7. Creando productos..." -ForegroundColor Yellow
$product1Body = @{
    name = "Mouse Gamer RGB G502"
    description = "Mouse gamer con iluminación RGB y 11 botones programables"
    price = 189900.00
    stock = 100
    categoryId = $CategoryId
    brandId = $BrandId
} | ConvertTo-Json

try {
    $product1Response = Invoke-RestMethod -Uri "$ProductUrl/api/v1/products" -Method Post `
        -ContentType "application/json" -Body $product1Body -ErrorAction Stop

    $ProductId1 = $product1Response.id
    Write-Host "✓ Producto 1 (Mouse) creado con ID: $ProductId1" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear producto 1: $_" -ForegroundColor Red
    exit 1
}

# Paso 7: Crear producto 2 (Teclado)
$product2Body = @{
    name = "Teclado Mecánico RGB K70"
    description = "Teclado mecánico con switches Cherry MX Red"
    price = 459900.00
    stock = 50
    categoryId = $CategoryId
    brandId = $BrandId
} | ConvertTo-Json

try {
    $product2Response = Invoke-RestMethod -Uri "$ProductUrl/api/v1/products" -Method Post `
        -ContentType "application/json" -Body $product2Body -ErrorAction Stop

    $ProductId2 = $product2Response.id
    Write-Host "✓ Producto 2 (Teclado) creado con ID: $ProductId2" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear producto 2: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Verificar inventarios
Write-Host "8. Verificando inventarios..." -ForegroundColor Yellow
Start-Sleep -Seconds 2  # Dar tiempo para que se creen los inventarios

try {
    $inventory1 = Invoke-RestMethod -Uri "$InventoryUrl/api/v1/inventory/product/$ProductId1" -Method Get -ErrorAction SilentlyContinue
    if ($inventory1) {
        Write-Host "✓ Inventario producto 1 (Mouse): $($inventory1.availableStock) unidades disponibles" -ForegroundColor Green
    }
}
catch {
    Write-Host "⚠ Inventario producto 1 no encontrado (puede crearse al primer uso)" -ForegroundColor Yellow
}

try {
    $inventory2 = Invoke-RestMethod -Uri "$InventoryUrl/api/v1/inventory/product/$ProductId2" -Method Get -ErrorAction SilentlyContinue
    if ($inventory2) {
        Write-Host "✓ Inventario producto 2 (Teclado): $($inventory2.availableStock) unidades disponibles" -ForegroundColor Green
    }
}
catch {
    Write-Host "⚠ Inventario producto 2 no encontrado (puede crearse al primer uso)" -ForegroundColor Yellow
}
Write-Host ""

# Resumen
Write-Host "========================================" -ForegroundColor Blue
Write-Host "  ✅ Setup Completado Exitosamente" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Datos creados:" -ForegroundColor Green
Write-Host "  • Usuario ID: $UserId" -ForegroundColor Yellow
Write-Host "  • Cliente ID: $CustomerId" -ForegroundColor Yellow
Write-Host "  • Categoría ID: $CategoryId" -ForegroundColor Yellow
Write-Host "  • Marca ID: $BrandId" -ForegroundColor Yellow
Write-Host "  • Producto 1 (Mouse) ID: $ProductId1" -ForegroundColor Yellow
Write-Host "  • Producto 2 (Teclado) ID: $ProductId2" -ForegroundColor Yellow
Write-Host ""
Write-Host "Puedes usar estos IDs para probar el flujo de órdenes:" -ForegroundColor Green
Write-Host ""
Write-Host "Ejemplo - Crear orden:" -ForegroundColor Blue
Write-Host @"
`$orderBody = @{
    customerId = $CustomerId
    shippingAddress = "Calle 123 #45-67, Bogotá, Colombia"
    items = @(
        @{
            productId = $ProductId1
            quantity = 5
            unitPrice = 189900.00
        }
    )
    notes = "Prueba E2E"
} | ConvertTo-Json

`$order = Invoke-RestMethod -Uri "$OrderUrl/api/v1/orders" -Method Post ``
    -ContentType "application/json" -Body `$orderBody

Write-Host "Orden creada: `$(`$order.orderNumber)"
"@
Write-Host ""
Write-Host "Para más ejemplos, consulta: TESTING_GUIDE.md" -ForegroundColor Green
Write-Host ""

# Guardar IDs en archivo
$testDataIds = @"
# Test Data IDs - Generado el $(Get-Date)
USER_ID=$UserId
CUSTOMER_ID=$CustomerId
CATEGORY_ID=$CategoryId
BRAND_ID=$BrandId
PRODUCT_ID_1=$ProductId1
PRODUCT_ID_2=$ProductId2
"@

$testDataIds | Out-File -FilePath "test-data-ids.txt" -Encoding UTF8
Write-Host "✓ IDs guardados en: test-data-ids.txt" -ForegroundColor Green
Write-Host ""

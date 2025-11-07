@echo off
REM 🧪 Script de Configuración de Datos de Prueba - Proyecto Arka (Windows)
REM Versión: 1.0
REM Fecha: 2025-01-15

setlocal enabledelayedexpansion

REM URLs de servicios
set AUTH_URL=http://localhost:8082
set PRODUCT_URL=http://localhost:8081
set CUSTOMER_URL=http://localhost:8083
set INVENTORY_URL=http://localhost:8084
set ORDER_URL=http://localhost:8085

echo ========================================
echo   Setup de Datos de Prueba - Arka
echo ========================================
echo.

REM Verificar que curl está disponible
where curl >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: curl no está instalado o no está en el PATH
    echo Instala curl o usa Git Bash para ejecutar setup-test-data.sh
    exit /b 1
)

echo 1. Verificando servicios...
echo Verificando Auth Server...
curl -s -f %AUTH_URL%/actuator/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Auth Server no disponible en %AUTH_URL%
    exit /b 1
)
echo [OK] Auth Server

curl -s -f %PRODUCT_URL%/actuator/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Product Service no disponible en %PRODUCT_URL%
    exit /b 1
)
echo [OK] Product Service

curl -s -f %CUSTOMER_URL%/actuator/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Customer Service no disponible en %CUSTOMER_URL%
    exit /b 1
)
echo [OK] Customer Service

curl -s -f %INVENTORY_URL%/actuator/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Inventory Service no disponible en %INVENTORY_URL%
    exit /b 1
)
echo [OK] Inventory Service

curl -s -f %ORDER_URL%/actuator/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Order Service no disponible en %ORDER_URL%
    exit /b 1
)
echo [OK] Order Service
echo.

REM Paso 1: Registrar usuario
echo 2. Registrando usuario de prueba...
curl -s -X POST %AUTH_URL%/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser1\",\"email\":\"testuser1@email.com\",\"password\":\"Test123!\",\"firstName\":\"Juan\",\"lastName\":\"Perez\",\"role\":\"CUSTOMER\"}" ^
  > register_response.json

REM Extraer userId (simplificado para Windows)
echo Usuario registrado. Revisar register_response.json para userId
echo.

REM Paso 2: Crear cliente
echo 3. Creando cliente...
echo Nota: Editar el siguiente comando con el userId correcto
echo curl -X POST %CUSTOMER_URL%/api/v1/customers ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":1,\"companyName\":\"Tech Solutions SAS\",\"taxId\":\"900123456-7\",\"contactName\":\"Juan Perez\",\"phone\":\"+57 300 1234567\",\"email\":\"juan@techsolutions.com\",\"country\":\"COLOMBIA\"}"
echo.

REM Paso 3: Crear categoría
echo 4. Creando categoría...
curl -s -X POST %PRODUCT_URL%/api/v1/categories ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Perifericos\",\"description\":\"Teclados, mouse, audifonos\"}" ^
  > category_response.json

echo Categoria creada. Revisar category_response.json
echo.

REM Paso 4: Crear marca
echo 5. Creando marca...
curl -s -X POST %PRODUCT_URL%/api/v1/brands ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Logitech\",\"description\":\"Perifericos de alta calidad\"}" ^
  > brand_response.json

echo Marca creada. Revisar brand_response.json
echo.

REM Paso 5: Crear productos
echo 6. Creando productos...
echo Nota: Editar con categoryId y brandId correctos
echo.

echo ========================================
echo   Notas Importantes
echo ========================================
echo.
echo Este script de Windows es simplificado debido a limitaciones
echo de parsing JSON en batch.
echo.
echo RECOMENDACION: Usar uno de estos metodos:
echo   1. Git Bash: bash setup-test-data.sh
echo   2. PowerShell: powershell -File setup-test-data.ps1
echo   3. Manual: Seguir TESTING_GUIDE.md paso a paso
echo.
echo Los archivos JSON de respuesta se guardaron en:
echo   - register_response.json
echo   - category_response.json
echo   - brand_response.json
echo.
echo Usa estos archivos para obtener los IDs necesarios.
echo.

endlocal

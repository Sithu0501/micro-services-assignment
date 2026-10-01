# RideLink Microservices - Startup Script (PowerShell)
Write-Host "Starting RideLink Microservices..." -ForegroundColor Cyan

$accountJar = Join-Path $PSScriptRoot "account-service\target\account-service-1.0.0.jar"
$driverJar = Join-Path $PSScriptRoot "driver-vehicle-service\target\driver-vehicle-service-1.0.0.jar"

# Build JARs if they don't exist
if (-not (Test-Path $accountJar)) {
    Write-Host "Building account-service JAR..." -ForegroundColor Yellow
    Push-Location (Join-Path $PSScriptRoot "account-service")
    .\mvnw.cmd package -DskipTests
    Pop-Location
}

if (-not (Test-Path $driverJar)) {
    Write-Host "Building driver-vehicle-service JAR..." -ForegroundColor Yellow
    Push-Location (Join-Path $PSScriptRoot "driver-vehicle-service")
    .\mvnw.cmd package -DskipTests
    Pop-Location
}

# Start Account Service in a new window
Write-Host "Launching Account Service (Port 8081)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\account-service'; java -jar target\account-service-1.0.0.jar"

# Start Driver & Vehicle Service in a new window
Write-Host "Launching Driver & Vehicle Service (Port 8082)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\driver-vehicle-service'; java -jar target\driver-vehicle-service-1.0.0.jar"

Write-Host "`nAll services launched!" -ForegroundColor Cyan
Write-Host "Account Service Swagger UI:        http://localhost:8081/swagger-ui/index.html" -ForegroundColor White
Write-Host "Driver & Vehicle Service Swagger:  http://localhost:8082/swagger-ui/index.html" -ForegroundColor White

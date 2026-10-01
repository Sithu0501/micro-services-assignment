# RideLink Microservices - Startup Script (PowerShell)
param(
    [switch]$Background
)

Write-Host "Starting RideLink Microservices..." -ForegroundColor Cyan

$services = @(
    @{ Name = "account-service"; Port = 8081; Jar = "account-service\target\account-service-1.0.0.jar"; Swagger = "http://localhost:8081/swagger-ui/index.html" },
    @{ Name = "driver-vehicle-service"; Port = 8082; Jar = "driver-vehicle-service\target\driver-vehicle-service-1.0.0.jar"; Swagger = "http://localhost:8082/swagger-ui/index.html" },
    @{ Name = "ride-management-service"; Port = 8083; Jar = "ride-management-service\target\ride-management-service-1.0.0.jar"; Swagger = "http://localhost:8083/swagger-ui/index.html" },
    @{ Name = "fare-payment-service"; Port = 8084; Jar = "fare-payment-service\target\fare-payment-service-0.0.1-SNAPSHOT.jar"; Swagger = "http://localhost:8084/swagger-ui/index.html" }
)

foreach ($s in $services) {
    $jarPath = Join-Path $PSScriptRoot $s.Jar
    $svcDir = Join-Path $PSScriptRoot $s.Name

    if (-not (Test-Path $jarPath)) {
        Write-Host "Building $($s.Name)..." -ForegroundColor Yellow
        Push-Location $svcDir
        .\mvnw.cmd package -DskipTests
        Pop-Location
    }

    Write-Host "Launching $($s.Name) on port $($s.Port)..." -ForegroundColor Green
    if ($Background) {
        Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath -WorkingDirectory $svcDir -WindowStyle Hidden
    } else {
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$svcDir'; java -jar '$jarPath'"
    }
}

Write-Host "`nAll 4 services launched!" -ForegroundColor Cyan
Write-Host "Account Service Swagger UI:        http://localhost:8081/swagger-ui/index.html" -ForegroundColor White
Write-Host "Driver & Vehicle Service Swagger:  http://localhost:8082/swagger-ui/index.html" -ForegroundColor White
Write-Host "Ride Management Service Swagger:   http://localhost:8083/swagger-ui/index.html" -ForegroundColor White
Write-Host "Fare & Payment Service Swagger:    http://localhost:8084/swagger-ui/index.html" -ForegroundColor White
Write-Host "Unified Portal:                    $PSScriptRoot\unified-swagger.html" -ForegroundColor Yellow

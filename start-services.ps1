# RideLink Microservices - Startup Script (PowerShell)
param(
    [switch]$Background
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Starting RideLink Microservices        " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$services = @(
    @{ Name = "Account Service"; Dir = "account-service"; Port = 8081; Jar = "target\account-service-1.0.0.jar" },
    @{ Name = "Driver & Vehicle Service"; Dir = "driver-vehicle-service"; Port = 8082; Jar = "target\driver-vehicle-service-1.0.0.jar" },
    @{ Name = "Ride Management Service"; Dir = "ride-management-service"; Port = 8083; Jar = "target\ride-management-service-1.0.0.jar" },
    @{ Name = "Fare & Payment Service"; Dir = "fare-payment-service"; Port = 8084; Jar = "target\fare-payment-service-0.0.1-SNAPSHOT.jar" }
)

foreach ($s in $services) {
    $svcDir = Join-Path $PSScriptRoot $s.Dir
    $jarPath = Join-Path $svcDir $s.Jar

    if (-not (Test-Path $jarPath)) {
        Write-Host "Building $($s.Name)..." -ForegroundColor Yellow
        Push-Location $svcDir
        .\mvnw.cmd package -DskipTests
        Pop-Location
    }

    Write-Host "Launching $($s.Name) on port $($s.Port)..." -ForegroundColor Green
    if ($Background) {
        Start-Process -FilePath "java" -ArgumentList "-jar", $s.Jar -WorkingDirectory $svcDir -WindowStyle Hidden
    } else {
        Start-Process powershell -WorkingDirectory $svcDir -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting $($s.Name) on Port $($s.Port)...' -ForegroundColor Cyan; java -jar $($s.Jar)"
    }
}

Write-Host "`nAll 4 microservices launched successfully!" -ForegroundColor Cyan
Write-Host "Account Service Swagger UI:        http://localhost:8081/swagger-ui/index.html" -ForegroundColor White
Write-Host "Driver & Vehicle Service Swagger:  http://localhost:8082/swagger-ui/index.html" -ForegroundColor White
Write-Host "Ride Management Service Swagger:   http://localhost:8083/swagger-ui/index.html" -ForegroundColor White
Write-Host "Fare & Payment Service Swagger:    http://localhost:8084/swagger-ui/index.html" -ForegroundColor White
Write-Host "Unified Portal:                    $PSScriptRoot\unified-swagger.html" -ForegroundColor Yellow

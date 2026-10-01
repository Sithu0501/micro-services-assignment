@echo off
REM ==============================================================================
REM RideLink Microservices - Windows Startup Script
REM Starts all four Spring Boot microservices in separate terminal windows.
REM ==============================================================================

setlocal enabledelayedexpansion
title RideLink Startup Manager

echo ===================================================
echo Starting RideLink Microservices System
echo ===================================================
echo [1/4] Starting Account Service (Port 8081)...
start "RideLink - Account Service [8081]" cmd /k "cd /d "%~dp0account-service" && ..\mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo [2/4] Starting Driver & Vehicle Service (Port 8082)...
start "RideLink - Driver Service [8082]" cmd /k "cd /d "%~dp0driver-vehicle-service" && ..\mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo [3/4] Starting Ride Management Service (Port 8083)...
start "RideLink - Ride Service [8083]" cmd /k "cd /d "%~dp0ride-management-service" && ..\mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo [4/4] Starting Fare & Payment Service (Port 8084)...
start "RideLink - Fare Payment Service [8084]" cmd /k "cd /d "%~dp0fare-payment-service" && ..\mvnw.cmd spring-boot:run"

echo ===================================================
echo All services launched!
echo.
echo Service URLs:
echo - Account Service:         http://localhost:8081/swagger-ui/index.html
echo - Driver & Vehicle:        http://localhost:8082/swagger-ui/index.html
echo - Ride Management:         http://localhost:8083/swagger-ui/index.html
echo - Fare & Payment:          http://localhost:8084/swagger-ui/index.html
echo.
echo Central API Documentation:
echo Open docs/index.html in your browser.
echo ===================================================
pause

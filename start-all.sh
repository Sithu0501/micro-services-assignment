#!/usr/bin/env bash
# ==============================================================================
# RideLink Microservices - Unix/macOS Startup Script
# Starts all four Spring Boot microservices in the background.
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==================================================="
echo "Starting RideLink Microservices System"
echo "==================================================="

echo "[1/4] Starting Account Service (Port 8081)..."
(cd "$SCRIPT_DIR/account-service" && ./../mvnw spring-boot:run) > "$SCRIPT_DIR/account-service.log" 2>&1 &
PID_ACCOUNT=$!

sleep 5

echo "[2/4] Starting Driver & Vehicle Service (Port 8082)..."
(cd "$SCRIPT_DIR/driver-vehicle-service" && ./../mvnw spring-boot:run) > "$SCRIPT_DIR/driver-service.log" 2>&1 &
PID_DRIVER=$!

sleep 5

echo "[3/4] Starting Ride Management Service (Port 8083)..."
(cd "$SCRIPT_DIR/ride-management-service" && ./../mvnw spring-boot:run) > "$SCRIPT_DIR/ride-service.log" 2>&1 &
PID_RIDE=$!

sleep 5

echo "[4/4] Starting Fare & Payment Service (Port 8084)..."
(cd "$SCRIPT_DIR/fare-payment-service" && ./../mvnw spring-boot:run) > "$SCRIPT_DIR/fare-service.log" 2>&1 &
PID_PAYMENT=$!

echo "==================================================="
echo "All 4 microservices launched in the background!"
echo "PIDs: Account=$PID_ACCOUNT, Driver=$PID_DRIVER, Ride=$PID_RIDE, Payment=$PID_PAYMENT"
echo ""
echo "Service Swagger Documentation:"
echo "- Account Service:         http://localhost:8081/swagger-ui/index.html"
echo "- Driver & Vehicle:        http://localhost:8082/swagger-ui/index.html"
echo "- Ride Management:         http://localhost:8083/swagger-ui/index.html"
echo "- Fare & Payment:          http://localhost:8084/swagger-ui/index.html"
echo ""
echo "Central API Documentation:"
echo "Open docs/index.html in your browser."
echo "==================================================="

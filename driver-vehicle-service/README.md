# RideLink Driver & Vehicle Service

## Overview

The **Driver & Vehicle Service** is Member 2's responsibility in the **RideLink** ride-hailing backend platform — an enterprise-grade Java 25 + Spring Boot 3.5.x microservices system.

This service is the **sole operational authority** over driver operational profiles, vehicle lifecycle management, driver availability states, simulated GPS positioning, and eligible driver discovery for dispatch by the Ride Management Service. It is **independently deployable** and owns an **exclusive database** (`ridelink_driver_db`) that no other microservice may directly access.

---

## Project Context

RideLink consists of exactly **four backend microservices**:

| # | Service | Owner | Database | Port |
|---|---------|-------|----------|------|
| 1 | Account Service | Member 1 | `ridelink_account_db` | 8081 |
| 2 | **Driver & Vehicle Service** ← *this service* | Member 2 | `ridelink_driver_db` | 8082 |
| 3 | Ride Management Service | Member 3 | `ridelink_ride_db` | 8083 |
| 4 | Fare & Payment Service | Member 4 | `ridelink_payment_db` | 8084 |

This service **does NOT** directly access `ridelink_account_db`, `ridelink_ride_db`, or `ridelink_payment_db`. It links to authenticated drivers via the immutable `userId` claim embedded in Account Service JWT tokens.

---

## Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 25 (LTS) / compatible with JDK 25+ |
| **Framework** | Spring Boot 3.5.10 |
| **Build Tool** | Apache Maven 3.9+ (via `./mvnw.cmd` / `./mvnw` wrapper) |
| **Database** | MongoDB Atlas (`ridelink_driver_db`) |
| **Authentication** | Stateless JWT (JJWT 0.12.6) with HMAC-SHA256 signature verification |
| **Security** | Spring Security 6 (`SecurityFilterChain`, RBAC) |
| **API Documentation** | SpringDoc OpenAPI 3 / Swagger UI 2.8.5 |
| **Validation** | Jakarta Bean Validation 3.0 (`@Valid`, `@NotBlank`, etc.) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| **Logging** | SLF4J + Logback |
| **Distance Algorithm** | Haversine Great-Circle Formula (in `GeoUtils`) |

---

## Environment Variables

The service requires these environment variables. **Never hardcode secrets.**

| Variable | Description | Default / Example |
|----------|-------------|-------------------|
| `PORT` | HTTP server listening port | `8082` |
| `MONGODB_URI` | MongoDB Atlas connection string | `mongodb+srv://.../ridelink_driver_db` |
| `JWT_SECRET` | HMAC-SHA256 signing secret (min 256 bits / 32 chars) | Shared key matching Account Service |
| `JWT_EXPIRATION` | JWT token validity window in milliseconds | `86400000` (24 hours) |

A template file is provided at `.env.example`.

---

## Architecture & Directory Structure

```
driver-vehicle-service/
├── .gitignore
├── .env.example
├── mvnw / mvnw.cmd / .mvn/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridelink/drivervehicleservice/
    │   │   ├── DriverVehicleServiceApplication.java
    │   │   ├── config/
    │   │   │   ├── MongoConfig.java            (@EnableMongoAuditing)
    │   │   │   ├── OpenApiConfig.java          (OpenAPI 3 / Swagger BearerAuth)
    │   │   │   └── SecurityConfig.java         (SecurityFilterChain & RBAC rules)
    │   │   ├── controller/
    │   │   │   ├── DriverController.java       (/api/v1/drivers/**)
    │   │   │   └── VehicleController.java      (/api/v1/vehicles/**)
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── CreateDriverRequest.java
    │   │   │   │   ├── UpdateDriverRequest.java
    │   │   │   │   ├── UpdateAvailabilityRequest.java
    │   │   │   │   ├── UpdateLocationRequest.java
    │   │   │   │   ├── CreateVehicleRequest.java
    │   │   │   │   └── UpdateVehicleRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java        (Uniform response wrapper)
    │   │   │       ├── DriverResponse.java     (Safe projection)
    │   │   │       ├── VehicleResponse.java    (Vehicle projection)
    │   │   │       └── EligibleDriverResponse.java (Ride dispatch projection)
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── ErrorResponse.java          (Standardized error JSON)
    │   │   │   ├── GlobalExceptionHandler.java (@RestControllerAdvice)
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── model/
    │   │   │   ├── AvailabilityStatus.java     (AVAILABLE, UNAVAILABLE, ON_TRIP)
    │   │   │   ├── Driver.java                 (@Document collection = "drivers")
    │   │   │   ├── Location.java               (Simulated GPS latitude & longitude)
    │   │   │   ├── Vehicle.java                (@Document collection = "vehicles")
    │   │   │   └── VehicleType.java            (SEDAN, SUV, VAN, THREE_WHEELER, MOTORCYCLE)
    │   │   ├── repository/
    │   │   │   ├── DriverRepository.java       (MongoRepository<Driver, String>)
    │   │   │   └── VehicleRepository.java      (MongoRepository<Vehicle, String>)
    │   │   ├── security/
    │   │   │   ├── CustomAccessDeniedHandler.java   (403 JSON handler)
    │   │   │   ├── JwtAuthenticationEntryPoint.java (401 JSON handler)
    │   │   │   ├── JwtAuthenticationFilter.java     (Stateless Bearer JWT filter)
    │   │   │   └── UserPrincipal.java               (Stateless UserDetails)
    │   │   ├── service/
    │   │   │   ├── DriverService.java          (Profile, availability, location, eligibility)
    │   │   │   ├── JwtService.java             (Claims extraction & token validation)
    │   │   │   └── VehicleService.java         (Vehicle CRUD & ownership enforcement)
    │   │   └── util/
    │   │       └── GeoUtils.java               (Haversine great-circle distance)
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/ridelink/drivervehicleservice/
        │   ├── controller/
        │   │   ├── DriverControllerTest.java   (MockMvc integration tests)
        │   │   └── VehicleControllerTest.java  (MockMvc integration tests)
        │   ├── service/
        │   │   ├── DriverServiceTest.java      (Mockito unit tests)
        │   │   ├── JwtServiceTest.java         (Token validation tests)
        │   │   └── VehicleServiceTest.java     (Ownership & CRUD unit tests)
        │   └── util/
        │       └── GeoUtilsTest.java           (Distance algorithm unit tests)
        └── resources/
            ├── application-test.yml
            └── mockito-extensions/
                └── org.mockito.plugins.MockMaker
```

---

## REST API Reference

All requests and responses use `application/json`. Endpoints requiring authentication expect the HTTP header:
`Authorization: Bearer <jwt_token>`

### Driver Endpoints (`/api/v1/drivers`)

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/drivers` | Create driver operational profile | `DRIVER`, `ADMIN` |
| `GET` | `/api/v1/drivers/me` | Get current driver profile | `DRIVER`, `ADMIN` |
| `PUT` | `/api/v1/drivers/me` | Update current driver profile | `DRIVER`, `ADMIN` |
| `PATCH` | `/api/v1/drivers/me/availability` | Update availability status (`AVAILABLE`, `UNAVAILABLE`, `ON_TRIP`) | `DRIVER`, `ADMIN` |
| `PUT` | `/api/v1/drivers/me/location` | Update simulated GPS coordinates (`latitude`, `longitude`) | `DRIVER`, `ADMIN` |
| `GET` | `/api/v1/drivers/eligible` | Query available eligible drivers for ride dispatch | Any Authenticated |
| `GET` | `/api/v1/drivers/{id}` | Direct lookup by internal driver profile ID | Any Authenticated |

### Vehicle Endpoints (`/api/v1/vehicles`)

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/vehicles` | Register a new vehicle | `DRIVER`, `ADMIN` |
| `GET` | `/api/v1/vehicles/me` | Get all vehicles owned by current driver | `DRIVER`, `ADMIN` |
| `GET` | `/api/v1/vehicles/{id}` | Direct lookup of vehicle by ID | Any Authenticated |
| `PUT` | `/api/v1/vehicles/{id}` | Update vehicle details (owner only) | `DRIVER`, `ADMIN` |
| `DELETE` | `/api/v1/vehicles/{id}` | Remove vehicle (owner only) | `DRIVER`, `ADMIN` |

---

## Business Invariants & Rules

1. **Profile Exclusivity**: Each Account Service user may create at most **one** driver profile (`userId` unique index).
2. **License Uniqueness**: Driver license numbers are globally unique (`licenseNumber` unique index).
3. **Vehicle Plate Uniqueness**: Vehicle registration numbers / plates are globally unique (`registrationNumber` unique index).
4. **Availability Prerequisite**: A driver **cannot** set their status to `AVAILABLE` unless they have registered at least **one vehicle**.
5. **Vehicle Deletion Cascade**: If a driver deletes their last remaining vehicle while being `AVAILABLE`, their status is automatically set to `UNAVAILABLE`.
6. **Strict Ownership**: A driver can only view, modify, or delete their own vehicles. Modifying another driver's vehicle yields `403 Forbidden`.
7. **Simulated Location**: Coordinates must satisfy standard geographic boundaries: latitude in `[-90.0, 90.0]`, longitude in `[-180.0, 180.0]`.
8. **Proximity Search**: Eligible driver queries calculate distances using the **Haversine formula** and sort available drivers in ascending distance from the pickup coordinate.

---

## Inter-Service Communication Contract

### How Ride Management Service (Member 3) Matches Drivers

When a passenger requests a ride, the **Ride Management Service** calls:

```http
GET /api/v1/drivers/eligible?serviceArea=Colombo&latitude=6.9271&longitude=79.8612&radius=10.0
Authorization: Bearer <jwt_token>
```

#### Response Example (`200 OK`):

```json
{
  "success": true,
  "message": "Eligible drivers retrieved successfully",
  "data": [
    {
      "driverId": "6501f2e8b4a2c10012ab34cd",
      "userId": "6501f2e8b4a2c10012ab34ce",
      "licenseNumber": "B1234567",
      "phoneNumber": "+94771234567",
      "availabilityStatus": "AVAILABLE",
      "serviceArea": "Colombo",
      "currentLocation": {
        "latitude": 6.9350,
        "longitude": 79.8500
      },
      "distanceKm": 1.52,
      "vehicle": {
        "id": "6501f2e8b4a2c10012ab34cf",
        "driverId": "6501f2e8b4a2c10012ab34cd",
        "registrationNumber": "CAB-1234",
        "make": "Toyota",
        "model": "Prius",
        "year": 2022,
        "color": "White",
        "vehicleType": "SEDAN",
        "capacity": 4,
        "createdAt": "2026-09-28T10:00:00Z",
        "updatedAt": "2026-09-28T10:00:00Z"
      }
    }
  ],
  "timestamp": "2026-09-29T00:00:00Z"
}
```

---

## How to Build and Run

### 1. Build and Run Tests

```bash
cd driver-vehicle-service
./mvnw.cmd clean test
```

### 2. Start the Service Locally

```bash
./mvnw.cmd spring-boot:run
```

The service will start on port `8082`.

### 3. Access Swagger UI / OpenAPI Docs

- **Swagger UI**: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

---

## Test Coverage Summary

The service includes **45 automated tests** covering:
- **Unit Tests**: DriverService, VehicleService, JwtService, GeoUtils
- **MockMvc Controller Tests**: DriverController, VehicleController
- **Security & RBAC Tests**: 401 unauthenticated, 403 passenger access restrictions, ownership enforcement
- **Validation Tests**: Jakarta Bean Validation constraints and GlobalExceptionHandler formatting

All tests execute hermetically using mock repositories and Mockito without requiring a live external MongoDB connection.

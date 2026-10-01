# RideLink Backend – IT3130 Application Development Group Assignment

## 📌 Project Overview

RideLink is a backend-only ride-hailing microservices platform developed for the **IT3130 – Application Development Group Assignment**.

The system is designed and architected as **four independently developed, independently executable Java 25 Spring Boot microservices**. Each microservice has a strictly isolated business domain, clear service boundaries, its own dedicated MongoDB database, and its own Swagger/OpenAPI documentation.

The backend provides RESTful JSON APIs that can be developed, tested, and demonstrated using Swagger UI / OpenAPI, Postman, and automated unit/integration tests. No frontend application or MERN/Node.js stack is introduced.

---

## 🎯 Assignment Summary

| Specification | Details |
| --- | --- |
| **Module** | IT3130 – Application Development |
| **Project** | RideLink Backend Microservices Platform |
| **Architecture** | Independent Microservices (Synchronous REST Communication) |
| **Core Technology** | Java 25, Spring Boot 3.5, Maven Reactor Aggregator |
| **Databases** | 4 Isolated MongoDB Databases (Database-per-service pattern) |
| **API Style** | RESTful JSON APIs |
| **API Documentation** | OpenAPI 3.0 / Swagger UI on every service + Central `docs/index.html` |
| **Security** | Spring Security 6, JWT Bearer Token, Role-Based Access Control (RBAC) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test / MockMvc (89 Automated Tests, 100% Passing) |
| **API Testing** | Postman 2.1 Collection (`postman/RideLink.postman_collection.json`) |
| **Build Automation** | Root Aggregator POM + GitHub Actions CI (`.github/workflows/ci.yml`) |

---

## 🏗️ System Architecture

RideLink consists of exactly four microservices maintaining strict persistence boundaries:

```
                          ┌─────────────────────────────────────┐
                          │         Central API Index           │
                          │          (docs/index.html)          │
                          └──────────────────┬──────────────────┘
                                             │
                       ┌─────────────────────┼─────────────────────┐
                       │                     │                     │
                       ▼                     ▼                     ▼
              ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
              │ Account Service │   │Driver & Vehicle │   │ Ride Management │
              │      :8081      │   │ Service :8082   │   │  Service :8083  │
              └────────┬────────┘   └────────┬────────┘   └────────┬────────┘
                       │                     │                     │
                       │ REST                │ REST                │ REST
                       │ (Validate Auth)     │ (Query Drivers)     │ (Calculate / Pay)
                       │                     │                     │
                       ▼                     ▼                     ▼
              ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
              │     MongoDB     │   │     MongoDB     │   │     MongoDB     │
              │ridelink_account │   │ ridelink_driver │   │  ridelink_ride  │
              │      _db        │   │      _db        │   │       _db       │
              └─────────────────┘   └─────────────────┘   └────────┬────────┘
                                                                   │
                                                                   │ REST (HTTP)
                                                                   ▼
                                                          ┌─────────────────┐
                                                          │ Fare & Payment  │
                                                          │  Service :8084  │
                                                          └────────┬────────┘
                                                                   │
                                                                   ▼
                                                          ┌─────────────────┐
                                                          │     MongoDB     │
                                                          │ridelink_payment │
                                                          │      _db        │
                                                          └─────────────────┘
```

### 🔒 Database Isolation Policy
Each microservice strictly owns and manages its own persistence store:
- **Account Service** $\rightarrow$ `ridelink_account_db`
- **Driver & Vehicle Service** $\rightarrow$ `ridelink_driver_db`
- **Ride Management Service** $\rightarrow$ `ridelink_ride_db`
- **Fare & Payment Service** $\rightarrow$ `ridelink_payment_db`

**Strict Architectural Invariant:**  
A service **never** directly accesses or imports repositories of another service's database. For instance, the Ride Management Service never connects to `ridelink_driver_db` or `ridelink_account_db`; all cross-boundary interactions occur strictly via synchronous HTTP/REST APIs.

---

## 🧩 Microservices Specification

| Service | Port | Database | Primary Responsibility | Swagger UI URL |
| :--- | :---: | :--- | :--- | :--- |
| **Account Service** | `8081` | `ridelink_account_db` | User registration, login, JWT token issuance, user profile management, RBAC (`PASSENGER`, `DRIVER`, `ADMIN`), account status | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) |
| **Driver & Vehicle Service** | `8082` | `ridelink_driver_db` | Driver operational profile, vehicle registration & specs, real-time availability toggle, simulated location & proximity matching | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) |
| **Ride Management Service** | `8083` | `ridelink_ride_db` | Ride booking requests, driver assignment, ride lifecycle state machine, cancellation rules, passenger/driver trip history | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) |
| **Fare & Payment Service** | `8084` | `ridelink_payment_db` | Distance/duration & surge fare estimation, payment transaction processing, simulated payments, transaction receipts | [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) |

---

## 📦 Repository Structure

```
RideLink-Backend/
│
├── account-service/                  # Member 1: User & Authentication Domain
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── driver-vehicle-service/           # Member 2: Driver & Vehicle Operations
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── ride-management-service/          # Member 3: Ride Lifecycle Domain
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── fare-payment-service/             # Member 4: Fare & Payment Domain
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── docs/
│   └── index.html                    # Central API Documentation Portal
│
├── postman/
│   └── RideLink.postman_collection.json # Group Postman Collection & Workflows
│
├── .github/
│   └── workflows/
│       └── ci.yml                    # Automated GitHub Actions CI Pipeline
│
├── pom.xml                           # Root Maven Aggregator POM
├── .env.example                      # Central Environment Variables Template
├── .gitignore                        # Git exclusion rules
├── start-all.bat                     # Windows Multi-Service Startup Script
├── start-all.sh                      # Unix/macOS Multi-Service Startup Script
├── stop-services.ps1                 # Service shutdown script for Windows
└── README.md                         # Comprehensive System Documentation
```

---

## 🛠️ Root Maven Aggregator

The root [pom.xml](file:///c:/Users/Jayanga/Desktop/all%20in%20one/micro-services-assignment/pom.xml) is configured as a Maven aggregator POM (`<packaging>pom</packaging>`).

It allows building, packaging, and testing all four microservices with single root commands while preserving their independent executable status and dependencies:

```bash
# Execute unit & slice tests across all 4 microservices
./mvnw clean test

# Build executable production JARs for all 4 microservices
./mvnw clean package
```

> **Note:** The root POM is strictly an aggregator. It does not merge the services into a single monolithic JAR or combine their configurations. Each service continues to possess its own `@SpringBootApplication`, port, configuration, and build artifact.

---

## 📖 Central API Documentation Index

A clean, responsive, framework-free documentation index is provided at:
📂 [docs/index.html](file:///c:/Users/Jayanga/Desktop/all%20in%20one/micro-services-assignment/docs/index.html)

### Features:
- Title: **RideLink Backend API Documentation**
- Subtitle: **IT3130 Application Development Group Assignment**
- Four distinct cards representing each microservice with its assigned port, database, and domain description.
- Direct links to:
  - **Swagger UI**: `/swagger-ui/index.html`
  - **OpenAPI 3.0 JSON specification**: `/v3/api-docs`

---

## 🚀 Running the System

### Option A: Automated Multi-Service Startup Script

#### Windows:
```cmd
start-all.bat
```
*(Launches each microservice in its own separate command prompt window).*

To stop all running services on Windows:
```powershell
powershell -ExecutionPolicy Bypass -File .\stop-services.ps1
```

#### Unix / macOS:
```bash
chmod +x start-all.sh
./start-all.sh
```

---

### Option B: Running Services Individually

Open four separate terminal windows and run:

```bash
# Terminal 1 - Account Service (Port 8081)
cd account-service
..\mvnw spring-boot:run

# Terminal 2 - Driver & Vehicle Service (Port 8082)
cd driver-vehicle-service
..\mvnw spring-boot:run

# Terminal 3 - Ride Management Service (Port 8083)
cd ride-management-service
..\mvnw spring-boot:run

# Terminal 4 - Fare & Payment Service (Port 8084)
cd fare-payment-service
..\mvnw spring-boot:run
```

---

## ⚙️ Environment Variables

A template file [`.env.example`](file:///c:/Users/Jayanga/Desktop/all%20in%20one/micro-services-assignment/.env.example) is provided at the root:

| Variable | Default / Example | Purpose |
| --- | --- | --- |
| `ACCOUNT_SERVICE_PORT` | `8081` | HTTP listening port for Account Service |
| `DRIVER_SERVICE_PORT` | `8082` | HTTP listening port for Driver Service |
| `RIDE_SERVICE_PORT` | `8083` | HTTP listening port for Ride Service |
| `PAYMENT_SERVICE_PORT` | `8084` | HTTP listening port for Fare & Payment Service |
| `ACCOUNT_MONGODB_URI` | `mongodb+srv://.../ridelink_account_db` | MongoDB Atlas URI for Account Service |
| `DRIVER_MONGODB_URI` | `mongodb+srv://.../ridelink_driver_db` | MongoDB Atlas URI for Driver Service |
| `RIDE_MONGODB_URI` | `mongodb+srv://.../ridelink_ride_db` | MongoDB Atlas URI for Ride Service |
| `PAYMENT_MONGODB_URI` | `mongodb+srv://.../ridelink_payment_db` | MongoDB Atlas URI for Fare & Payment Service |
| `ACCOUNT_SERVICE_URL` | `http://localhost:8081` | REST URL used by Driver & Ride Services |
| `DRIVER_SERVICE_URL` | `http://localhost:8082` | REST URL used by Ride Management Service |
| `PAYMENT_SERVICE_URL` | `http://localhost:8084` | REST URL used by Ride Management Service |
| `JWT_SECRET` | *Secret Key (256+ bits)* | HMAC-SHA256 signature key for JWT tokens |
| `JWT_EXPIRATION_MS` | `86400000` | JWT token validity in milliseconds (24h) |

---

## 🔄 End-to-End Integrated Demonstration Workflow

The group Postman collection ([`postman/RideLink.postman_collection.json`](file:///c:/Users/Jayanga/Desktop/all%20in%20one/micro-services-assignment/postman/RideLink.postman_collection.json)) contains an executable end-to-end workflow simulating a complete ride lifecycle across all four microservices:

```
[1] Register Passenger ──> [2] Login & Get JWT ──> [3] Calculate Fare Estimate
         (Account)                  (Account)                   (Payment)
                                                                    │
[6] Accept Ride <───────── [5] Assign Driver <───── [4] Book Ride ◄─┘
      (Ride)                     (Ride)                  (Ride)
         │
         ▼
[7] Start Ride ──────────> [8] Complete Ride ────> [9] Process Payment ──> [10] View Receipt
      (Ride)                     (Ride)                    (Payment)              (Payment)
```

1. **Step 1 - Register Passenger (`Account Service` - `8081`):**  
   `POST /api/v1/auth/register` creates passenger account with encrypted password.
2. **Step 2 - Login Passenger (`Account Service` - `8081`):**  
   `POST /api/v1/auth/login` verifies credentials and returns JWT bearer token.
3. **Step 3 - Fare Estimation (`Fare & Payment Service` - `8084`):**  
   `POST /api/fares/calculate` calculates estimated trip fare based on GPS coordinates.
4. **Step 4 - Request Ride (`Ride Management Service` - `8083`):**  
   `POST /api/v1/rides` registers trip in `REQUESTED` state with passenger reference.
5. **Step 5 - Assign Driver (`Ride Management Service` - `8083`):**  
   `POST /api/v1/rides/{id}/assign` links an eligible driver from Driver Service; state moves to `ASSIGNED`.
6. **Step 6 - Driver Accepts Ride (`Ride Management Service` - `8083`):**  
   `PATCH /api/v1/rides/{id}/accept` transitions status to `ACCEPTED`.
7. **Step 7 - Driver Starts Ride (`Ride Management Service` - `8083`):**  
   `PATCH /api/v1/rides/{id}/start` transitions status to `IN_PROGRESS`.
8. **Step 8 - Driver Completes Ride (`Ride Management Service` - `8083`):**  
   `PATCH /api/v1/rides/{id}/complete` finalizes trip to `COMPLETED`.
9. **Step 9 - Process Payment (`Fare & Payment Service` - `8084`):**  
   `POST /api/payments` records simulated card transaction for the completed ride.
10. **Step 10 - Retrieve Receipt (`Fare & Payment Service` - `8084`):**  
    `GET /api/receipts/ride/{rideId}` returns payment receipt and itemized breakdown.

---

## 🧪 Testing and Quality Assurance

### Test Suite Execution
Automated unit, service, and web layer tests are implemented across all four services:
- **Account Service**: 17 tests (Auth, UserController, Token validation)
- **Driver & Vehicle Service**: 21 tests (DriverService, VehicleService, Availability, Proximity)
- **Ride Management Service**: 37 tests (RideService, RideValidationService, RideAssignmentService, RideController)
- **Fare & Payment Service**: 14 tests (FareCalculationService, PaymentService, ReceiptService)
- **Total Test Count**: **89 tests**, **100% passing**, **0 failures**.

```bash
# Run all tests via Maven reactor
./mvnw clean test
```

---

## 👥 Team Responsibilities & Git Boundaries

| Member | Service Directory | Assigned Port | Primary Business Domain |
| :--- | :--- | :---: | :--- |
| **Member 1** | `account-service` | `8081` | Authentication, JWT, Users, Profiles, RBAC |
| **Member 2** | `driver-vehicle-service` | `8082` | Driver Profiles, Vehicles, Availability, Locations |
| **Member 3** | `ride-management-service` | `8083` | Ride Requests, Driver Assignment, State Machine |
| **Member 4** | `fare-payment-service` | `8084` | Fare Estimation, Transactions, Receipts |

Root-level files (`pom.xml`, `README.md`, `docs/`, `postman/`, `.github/`, scripts) represent shared integration infrastructure and do not infringe on individual member code ownership.

---

## 🏁 Final Integration Audit Status

- [x] Exactly four business microservices preserved
- [x] All microservices use Java 25 & Spring Boot 3.5
- [x] Four independent MongoDB databases with strict isolation
- [x] No direct cross-service database access or foreign repository imports
- [x] Standardized service ports (8081, 8082, 8083, 8084)
- [x] Individual OpenAPI / Swagger UI accessible on all services
- [x] Central API documentation landing page created (`docs/index.html`)
- [x] Root Maven aggregator build functional (`mvn clean package`)
- [x] Root Maven aggregator test suite functional (`mvn clean test` - 89/89 passing)
- [x] JWT authentication and RBAC security intact
- [x] Jakarta Bean Validation functional across all domains
- [x] Downstream REST failure handling and resilience implemented
- [x] Zero committed secrets or credentials (`.env.example` provided)
- [x] Organized group Postman collection with end-to-end workflow
- [x] GitHub Actions CI workflow configured (`.github/workflows/ci.yml`)
- [x] Zero MERN / Node.js / Express components introduced
# RideLink Backend – IT3130 Application Development Group Assignment

## 📌 Project Overview

RideLink is a backend-only ride-hailing microservices system developed for the **IT3130 – Application Development Group Assignment**.

The system is designed using four independently executable Spring Boot microservices. Each microservice has a clear business responsibility and its own independent MongoDB database.

The backend provides RESTful JSON APIs that can be developed, tested, and demonstrated using Swagger UI / OpenAPI and Postman. No frontend application is required.

## 🎯 Assignment Summary

| Item | Details |
| --- | --- |
| **Module** | IT3130 – Application Development |
| **Project** | RideLink |
| **Architecture** | Microservices |
| **Backend** | Java + Spring Boot |
| **Java Version** | Java 25 |
| **Database** | MongoDB |
| **API Style** | REST / JSON |
| **API Documentation** | OpenAPI / Swagger UI |
| **API Testing** | Postman + Swagger UI |
| **Frontend** | Not required |
| **Services** | 4 |
| **Persistence** | Independent database per service |

---

## 🏗️ System Architecture

RideLink consists of four core microservices:

```
                         ┌──────────────────────┐
                         │      API Clients     │
                         │ Swagger / Postman    │
                         └──────────┬───────────┘
                                    │
                   ┌────────────────┼────────────────┐
                   │                │                │
                   ▼                ▼                ▼
          ┌────────────────┐ ┌───────────────┐ ┌─────────────────┐
          │ Account Service│ │ Driver &      │ │ Ride Management │
          │    :8081       │ │ Vehicle       │ │ Service :8083   │
          │                │ │ Service :8082 │ │                 │
          └───────┬────────┘ └───────┬───────┘ └────────┬────────┘
                  │                  │                  │
                  ▼                  ▼                  │
        ┌──────────────────┐ ┌──────────────────┐       │
        │ MongoDB          │ │ MongoDB          │       │
        │ ridelink_account │ │ ridelink_driver  │       │
        │ _db              │ │ _db              │       │
        └──────────────────┘ └──────────────────┘       │
                                                         │
                                  ┌──────────────────────┘
                                  │
                                  ▼
                         ┌──────────────────────┐
                         │ Fare & Payment       │
                         │ Service :8084        │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ MongoDB              │
                         │ ridelink_payment_db  │
                         └──────────┬───────────┘
```

> **Important Architecture Rule:**  
> Each microservice owns its own database:
> - **Account Service** $\rightarrow$ `ridelink_account_db`
> - **Driver & Vehicle Service** $\rightarrow$ `ridelink_driver_db`
> - **Ride Management Service** $\rightarrow$ `ridelink_ride_db`
> - **Fare & Payment Service** $\rightarrow$ `ridelink_payment_db`
>
> A service must **never** directly query or modify another service's database. Inter-service communication must happen through documented APIs.

---

## 📦 Repository Structure

```
RideLink-Backend/
│
├── account-service/
│   ├── src/
│   ├── pom.xml
│   ├── README.md
│   └── .env.example
│
├── driver-vehicle-service/
│   ├── src/
│   ├── pom.xml
│   ├── README.md
│   └── .env.example
│
├── ride-management-service/
│   ├── src/
│   ├── pom.xml
│   ├── README.md
│   └── .env.example
│
├── fare-payment-service/
│   ├── src/
│   ├── pom.xml
│   ├── README.md
│   └── .env.example
│
├── docs/
│   ├── architecture/
│   ├── sequence-diagrams/
│   └── api/
│
├── postman/
│   ├── RideLink.postman_collection.json
│   └── RideLink.postman_environment.json
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── .gitignore
└── README.md
```

---

## 🧩 Microservices

### 1. Account Service

- **Port:** `8081`
- **Database:** `ridelink_account_db`
- **Swagger UI:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

#### Responsibility
The Account Service manages passenger registration, driver registration, authentication, JWT token issuance, user profiles, role management, account status, and user CRUD operations.

#### Main Roles & Statuses
- **Roles:** `PASSENGER`, `DRIVER`, `ADMIN`
- **Account Status:** `ACTIVE`, `SUSPENDED`, `DEACTIVATED`

#### Main API Groups
```http
POST   /api/v1/auth/register
POST   /api/v1/auth/login

POST   /api/v1/users
GET    /api/v1/users
GET    /api/v1/users/{id}
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}

GET    /api/v1/users/me
PUT    /api/v1/users/me

PATCH  /api/v1/users/{id}/status
```

#### Security
- BCrypt password hashing
- JWT authentication & Role-Based Access Control (RBAC)
- Protected user endpoints & Admin-only administrative operations
- Passwords are never returned in API responses
- Secrets loaded through environment variables

---

### 2. Driver & Vehicle Service

- **Port:** `8082`
- **Database:** `ridelink_driver_db`
- **Swagger UI:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

#### Responsibility
The Driver & Vehicle Service manages driver operational profiles, driver availability, vehicle information, service areas, simulated current driver location, and eligible available-driver retrieval.

#### Main Concepts
- Driver Profile
- Vehicle Information
- Driver Availability
- Service Area
- Current Location (Simulated)

The service provides APIs required by the Ride Management Service to identify eligible available drivers.

---

### 3. Ride Management Service

- **Port:** `8083`
- **Database:** `ridelink_ride_db`
- **Swagger UI:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

#### Responsibility
The Ride Management Service manages ride requests, pickup location, destination, driver assignment, ride lifecycle, ride retrieval, ride cancellation, and ride completion.

#### Ride Lifecycle
```
REQUESTED ──> ASSIGNED ──> ACCEPTED ──> IN_PROGRESS ──> COMPLETED
```
**Cancellation Path:**
- `REQUESTED` $\rightarrow$ `CANCELLED`
- `ASSIGNED` $\rightarrow$ `CANCELLED`
- `ACCEPTED` $\rightarrow$ `CANCELLED`

The service validates state transitions and rejects invalid lifecycle operations.

---

### 4. Fare & Payment Service

- **Port:** `8084`
- **Database:** `ridelink_payment_db`
- **Swagger UI:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html)

#### Responsibility
The Fare & Payment Service manages fare estimation, final fare calculation, documented fare rules, simulated payment recording, payment status, receipt generation, and receipt retrieval.

#### Example Payment States
- `PENDING`
- `SUCCESS`
- `FAILED`

---

## 🔄 Core RideLink Workflows

### Workflow 1 – Account and Access
```
User ──> Register ──> Account Service ──> Validate & Hash Password ──> MongoDB
User ──> Login ──> Account Service ──> Verify Credentials & Generate JWT
```

### Workflow 2 – Driver Preparation
```
Driver ──> Account Service ──> Driver & Vehicle Service
 (Configure Driver Profile, Vehicle Information, Availability, Service Area, Current Location)
```

### Workflow 3 – Fare Estimation
```
Passenger ──> Ride Management Service (Pickup, Destination) ──> Fare & Payment Service (Estimate Fare)
```

### Workflow 4 – Ride Request and Driver Assignment
```
Passenger ──> Ride Management Service (Create Ride) ──> Driver & Vehicle Service (Find Eligible Driver) ──> Ride Management Service (Assign Driver)
```

### Workflow 5 – Ride Lifecycle
```
REQUESTED ──> ASSIGNED ──> ACCEPTED ──> IN_PROGRESS ──> COMPLETED
  │             │            │
  └─────────────┼────────────┘ ──> CANCELLED
```

### Workflow 6 – Completion and Payment
```
Ride Completed ──> Fare & Payment Service
                    ├── Calculate final fare
                    ├── Record simulated payment
                    ├── Update payment status
                    └── Generate receipt
```

---

## 🔗 Inter-Service Communication

RideLink uses REST-based synchronous communication between services where cross-service information is required.

- **Interaction 1:** Ride Management Service $\xrightarrow{\text{GET eligible drivers}}$ Driver & Vehicle Service
- **Interaction 2:** Ride Management Service $\xrightarrow{\text{POST fare estimation}}$ Fare & Payment Service

### Rules for Inter-Service Communication
1. Use stable IDs between services.
2. Do not share MongoDB collections between services.
3. Do not directly access another service's database.
4. Validate inter-service requests and responses.
5. Handle timeout and error scenarios appropriately.
6. Document API contracts.

---

## 🔐 Security

- **Authentication:** JWT access tokens issued by Account Service on login (`Authorization: Bearer <JWT_TOKEN>`).
- **Authorization:** Role-Based Access Control (`PASSENGER`, `DRIVER`, `ADMIN`).
- **Password Security:** Passwords hashed with BCrypt, never stored as plaintext, never returned in API responses, and never logged.
- **Secrets Management:** Configured via environment variables (`MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION`). Real secrets are never committed to Git.

---

## 🗄️ Database Design

A single MongoDB Atlas cluster may host the four independent databases:

```
ridelink
├── ridelink_account_db ──> users
├── ridelink_driver_db  ──> drivers, vehicles
├── ridelink_ride_db    ──> rides
└── ridelink_payment_db ──> fares, payments
```

---

## 🧪 Validation and Error Handling

All services validate incoming requests using Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Pattern`) and custom business rules.

### Standard HTTP Status Codes

| Status | Meaning |
| --- | --- |
| `200` | Successful operation |
| `201` | Resource created |
| `204` | Successful operation with no response body |
| `400` | Invalid request / validation failure |
| `401` | Authentication required or invalid |
| `403` | Insufficient permissions |
| `404` | Resource not found |
| `409` | Resource conflict |
| `500` | Unexpected server error |

### Error Response Format
```json
{
  "timestamp": "2026-09-28T10:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/v1/example",
  "fieldErrors": {
    "email": "Invalid email format"
  }
}
```

---

## ❌ Negative Scenarios

1. **Duplicate Registration:** Registering with an existing email returns `409 Conflict`.
2. **Unauthorized Role:** Passenger attempting an Admin operation returns `403 Forbidden`.
3. **Invalid JWT / Missing Auth:** Returns `401 Unauthorized`.
4. **Invalid Ride State Transition:** Transitioning out of sequence returns `400 Bad Request`.
5. **No Eligible Driver:** Returns `404 Not Found` or `400 Bad Request` depending on state.

---

## 📖 API Documentation

Swagger UI URLs:
- **Account Service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Driver & Vehicle Service:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **Ride Management Service:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- **Fare & Payment Service:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html)

OpenAPI JSON endpoint: `/v3/api-docs`

---

## 📮 Postman

A shared Postman collection is maintained for the complete system:

```
RideLink/
├── 01 - Account Service
│   ├── Register Passenger
│   ├── Register Driver
│   ├── Login
│   ├── Get Profile
│   ├── Update Profile
│   ├── Get Users
│   └── Account Status
├── 02 - Driver & Vehicle Service
│   ├── Driver CRUD
│   ├── Vehicle CRUD
│   ├── Availability
│   └── Eligible Drivers
├── 03 - Ride Management Service
│   ├── Create Ride
│   ├── Assign Driver
│   ├── Accept Ride
│   ├── Start Ride
│   ├── Complete Ride
│   └── Cancel Ride
└── 04 - Fare & Payment Service
    ├── Estimate Fare
    ├── Calculate Final Fare
    ├── Create Payment
    ├── Payment Status
    └── Receipt
```

**Environment Variables:**
`accountBaseUrl`, `driverBaseUrl`, `rideBaseUrl`, `paymentBaseUrl`, `jwtToken`, `userId`, `driverId`, `rideId`, `paymentId`.

---

## 🌿 Git Workflow & Continuous Integration

- **Main Branches:** `main`, `develop`
- **Feature Branches:** `feature/member1-account-service`, `feature/member2-driver-vehicle-service`, `feature/member3-ride-management-service`, `feature/member4-fare-payment-service`
- **CI Pipeline:** `.github/workflows/ci.yml` (Java 25 setup, Maven build, automated testing across all microservices).

---

## 🚀 Running the Services Locally

Each service can be built and run independently:

```bash
# Account Service (Port 8081)
cd account-service
mvn clean test
mvn spring-boot:run

# Driver & Vehicle Service (Port 8082)
cd driver-vehicle-service
mvn clean test
mvn spring-boot:run

# Ride Management Service (Port 8083)
cd ride-management-service
mvn clean test
mvn spring-boot:run

# Fare & Payment Service (Port 8084)
cd fare-payment-service
mvn clean test
mvn spring-boot:run
```

---

## 👥 Team Responsibilities

| Member | Service | Main Responsibility |
| --- | --- | --- |
| **Member 1** | Account Service | Authentication, users, profiles, roles, account status |
| **Member 2** | Driver & Vehicle Service | Driver profiles, vehicles, availability, location |
| **Member 3** | Ride Management Service | Ride requests, assignment, lifecycle |
| **Member 4** | Fare & Payment Service | Fare estimation, final fare, payment, receipts |

---

## 🏁 Final Project Status

- [ ] Account Service
- [ ] Driver & Vehicle Service
- [ ] Ride Management Service
- [ ] Fare & Payment Service
- [ ] MongoDB Databases
- [ ] REST APIs
- [ ] Inter-service Communication
- [ ] JWT Authentication
- [ ] RBAC
- [ ] Validation
- [ ] Exception Handling
- [ ] Swagger/OpenAPI
- [ ] Postman Collection
- [ ] Unit Tests
- [ ] Integration Tests
- [ ] CI Pipeline
- [ ] Architecture Diagram
- [ ] Sequence Diagram
- [ ] README
- [ ] Git/PR Workflow
- [ ] Final Demo
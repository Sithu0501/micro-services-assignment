# RideLink Account Service

## Overview

The **Account Service** is Member 1's responsibility in the **RideLink** ride-hailing backend platform — a Java 25 + Spring Boot 3.5.x microservices system.

This service is the **sole authority** over user identity, authentication, authorization, roles, and account lifecycle management for the entire RideLink platform. It is **independently deployable** and owns an **exclusive database** (`ridelink_account_db`) that no other microservice may directly access.

---

## Project Context

RideLink consists of exactly **four backend microservices**:

| # | Service | Owner | Database |
|---|---------|-------|----------|
| 1 | **Account Service** ← *this service* | Member 1 | `ridelink_account_db` |
| 2 | Driver & Vehicle Service | Member 2 | `ridelink_driver_db` |
| 3 | Ride Management Service | Member 3 | `ridelink_ride_db` |
| 4 | Fare & Payment Service | Member 4 | `ridelink_payment_db` |

This service **does NOT** directly access `ridelink_driver_db`, `ridelink_ride_db`, or `ridelink_payment_db`. Other services communicate with Account Service via stable User IDs through REST APIs.

---

## Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 3.5.10 |
| Build Tool | Maven 3.9.9 (via `mvnw.cmd` wrapper) |
| Database | MongoDB Atlas (`ridelink_account_db`) |
| Authentication | Stateless JWT (JJWT 0.12.6) |
| Security | Spring Security 6 (`SecurityFilterChain`) |
| API Documentation | SpringDoc OpenAPI 3 / Swagger UI 2.8.5 |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| Logging | SLF4J + Logback |
| Serialization | Jackson |

---

## Prerequisites

- **Java 25** (LTS) — [Download Oracle JDK 25](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** — OR use the included `./mvnw.cmd` / `./mvnw` wrapper (no installation required)
- **MongoDB Atlas** — A valid cluster URI for `ridelink_account_db`
- **Git** — For version control

---

## Environment Variables

The service requires these environment variables. **Never hardcode secrets.**

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | Full MongoDB Atlas connection URI | `mongodb+srv://...` |
| `JWT_SECRET` | HMAC-SHA256 signing secret (min 32 chars) | `YourLong...Secret` |
| `JWT_EXPIRATION` | Token lifespan in milliseconds | `86400000` (24h) |
| `PORT` | Server port (optional, defaults to 8081) | `8081` |

### Configuration Setup

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your actual credentials:
   ```env
   MONGODB_URI=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/ridelink_account_db?retryWrites=true&w=majority
   JWT_SECRET=YourLongRandomSecretAtLeast32CharactersHere
   JWT_EXPIRATION=86400000
   ```

3. Load the environment before running:
   ```powershell
   # PowerShell
   Get-Content .env | ForEach-Object {
     if ($_ -match '^([^#][^=]+)=(.+)$') {
       [Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), 'Process')
     }
   }
   ```
   ```bash
   # Bash/Linux/macOS
   export $(grep -v '^#' .env | xargs)
   ```

> **Security Note:** `.env` is in `.gitignore` and must **never be committed to source control**.

---

## How to Run

### Using Maven Wrapper (Recommended)

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

### Using System Maven

```bash
mvn spring-boot:run
```

The service starts at: **http://localhost:8081**

---

## How to Test

```bash
# Run all tests
.\mvnw.cmd test          # Windows
./mvnw test              # Linux/macOS

# Build JAR (includes tests)
.\mvnw.cmd clean package
```

**Test results:** 62 tests, 0 failures, 0 errors.

---

## Swagger / API Documentation

| URL | Description |
|-----|-------------|
| `http://localhost:8081/swagger-ui/index.html` | Interactive Swagger UI |
| `http://localhost:8081/v3/api-docs` | Raw OpenAPI 3 JSON |

### Authenticating in Swagger UI

1. Call `POST /api/v1/auth/login` to obtain a JWT token
2. Click **Authorize** (🔒) at the top of Swagger UI
3. Enter: `Bearer <your_token>`
4. All protected endpoints are now accessible

---

## API Endpoint Reference

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | Register a new user (PASSENGER or DRIVER) |
| `POST` | `/api/v1/auth/login` | Authenticate and receive JWT token |

### User Profile (Authenticated)

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/api/v1/users/me` | Get own profile | Any authenticated |
| `PUT` | `/api/v1/users/me` | Update own profile | Any authenticated |

### User CRUD

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `POST` | `/api/v1/users` | Create a user (any role) | ADMIN |
| `GET` | `/api/v1/users` | List all users | ADMIN |
| `GET` | `/api/v1/users/{id}` | Get user by ID | ADMIN or self |
| `PUT` | `/api/v1/users/{id}` | Update user by ID | ADMIN or self |
| `DELETE` | `/api/v1/users/{id}` | Delete user | ADMIN |

### Account Status (Admin Only)

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `PATCH` | `/api/v1/users/{id}/status` | Change account status | ADMIN |

---

## Example API Requests

### Register a Passenger
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Kasun Perera",
    "email": "kasun.perera@example.com",
    "phone": "0771234567",
    "password": "RideLink@2026",
    "role": "PASSENGER"
  }'
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "64f1a2b3c4d5e6f7a8b9c0d1",
      "fullName": "Kasun Perera",
      "email": "kasun.perera@example.com",
      "phone": "+94771234567",
      "role": "PASSENGER",
      "status": "ACTIVE",
      "createdAt": "2026-09-28T00:00:00Z",
      "updatedAt": "2026-09-28T00:00:00Z"
    }
  },
  "timestamp": "2026-09-28T00:00:00Z"
}
```

### Login
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "kasun.perera@example.com",
    "password": "RideLink@2026"
  }'
```

### Get Own Profile (Authenticated)
```bash
curl -X GET http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer <your_token>"
```

### Update Own Profile
```bash
curl -X PUT http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Kasun M. Perera",
    "phone": "0779876543"
  }'
```

### Suspend Account (Admin Only)
```bash
curl -X PATCH http://localhost:8081/api/v1/users/{id}/status \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "SUSPENDED"}'
```

---

## Error Response Format

All error responses follow a consistent format:

```json
{
  "timestamp": "2026-09-28T00:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed for one or more fields",
  "path": "/api/v1/auth/register",
  "fieldErrors": {
    "email": "Invalid email format",
    "password": "Password must contain at least one uppercase letter..."
  }
}
```

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK — Successful GET, update, login |
| `201` | Created — Successful registration or resource creation |
| `204` | No Content — Successful deletion |
| `400` | Bad Request — Validation failure, malformed JSON |
| `401` | Unauthorized — Missing/invalid/expired JWT |
| `403` | Forbidden — Insufficient permissions, suspended/deactivated account |
| `404` | Not Found — User not found |
| `409` | Conflict — Duplicate email |
| `500` | Internal Server Error — Unexpected server failure |

---

## Roles and Access Control

| Role | Description | Default Registration |
|------|-------------|---------------------|
| `PASSENGER` | Standard ride-requesting user | ✅ Default (if role omitted) |
| `DRIVER` | Registered driver offering rides | ✅ Allowed via public registration |
| `ADMIN` | System administrator | ❌ **Not allowed via public registration** |

> **Security Rule:** Any public registration attempt with `"role": "ADMIN"` is rejected with **HTTP 400 Bad Request**. ADMIN accounts must be created through the `POST /api/v1/users` endpoint by an existing admin.

---

## Account Status

| Status | Description | Can Authenticate? |
|--------|-------------|------------------|
| `ACTIVE` | Normal operational state | ✅ Yes |
| `SUSPENDED` | Temporarily blocked by admin | ❌ No (403) |
| `DEACTIVATED` | Permanently deactivated | ❌ No (403) |

---

## Database Structure

**Database:** `ridelink_account_db`  
**Collection:** `users`

```json
{
  "_id": "ObjectId (String)",
  "fullName": "String",
  "email": "String (unique, lowercase)",
  "phone": "String (E.164 format: +947XXXXXXXX)",
  "password": "String (BCrypt hash, NEVER returned in API)",
  "role": "PASSENGER | DRIVER | ADMIN",
  "status": "ACTIVE | SUSPENDED | DEACTIVATED",
  "createdAt": "ISODate",
  "updatedAt": "ISODate"
}
```

---

## Validation Rules

### Password
- Minimum 8 characters, maximum 64 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (`@$!%*?&#^()_+-=[]{}`)

### Email
- Required and must be a valid email format
- Automatically normalized to lowercase
- Must be unique across the system

### Phone Number (Sri Lankan Mobile)
- Supports formats: `+94771234567`, `0771234567`, `771234567`
- Normalized to E.164 international format (`+947XXXXXXXX`)
- Must be a valid Sri Lankan mobile number prefix (7X...)

### Full Name
- Minimum 2 characters, maximum 100 characters
- Required

---

## Security Design Decisions

| Decision | Rationale |
|----------|-----------|
| **BCrypt (strength 12)** | Industry-standard adaptive hashing for passwords; computationally expensive to brute-force |
| **Stateless JWT** | Enables horizontally scalable microservices without server-side session storage |
| **CSRF disabled** | Safe for stateless REST APIs using Bearer tokens; CSRF attacks require cookie-based sessions |
| **Independent DB** | Prevents tight coupling between microservices; each service owns its data boundary |
| **DTOs instead of entities** | Prevents accidental password/secret exposure; stable API contracts |
| **RBAC via Spring Security** | Centralized, declarative authorization rather than scattered manual checks |
| **Email normalization** | Prevents duplicate registrations from case variations (e.g., User@Email.com vs user@email.com) |

---

## Project Structure

```
account-service/
├── pom.xml
├── mvnw.cmd / mvnw
├── .env.example
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/ridelink/accountservice/
    │   │   ├── AccountServiceApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── MongoConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   └── UserController.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   └── JwtService.java
    │   │   ├── repository/
    │   │   │   └── UserRepository.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Role.java
    │   │   │   └── AccountStatus.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── UpdateProfileRequest.java
    │   │   │   │   ├── UpdateAccountStatusRequest.java
    │   │   │   │   └── CreateUserRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java
    │   │   │       ├── UserResponse.java
    │   │   │       ├── RegisterResponse.java
    │   │   │       └── LoginResponse.java
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   ├── CustomAccessDeniedHandler.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   └── CustomUserDetails.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── InvalidCredentialsException.java
    │   │   │   ├── AccountDisabledException.java
    │   │   │   └── BadRequestException.java
    │   │   └── util/
    │   │       └── ValidationUtils.java
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/ridelink/accountservice/
        │   ├── controller/
        │   │   ├── AuthControllerTest.java
        │   │   └── UserControllerTest.java
        │   ├── service/
        │   │   ├── AuthServiceTest.java
        │   │   ├── JwtServiceTest.java
        │   │   └── UserServiceTest.java
        │   └── util/
        │       └── ValidationUtilsTest.java
        └── resources/
            ├── application-test.yml
            └── mockito-extensions/
                └── org.mockito.plugins.MockMaker
```

---

## Inter-Service Integration Notes

When other microservices need to reference a user:
- Use the **stable `id` field** (MongoDB ObjectId as String)
- Call `GET /api/v1/users/{id}` with a valid service-level JWT to verify user existence/role
- **Never share databases** — store only the `userId` reference in other service databases
- Future: A shared JWT secret or service-to-service API key can be configured for internal calls

---

## IT3130 Assignment Notes

- **IT3130 Application Development Group Assignment – RideLink**
- **Member 1 – Account Service**
- Built with Java 25, Spring Boot 3.5.10, MongoDB Atlas, JWT
- All 62 tests pass (`mvnw test`)
- Full JAR artifact: `target/account-service-1.0.0.jar`
- Swagger UI available at: `http://localhost:8081/swagger-ui/index.html`

# RideLink Fare & Payment Service

Spring Boot microservice for fare estimates, saved final fares, simulated payment recording, and receipt retrieval. It runs independently on port `8084` and stores data in its own MongoDB database (`ridelink_payment_db`).

## Requirements

- Java 25
- MongoDB

## Run

Set `JWT_SECRET` to the same secret used by Account Service. `PAYMENT_MONGODB_URI` may point to a local MongoDB instance or the service's dedicated database in a shared cluster.

```powershell
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8084/swagger-ui/index.html`  
OpenAPI JSON: `http://localhost:8084/v3/api-docs`

## API

All business endpoints require a valid Account Service bearer token.

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/fares/estimate` | Authenticated | Estimate from pickup, destination, and distance |
| POST | `/api/fares/final` | Driver or admin | Save the final fare for a ride |
| POST | `/api/payments` | Passenger or admin | Record a simulated payment for a saved final fare |
| GET | `/api/payments/{paymentId}` | Passenger or admin | Retrieve payment details |
| GET | `/api/receipts/{rideId}` | Passenger or admin | Retrieve the successful payment receipt |

The current fare rule is `LKR 100.00` base fare plus `LKR 80.00` per kilometre. The service records simulated payments as successful; it does not connect to a payment provider.

## Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8084` | HTTP port |
| `PAYMENT_MONGODB_URI` | `mongodb://localhost:27017/ridelink_payment_db` | Mongo connection URI |
| `PAYMENT_DB_NAME` | `ridelink_payment_db` | Service database name |
| `JWT_SECRET` | Development shared secret | Must match Account Service |


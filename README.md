# Payment Gateway Service

A Spring Boot microservice that exposes a JWT-secured REST API for processing, capturing, refunding, and cancelling payments. Supports pluggable gateway providers (Stripe, Razorpay, or a built-in mock) and runs on H2 (dev) or Postgres (prod).

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Bean Validation
- **Spring Security + JWT (jjwt 0.12)**
- **Stripe Java SDK** + **Razorpay Java SDK**
- H2 (dev profile) / **Postgres** (postgres profile)
- springdoc-openapi (Swagger UI), Spring Boot Actuator
- Lombok, JUnit 5

## Profiles

| Profile | Activate with | DB |
|---|---|---|
| `dev` (default) | `SPRING_PROFILES_ACTIVE=dev` | H2 in-memory |
| `postgres` | `SPRING_PROFILES_ACTIVE=postgres` | Postgres |

## Gateway Providers

Select provider via `PAYMENT_PROVIDER` env var (or `payment.gateway.provider` property):

| Value | Behavior |
|---|---|
| `mock` *(default)* | In-process stub for local testing |
| `stripe` | Uses Stripe PaymentIntents (manual capture flow) |
| `razorpay` | Uses Razorpay Orders + Payments + Refunds |

Stripe and Razorpay clients are activated by `@ConditionalOnProperty` — only one is loaded at runtime.

## Quick Start (H2 + mock)

```bash
mvn spring-boot:run
```

## Quick Start (Postgres + Razorpay)

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=postgres \
PAYMENT_PROVIDER=razorpay \
RAZORPAY_KEY_ID=rzp_test_xxx \
RAZORPAY_KEY_SECRET=yyy \
mvn spring-boot:run
```

Or the whole stack via Docker Compose:

```bash
PAYMENT_PROVIDER=stripe STRIPE_API_KEY=sk_test_xxx docker compose up --build
```

## Authentication

All `/api/v1/payments/**` endpoints require a JWT.

1. **Login** (default user is `admin` / `admin123` — override via `ADMIN_USER` / `ADMIN_PASS`):

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer" }
```

2. **Call payment APIs** with the token:

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "orderId":"ord-1001","customerId":"cust-42","amount":1499.00,"currency":"INR","method":"CREDIT_CARD" }'
```

## Endpoints

| Method | Path | Auth |
|---|---|---|
| POST | `/api/v1/auth/login` | public |
| POST | `/api/v1/payments` | JWT |
| GET | `/api/v1/payments/{id}` | JWT |
| GET | `/api/v1/payments` | JWT |
| POST | `/api/v1/payments/{id}/capture` | JWT |
| POST | `/api/v1/payments/{id}/refund` | JWT |
| POST | `/api/v1/payments/{id}/cancel` | JWT |
| GET | `/actuator/health` | public |
| GET | `/swagger-ui.html` | public |

## Environment Variables

| Var | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` (H2) or `postgres` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/paymentdb` | Postgres JDBC URL |
| `DB_USER` / `DB_PASSWORD` | `payment` / `payment` | Postgres creds |
| `JWT_SECRET` | dev value | Base64-encoded 256-bit HMAC secret (generate with `openssl rand -base64 32`) |
| `JWT_EXPIRATION_MIN` | `60` | Token TTL |
| `ADMIN_USER` / `ADMIN_PASS` | `admin` / `admin123` | Demo in-memory user |
| `PAYMENT_PROVIDER` | `mock` | `mock` / `stripe` / `razorpay` |
| `STRIPE_API_KEY` | — | Required when provider=stripe |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | — | Required when provider=razorpay |

## Build & Test

```bash
mvn clean verify
```

## Replacing the In-Memory User Store

`SecurityConfig` wires an `InMemoryUserDetailsManager`. For production, replace with a JPA-backed `UserDetailsService` (e.g., a `User` entity + `UserRepository`).

## License



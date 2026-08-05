# RailEasy — Backend (Spring Boot + H2)

Spring Boot REST API for the RailEasy train ticket booking app.

- **Java:** 21
- **Framework:** Spring Boot 3.3.x (Web, Data JPA, Validation, Security)
- **Database:** H2 (file-based, dev profile)
- **Auth:** Stateless JWT (from Sprint 2)
- **Docs:** springdoc-openapi (Swagger UI)

## Prerequisites

- JDK 21 (`java -version` should report 21)
- Maven 3.9+ (or use the bundled wrapper `./mvnw`)

> **Note:** If Java/Maven are not yet installed, install a JDK 21 (e.g. Eclipse Temurin)
> and Maven, then the commands below will work.

## Run

```bash
cd backend
./mvnw spring-boot:run        # starts on http://localhost:8080 (profile: dev)
```

Useful URLs once running:

- Health check: http://localhost:8080/api/health
- Swagger UI:   http://localhost:8080/swagger-ui.html
- H2 console:   http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/raileasy`
  - User: `sa` / Password: *(empty)*

## Test

```bash
./mvnw test                   # JUnit 5 + Mockito; coverage via JaCoCo (target/site/jacoco)
```

## Project structure

```
src/main/java/com/raileasy/
├── RailEasyApplication.java
├── config/     SecurityConfig, OpenApiConfig, CorsConfig
├── common/     HealthController (+ error handling, PnrGenerator later)
├── user/       (Sprint 2)
├── train/      (Sprint 1)
├── schedule/   (Sprint 1)
└── booking/    (Sprint 3)
```

## Profiles

- `dev` (default): H2 file DB, `ddl-auto=create`, seed via `data.sql`.

## Seed data & admin login

- `data.sql` seeds **2 trains** and **2 schedules** (Chennai Central → Mumbai CSMT, 2025-10-21).
- The **admin user** is seeded at startup by `config/DataSeeder` (a `CommandLineRunner`) so the
  password is BCrypt-hashed at runtime — no hardcoded hash.
  - **Email:** `admin@raileasy.com`  **Password:** `Admin@123`
- Register passengers via `POST /api/auth/register`.

## API overview

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/register`, `/login`, `/logout` |
| Trains | `GET /api/trains` (public); `POST/PUT/DELETE /api/trains` (admin) |
| Schedules | `GET /api/schedules` (search), `/{id}/seats` (public); `GET /api/schedules/all`, `POST/PUT/DELETE` (admin) |
| Bookings | `POST /api/bookings`, `GET /api/bookings/mine`, `PUT /api/bookings/{id}/cancel` (auth) |

Import `../postman/RailEasy.postman_collection.json`; run **Auth → login (admin)** first to
capture the JWT into the `{{token}}` collection variable used by protected requests.

## Sprint progress

- [x] **Sprint 0** — Scaffolding: Maven project, H2 config, CORS, Swagger, security baseline, health endpoint.
- [x] **Sprint 1** — Train/Schedule entities + repositories; `GET /api/trains`; `GET /api/schedules` search (availability + fares); `GET /api/schedules/{id}/seats`; seed data; global error handling; `ScheduleService` unit tests.
- [x] **Sprint 2** — Auth (JWT), `User` entity, `AuthController` (register/login/logout), Spring Security (BCrypt + JWT filter), admin user seed, route protection.
- [x] **Sprint 3** — Bookings (`BookingService` with PNR + double-book guard, mine, cancel), Train + Schedule admin CRUD, `BookingSeatsAdapter` (real availability), `BookingServiceTest`.

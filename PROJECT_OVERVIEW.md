# RailEasy — Project Overview

> Reactive train ticket reservation system (full‑stack POC)
> **Team 22 · Capstone (FSE Java)**

---

## 1. Executive Summary

**RailEasy** is a production‑style, full‑stack train ticket reservation application.
It lets travellers **search trains**, **view live seat availability**, **book 1–4 seats**,
**manage their bookings**, and **download a PDF e‑ticket**, while **administrators** manage
the train catalogue and journey schedules.

The system is split into two independently deployable applications:

| Layer | Technology | Responsibility |
|-------|-----------|----------------|
| **Backend** | Java 21 · Spring Boot 4.1 · **Spring WebFlux** (fully reactive) | Secure REST API, business logic, persistence, JWT auth, PDF tickets |
| **Frontend** | Angular 17 (standalone components, signals) | Responsive SPA consuming the REST API |

The backend is **end‑to‑end non‑blocking** (`Mono`/`Flux`) using **Spring Data R2DBC**
over an in‑memory **H2** database, secured with **reactive Spring Security + JWT**.

---

## 2. Key Features

### Traveller (USER)
- Register / login (JWT‑based authentication).
- Search trains by **origin, destination and journey date**.
- View a **seat map** (8×8 = 64 seats per class) with live availability.
- Book **1–4 seats** per booking across three travel classes.
- View **My Bookings** (newest first) and **cancel** bookings.
- **Download a PDF e‑ticket** (PNR, seats, fare, journey details).

### Administrator (ADMIN)
- Manage **Trains** (create, update, delete).
- Manage **Schedules** (create, update, delete).
- All admin operations are role‑guarded on both the API and the UI.

### Cross‑cutting
- Centralised, consistent error responses via a global exception handler.
- OpenAPI / Swagger UI documentation.
- In‑memory **Caffeine cache** for near‑static train reference data.
- Seeded demo data (trains, schedules, default admin).

---

## 3. Technology Stack

### Backend (`backend/railEasy`)
- **Language / Runtime:** Java 21
- **Framework:** Spring Boot 4.1.0, Spring **WebFlux** (reactive REST)
- **Persistence:** Spring Data **R2DBC** + **H2** (in‑memory, reactive driver)
- **Security:** Reactive Spring Security + **JWT** (JJWT 0.12.6), **BCrypt** hashing
- **Validation:** Jakarta Bean Validation
- **Mapping:** **MapStruct** 1.6.3 (compile‑time entity ⇄ DTO)
- **Boilerplate:** Lombok
- **Caching:** **Caffeine** 3.1.8
- **PDF:** **OpenPDF** 2.0.3 (e‑ticket generation)
- **API Docs:** springdoc‑openapi (Swagger UI)
- **Testing:** JUnit 5, Mockito, Reactor **StepVerifier**, **JaCoCo** coverage
- **Build / Deploy:** Maven, multi‑stage **Dockerfile** (Temurin 21)

### Frontend (`frontend/railEasyUI`)
- **Framework:** Angular 17.3 (standalone components, **signals**, lazy‑loaded routes)
- **Language:** TypeScript 5.4
- **Reactivity:** RxJS 7.8
- **Auth:** JWT stored in `localStorage`, HTTP interceptor, route guards
- **Testing:** Karma + Jasmine
- **Build:** Angular CLI

---

## 4. System Architecture

### 4.1 High‑level

```
┌──────────────────────────┐        HTTPS / JSON        ┌──────────────────────────────┐
│   Angular 17 SPA          │  ───────────────────────▶ │   Spring WebFlux REST API   │
│   (railEasyUI)            │   Bearer <JWT>            │   (railEasy)                 │
│                           │  ◀─────────────────────── │                              │
│  - Search / Seat select   │        JSON / PDF         │  Controller → Service →      │
│  - Bookings / Admin       │                           │  Repository (R2DBC) → H2     │
└──────────────────────────┘                            └──────────────────────────────┘
        localStorage JWT                                     JWT filter · BCrypt · Caffeine
```

### 4.2 Backend layered (clean architecture)

```
controller  → Reactive REST endpoints (WebFlux)
service      → Business logic (interfaces + impl)
repository   → ReactiveCrudRepository (R2DBC)
dto          → Request / response models
entity       → R2DBC table mappings
mapper       → Entity ⇄ DTO (MapStruct)
security     → JWT filter chain, auth manager, handlers
config       → Security, OpenAPI, CORS, H2 console, seed data
exception    → Custom exceptions + @RestControllerAdvice
util         → JwtService, PnrGenerator, SecurityUtils, TicketPdfGenerator, SeatUtils
constants    → AppConstants, Role, BookingStatus, TravelClass
cache        → TrainCache (Caffeine)
```

### 4.3 Frontend structure

```
src/app/
  core/
    guards/        auth.guard.ts, admin.guard.ts
    interceptors/  auth.interceptor.ts (attaches Bearer token)
    models/        auth, booking, schedule, train, travel-class
    services/      auth, booking, schedule, train, toast
    utils/         http-error.util.ts
  features/
    auth/          login, register
    schedules/     search, seat-selection
    bookings/      my-bookings
    admin/         trains, schedules
  layout/          navbar
  shared/          toast-host
```

---

## 5. Data Model

### 5.1 Entities & Relationships
- **Train** `(1) —— (M)` **Schedule**
- **User** `(1) —— (M)` **Booking**
- **Schedule** `(1) —— (M)` **Booking**
- **Booking** `(1) —— (M)` **BookingSeat**

### 5.2 Tables (H2)

| Table | Key columns | Notes |
|-------|-------------|-------|
| `users` | `id`, `full_name`, `email` (unique), `password`, `role`, `created_at` | BCrypt‑hashed password; role = USER / ADMIN |
| `trains` | `id`, `train_number` (unique), `train_name`, `total_seats_per_class`, `active` | 64 seats per class |
| `schedules` | `id`, `train_id` (FK), `from_station`, `to_station`, `departure_time`, `arrival_time`, `journey_date`, `fare_sleeper`, `fare_ac3`, `fare_ac2` | Unique `(train_id, journey_date, departure_time)` |
| `bookings` | `id`, `pnr` (unique), `user_id` (FK), `schedule_id` (FK), `travel_class`, `total_fare`, `status`, `booked_at` | PNR = first 8 chars of a UUID |
| `booking_seats` | `id`, `booking_id` (FK), `schedule_id`, `travel_class`, `seat_no` | **Unique `(schedule_id, travel_class, seat_no)`** — authoritative anti‑double‑booking guard |

### 5.3 Domain rules
- **Travel classes:** `SLEEPER`, `AC_3`, `AC_2`.
- **Seat layout:** 8×8 = **64 seats** per class, labels `1A`..`8H`.
- **Seats per booking:** **1 to 4**.
- **Availability** is computed from `booking_seats` via `COUNT`/`GROUP BY`; cancelling a
  booking deletes its seat rows, freeing the seats.
- Indexes exist on the hot read paths (seat availability, user's bookings, schedule search).

---

## 6. REST API Reference

Base path: `/api/v1`

| Module | Method & Path | Access |
|--------|---------------|--------|
| **Auth** | `POST /auth/register` | Public |
| Auth | `POST /auth/login` | Public |
| Auth | `POST /auth/logout` | Public |
| **Schedule** | `GET /schedules?from=&to=&date=` | Public (train search) |
| Schedule | `GET /schedules/{id}` | Public |
| Schedule | `GET /schedules/{id}/seats?class=` | Public (seat map) |
| Schedule | `POST /schedules` | ADMIN |
| Schedule | `PUT /schedules/{id}` | ADMIN |
| Schedule | `DELETE /schedules/{id}` | ADMIN |
| **Train** | `GET /trains` | Authenticated |
| Train | `GET /trains/{id}` | Authenticated |
| Train | `POST /trains` | ADMIN |
| Train | `PUT /trains/{id}` | ADMIN |
| Train | `DELETE /trains/{id}` | ADMIN |
| **Booking** | `POST /bookings` | Authenticated (USER/ADMIN) |
| Booking | `GET /bookings/mine` | Authenticated |
| Booking | `GET /bookings/{id}` | Authenticated |
| Booking | `PUT /bookings/{id}/cancel` | Authenticated |
| Booking | `GET /bookings/{id}/ticket` (PDF) | Authenticated |

**Public paths** (no auth): `/api/v1/auth/**`, Swagger (`/v3/api-docs/**`, `/swagger-ui/**`), H2 console.

---

## 7. Security

- **Stateless JWT** authentication (reactive filter chain via `JwtSecurityContextRepository`).
- **BCrypt** password hashing.
- **Role‑based authorization** (`USER`, `ADMIN`) enforced at the API and mirrored by Angular
  route guards (`authGuard`, `adminGuard`).
- Configurable **JWT secret / expiry / issuer** — token expiry default **1 hour**; secret
  overridable via `RAILEASY_JWT_SECRET`.
- **CORS** configured for local dev origins (`:3000`, `:4200`, `:5173`).
- Custom `JwtAuthenticationEntryPoint` (401) and `JwtAccessDeniedHandler` (403) return
  consistent JSON errors.

---

## 8. Typical User Flows

**Booking flow (traveller)**
1. Register / login → receive JWT.
2. Search trains (`from`, `to`, `date`).
3. Open a schedule → view seat map → pick 1–4 seats and a travel class.
4. Confirm booking → PNR generated, seats reserved, fare computed.
5. View in **My Bookings**; download **PDF e‑ticket**; cancel if needed.

**Admin flow**
1. Login with seeded admin account.
2. Create/update/delete **Trains**.
3. Create/update/delete **Schedules** (with fares per class).

---

## 9. Running the Project

### Backend
```bash
cd backend/railEasy
./mvnw spring-boot:run
```
- App: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- H2 console (dev): <http://localhost:8082> — JDBC URL `jdbc:h2:tcp://localhost:9092/mem:raileasy`, user `sa`

**Seeded admin:** `admin@raileasy.com` / `password`

### Frontend
```bash
cd frontend/railEasyUI
npm install
npm start           # ng serve → http://localhost:4200
```
- API base URL configured in `src/environments/environment.ts` → `http://localhost:8080/api/v1`

### Docker (backend)
Multi‑stage build (Maven + Temurin 21 JRE), exposes port `8080`.
```bash
cd backend/railEasy
docker build -t raileasy .
docker run -p 8080:8080 raileasy
```

---

## 10. Testing & Quality

**Backend** — JUnit 5 + Mockito + Reactor `StepVerifier` (no DB / Spring context for service tests):
- `AuthServiceImplTest`, `TrainServiceImplTest`, `ScheduleServiceImplTest`, `BookingServiceImplTest`
- `TicketPdfGeneratorTest`, `TrainMapperTest`, `ScheduleMapperTest`, `BookingMapperTest`
- `JwtServiceTest`, `GlobalExceptionHandlerTest`, `SecurityUtilsTest`, `SeatUtilsTest`, `PnrGeneratorTest`

```bash
./mvnw test        # report: target/site/jacoco/index.html (JaCoCo)
```

**Frontend** — Karma + Jasmine:
```bash
ng test
```

Also included: a ready‑to‑use **Postman collection** at `docs/RailEasy.postman_collection.json`
(auto‑captures JWT and booking ids across requests).

---

## 11. Seeded Demo Data

- **Trains:** Tamil Nadu Express (12621), Chennai Mumbai Express (11041),
  Mumbai Rajdhani (12951), Shatabdi Express (12009) — 64 seats/class each.
- **Schedules:** incl. two Chennai → Mumbai journeys plus Mumbai → Delhi and Ahmedabad → Mumbai.
- **Admin:** `admin@raileasy.com` / `password` (BCrypt‑hashed).

---

## 12. Highlights for Presentation

- **Fully reactive** stack end‑to‑end (`Mono`/`Flux`, R2DBC) — non‑blocking I/O.
- **Modern Angular 17** with standalone components, **signals**, and lazy‑loaded routes.
- **Strong data integrity:** a DB‑level unique constraint prevents seat double‑booking.
- **Security done right:** stateless JWT, BCrypt, role‑based access on API + UI.
- **Developer experience:** Swagger UI, Postman collection, seeded data, Docker build.
- **Well tested:** unit tests across services, mappers, utils + JaCoCo coverage.

---

*Document generated from the RailEasy backend (`backend/railEasy`) and frontend
(`frontend/railEasyUI`) source. Suitable as the basis for a project presentation.*

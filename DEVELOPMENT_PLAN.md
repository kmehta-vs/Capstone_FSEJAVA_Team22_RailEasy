# RailEasy — Train Ticket Booking — Full-Stack Development Plan

> **Purpose of this document:** A detailed, phase-by-phase engineering guide for building the complete
> RailEasy full-stack application with AI coding agents. It translates the capstone requirements
> (`P3_RailEasy_TrainTicket.pdf`) into an executable plan tailored to the chosen tech stack.

---

## 1. Project Overview

**RailEasy (Foundation)** is a train ticket booking web application where:

- **Passengers** search trains between stations, view class availability, select seats, confirm bookings (with PNR), view "My Tickets", and cancel tickets.
- **Admins** manage trains and schedules (CRUD).

The focus is on **foundational full-stack skills**: Angular, Spring Boot, REST, JPA, Git, and basic testing.

### Chosen Tech Stack (overrides PDF suggestions)

| Layer | Choice | Notes |
|-------|--------|-------|
| **Database** | **H2** (in-memory + file mode) | Single DB for dev & demo. No PostgreSQL. Use `H2` UUID/`RANDOM_UUID()` semantics. |
| **Backend** | **Java 21**, **Spring Boot 3.3+** | Spring Web, Spring Data JPA, Validation, Spring Security (BCrypt + JWT), JUnit 5, Mockito. |
| **Frontend** | **Angular 21** (standalone components, signals), **Angular Material** | Routing, Services, HttpClient interceptors, reactive forms. |
| **Auth** | **Stateless JWT** | Chosen over sessions — documented trade-off below. |
| **API Docs** | **springdoc-openapi (Swagger UI)** | Plus a Postman collection. |
| **Deployment** | Localhost | BE on `:8080`, FE on `:4200`. |

### Auth Trade-off (JWT vs Session) — *documented decision*

We use **stateless JWT** because:
- No server-side session store needed → simpler for a single-node localhost app.
- Works cleanly with Angular's HTTP interceptor (attach `Authorization: Bearer <token>`).
- Trade-off: tokens can't be trivially revoked before expiry; mitigated with short expiry (e.g., 24h) and client-side logout (token discard).

---

## 2. High-Level Architecture

```
┌─────────────────────────────┐        HTTP/JSON (REST)        ┌──────────────────────────────┐
│   Angular 21 SPA (:4200)    │  ───────────────────────────▶ │  Spring Boot API (:8080)     │
│  - Standalone components    │  ◀─────────────────────────── │  - Controllers → Services →  │
│  - Angular Material UI      │        JWT Bearer token        │    Repositories (JPA)        │
│  - Auth/JWT interceptor     │                                │  - Spring Security (BCrypt)  │
│  - Route guards             │                                │  - Bean Validation           │
└─────────────────────────────┘                                │  - Global exception handler  │
                                                               └──────────────┬───────────────┘
                                                                              │ JPA/Hibernate
                                                                     ┌────────▼────────┐
                                                                     │   H2 Database   │
                                                                     │ (file-based dev)│
                                                                     └─────────────────┘
```

**Backend layering (SOLID basics):** `Controller` (thin) → `Service` (business logic) → `Repository` (data). DTOs cross the API boundary; entities never leak to the client.

---

## 3. Repository Structure

```
Capstone_FSEJAVA_Team22_RailEasy/
├── DEVELOPMENT_PLAN.md          ← this file
├── P3_RailEasy_TrainTicket.pdf
├── backend/
│   ├── readme.md
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/raileasy/
│       │   ├── RailEasyApplication.java
│       │   ├── config/          (SecurityConfig, OpenApiConfig, CorsConfig, DataSeeder)
│       │   ├── security/        (JwtService, JwtAuthFilter, CustomUserDetailsService)
│       │   ├── common/          (ApiError, GlobalExceptionHandler, PnrGenerator)
│       │   ├── user/            (User, UserRepository, AuthController, AuthService, dtos)
│       │   ├── train/           (Train, TrainRepository, TrainController, TrainService, dtos)
│       │   ├── schedule/        (Schedule, ScheduleRepository, ScheduleController, ScheduleService, dtos)
│       │   └── booking/         (Booking, BookingRepository, BookingController, BookingService, dtos)
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   └── data.sql         (seed data)
│       └── test/java/com/raileasy/   (unit tests: services + repos)
└── frontend/
    ├── readme.md
    └── raileasy-ui/             (ng new)
        └── src/app/
            ├── core/            (auth service, jwt interceptor, guards, api models, error handling)
            ├── shared/          (material module, toast, layout, reusable components)
            └── features/
                ├── auth/        (login, register)
                ├── search/      (home, search form, results)
                ├── booking/     (class-select, seat-grid, confirm, my-tickets)
                └── admin/       (trains-manage, schedules-manage)
```

---

## 4. Data Model

### Entities & Relationships

- **Train (1) — (M) Schedule**
- **User (1) — (M) Booking**
- **Schedule (1) — (M) Booking**

### Entity fields

**User**
| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | PK |
| `email` | String | unique, not null |
| `passwordHash` | String | BCrypt |
| `name` | String | |
| `isAdmin` | boolean | default `false` |
| `createdAt` | Instant | |

**Train**
| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | PK |
| `trainNumber` | String | unique, e.g. `12163` |
| `trainName` | String | e.g. `Chennai Express` |
| `seatsPerClass` | int | default `64` (8×8) |
| `createdAt` | Instant | |

**Schedule**
| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | PK |
| `train` | Train (M:1) | FK |
| `fromStation` | String | |
| `toStation` | String | |
| `departureTime` | LocalDateTime | |
| `arrivalTime` | LocalDateTime | |
| `journeyDate` | LocalDate | |
| `fareSleeper` | BigDecimal | |
| `fareAc3` | BigDecimal | |
| `fareAc2` | BigDecimal | |
| `createdAt` | Instant | |

**Booking**
| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | PK |
| `user` | User (M:1) | FK |
| `schedule` | Schedule (M:1) | FK |
| `travelClass` | Enum `SLEEPER, AC_3, AC_2` | |
| `seatNumbers` | String (CSV, e.g. `1A,1B`) | booked seats for this booking |
| `pnrNumber` | String | UUID first 8 chars, uppercase |
| `status` | Enum `CONFIRMED, CANCELLED` | |
| `createdAt` | Instant | |

### Key derivations (per PDF)
- **Seat availability** for a class = `train.seatsPerClass` − (count of seats across all `CONFIRMED` bookings for that schedule+class).
- **Seat grid**: 8 rows × 8 seats = 64 per class. Seat labels: rows `1..8`, columns `A..H` → e.g. `3C`.
- **Booked seats** for a class = union of `seatNumbers` from all `CONFIRMED` bookings of that schedule+class.
- **PNR** = `UUID.randomUUID().toString().substring(0,8).toUpperCase()`.

---

## 5. API Contract

Base path: `/api`. All non-auth mutating admin endpoints require `isAdmin`. Booking endpoints require authentication.

### Auth
| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/auth/register` | `{ email, password, name }` | `201` `{ id, email, name, isAdmin }` |
| POST | `/api/auth/login` | `{ email, password }` | `200` `{ token, user:{ id,email,name,isAdmin } }` |
| POST | `/api/auth/logout` | — | `204` (client discards token) |

### Schedules (search + admin CRUD)
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/api/schedules?from=&to=&date=` | public | search with class availability + fares |
| GET | `/api/schedules/{id}/seats?travelClass=` | public | returns booked seat numbers for that class |
| POST | `/api/schedules` | admin | create |
| PUT | `/api/schedules/{id}` | admin | update |
| DELETE | `/api/schedules/{id}` | admin | delete |

### Trains (admin)
| Method | Path | Auth |
|--------|------|------|
| GET | `/api/trains` | public (needed for schedule form) |
| POST | `/api/trains` | admin |
| PUT | `/api/trains/{id}` | admin |
| DELETE | `/api/trains/{id}` | admin |

### Bookings
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/bookings` | user | `{ scheduleId, travelClass, seatNumbers[] }` → creates booking + PNR |
| GET | `/api/bookings/mine` | user | current user's tickets |
| PUT | `/api/bookings/{id}/cancel` | user | cancel → status `CANCELLED`, seats freed |

### Consistent error format
```json
{
  "timestamp": "2025-09-12T12:00:00Z",
  "path": "/api/...",
  "error": "VALIDATION_ERROR",
  "message": "Field is required"
}
```
Error codes to support: `VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT` (e.g., seat already booked), `INTERNAL_ERROR`.

### Search response shape (example)
```json
[
  {
    "scheduleId": "…",
    "trainName": "Chennai Express",
    "trainNumber": "12163",
    "fromStation": "Chennai Central",
    "toStation": "Mumbai CSMT",
    "departureTime": "2025-10-21T06:00:00",
    "arrivalTime": "2025-10-22T05:30:00",
    "journeyDate": "2025-10-21",
    "classes": [
      { "travelClass": "SLEEPER", "fare": 450.00, "seatsAvailable": 62 },
      { "travelClass": "AC_3",    "fare": 1200.00, "seatsAvailable": 64 },
      { "travelClass": "AC_2",    "fare": 1800.00, "seatsAvailable": 60 }
    ]
  }
]
```

---

## 6. Business Rules & Validation

1. **Seat selection**: 1–4 seats per booking. Reject if fewer than 1 or more than 4.
2. **Concurrency/double-book guard**: within `BookingService.create`, re-check requested seats against currently booked seats (in a `@Transactional` method) before persisting; if any overlap → `409 CONFLICT` (`SEAT_ALREADY_BOOKED`).
3. **Cancel**: only the owning user can cancel; only `CONFIRMED` bookings can be cancelled; cancelling frees seats (status → `CANCELLED`, seats no longer counted).
4. **Admin guard**: `isAdmin` checked in Spring Security (role `ADMIN`) for train/schedule mutations.
5. **Validation**: `@NotBlank`, `@Email`, `@Positive` fares, non-null dates, valid enum values. Server-side always validates regardless of client.
6. **Fare**: total fare = per-class fare × number of seats (display in confirm screen).
7. **Password**: BCrypt hashing; never return `passwordHash`.

---

## 7. Delivery Plan (3 Sprints)

### Sprint 0 — Scaffolding (½–1 day)
- [x] Backend: `spring init` / Spring Initializr → `pom.xml` (Web, JPA, Validation, Security, H2, Lombok optional, springdoc, JUnit).
- [x] Frontend: `ng new raileasy-ui --routing --style=scss --standalone`; add Angular Material (`ng add @angular/material`). *(Angular 20 used — Angular 21 CLI needs Node ≥ 22; this machine has Node 20.)*
- [x] Configure H2 (`application-dev.yml`), enable H2 console, CORS for `:4200`.
- [x] Git: `main` + `feature/*` workflow; PR reviews. *(root/backend/frontend `.gitignore` added; workflow documented.)*
- [x] Health check endpoint + FE landing page wired to BE.

### Sprint 1 — Foundations & Train Search
- [x] **BE**: `Train`, `Schedule` entities + repositories.
- [x] **BE**: `GET /api/schedules` search (from/to/date) with availability + fares.
- [x] **BE**: `GET /api/trains`.
- [x] **BE**: Seed data (`data.sql`) — 2 trains, 2 schedules. *(admin user seeded in Sprint 2 when `users` table exists.)*
- [x] **FE**: Home + Search form (From / To / Date) + Search results cards.
- [x] **FE**: Core services + API models + HttpClient setup.
- [x] Swagger UI live; Postman collection started (`postman/RailEasy.postman_collection.json`).

### Sprint 2 — Auth & Seat Selection
- [x] **BE**: `User` entity; `POST /register`, `/login` (JWT), `/logout`.
- [x] **BE**: Spring Security config (BCrypt, JWT filter, route protection).
- [x] **BE**: `GET /api/schedules/{id}/seats?travelClass=` (booked seats). *(delivered in Sprint 1; consumed by FE seat grid now.)*
- [x] **FE**: Register/Login pages; JWT interceptor; auth guard; user context (signal-based service).
- [x] **FE**: Class selection UI; 8×8 seat grid (green=available, red=booked); select 1–4 seats; fare calc; validation + error toasts.
- [x] Protect booking routes on FE (guard) and BE.

### Sprint 3 — Booking & Admin
- [x] **BE**: `POST /api/bookings` (PNR generation, concurrency guard), `GET /api/bookings/mine`, `PUT /api/bookings/{id}/cancel`.
- [x] **BE**: Train CRUD + Schedule CRUD (admin-guarded).
- [x] **FE**: Confirm booking screen → PNR display; My Tickets table (PNR/Train/Date/Class/Seats/Status) + Cancel.
- [x] **FE**: Admin screens — Trains table + Schedules table + Add/Edit dialogs (Material). Admin menu shown only when `isAdmin`.
- [x] **Polish**: README (setup, seed, run order), Postman collection, unit tests ≥60% backend, structured errors, toasts.

---

## 8. Testing Strategy

- **Backend unit tests (JUnit 5 + Mockito)** — target **≥60% method coverage**:
  - `AuthService` (register hashing, login token, duplicate email).
  - `ScheduleService` (search filters, availability calc).
  - `BookingService` (PNR generation, seat overlap → conflict, cancel frees seats, 1–4 seat rule).
  - Repository slice tests (`@DataJpaTest`) for custom queries.
- **API testing**: Postman collection covering all endpoints (happy + error paths) and Swagger UI manual checks.
- **Frontend (optional)**: 1–2 key component tests (e.g., booking/seat-grid form).
- **Concurrency**: simulate 5–10 concurrent booking requests via Postman Runner / multiple tabs; verify no double-booking.

---

## 9. Non-Functional Requirements

| NFR | Target |
|-----|--------|
| API latency | ≤ 2 s under light load (1–5 concurrent local users) |
| UI initial render | ≤ 3 s on localhost |
| Security | BCrypt passwords; JWT stored securely (memory/localStorage with caveats); server-side validation |
| Error handling | Structured JSON on API; user-friendly Material toasts on UI |
| Logging | Console + simple request/response logs (no external infra) |
| Docs | README with setup/seed/run; Swagger/OpenAPI; Postman collection |

---

## 10. Seed Data (H2-compatible)

> The PDF SQL uses Postgres `gen_random_uuid()`/`now()`. For **H2** use `RANDOM_UUID()` and `CURRENT_TIMESTAMP`. Store UUIDs as `UUID` type (H2 supports it). Place in `src/main/resources/data.sql` and set `spring.sql.init.mode=always`. Seed the admin password as a real BCrypt hash of a known password (e.g., `Admin@123`).

```sql
-- Admin user (password = 'Admin@123' → replace with a real BCrypt hash)
INSERT INTO users (id, email, password_hash, is_admin, name, created_at)
VALUES (RANDOM_UUID(), 'admin@raileasy.com', '$2a$10$REPLACE_WITH_REAL_BCRYPT', TRUE, 'Admin', CURRENT_TIMESTAMP);

-- Trains
INSERT INTO train (id, train_number, train_name, seats_per_class, created_at) VALUES
 (RANDOM_UUID(), '12163', 'Chennai Express',  64, CURRENT_TIMESTAMP),
 (RANDOM_UUID(), '22691', 'Rajdhani Express', 64, CURRENT_TIMESTAMP);

-- Schedules (Chennai Central → Mumbai CSMT on 2025-10-21)
INSERT INTO schedule (id, train_id, from_station, to_station, departure_time, arrival_time, journey_date, fare_sleeper, fare_ac3, fare_ac2, created_at)
SELECT RANDOM_UUID(), t.id, 'Chennai Central', 'Mumbai CSMT',
       TIMESTAMP '2025-10-21 06:00:00', TIMESTAMP '2025-10-22 05:30:00', DATE '2025-10-21',
       450.00, 1200.00, 1800.00, CURRENT_TIMESTAMP
FROM train t WHERE t.train_number = '12163';

INSERT INTO schedule (id, train_id, from_station, to_station, departure_time, arrival_time, journey_date, fare_sleeper, fare_ac3, fare_ac2, created_at)
SELECT RANDOM_UUID(), t.id, 'Chennai Central', 'Mumbai CSMT',
       TIMESTAMP '2025-10-21 08:00:00', TIMESTAMP '2025-10-22 07:45:00', DATE '2025-10-21',
       500.00, 1350.00, 2000.00, CURRENT_TIMESTAMP
FROM train t WHERE t.train_number = '22691';
```

> **Tip:** Generate the BCrypt hash once via a small test or an online BCrypt tool, then paste it. Keep `spring.jpa.hibernate.ddl-auto=create` (dev) so schema is built before `data.sql` runs, or use `defer-datasource-initialization=true`.

---

## 11. Wireframes (from PDF)

- **Home**: Search panel — From Station / To Station / Date / **[Search Trains]**.
- **Search Results**: Cards — Train Name + Number / Departure–Arrival / class buttons (SLEEPER | AC3 | AC2) with fare + seats left.
- **Seat Grid**: 8×8 grid for selected class. Available = green, Booked = red. Total fare + **[Confirm Booking]**.
- **My Tickets**: Table — PNR / Train / Date / Class / Seats / Status — **[Cancel]**.
- **Admin**: Trains table + Schedules table + Add/Edit forms (Material dialogs).

---

## 12. Sample User Stories & Acceptance Criteria

**Stories**
- As a passenger, I search trains by station and date to plan my journey.
- As a passenger, I choose a travel class to book within budget.
- As a passenger, I see available seats to pick my preferred berth.
- As a passenger, I receive a PNR after booking to reference my ticket.
- As an admin, I add train schedules so passengers can book new routes.

**Acceptance Criteria (key)**
- Searching Chennai → Mumbai on a seeded date returns ≥ 2 trains, each showing classes with seat counts and fares.
- Selecting AC_3 shows an 8×8 grid with booked seats highlighted; already-booked seats cannot be selected.
- After confirming, a PNR appears on the confirmation screen.
- Cancelling a ticket sets status `CANCELLED` and frees seats.
- Only admin can access `/admin`; an added train becomes selectable when creating a schedule.

---

## 13. Definition of Ready / Done

**DoR**: story has clear AC; API/DB changes identified; UX reference/sketch exists.

**DoD**: code compiles; backend service unit tests pass; manual tests done (Postman + UI); no critical ESLint/TS issues; meaningful README update; peer review + merged to `main`.

---

## 14. Evaluation Rubric (targets to satisfy)

| Area | Weight | Focus |
|------|--------|-------|
| Frontend (Angular) | 25% | Routing, services, component communication, state (services/signals), TypeScript |
| Backend (Spring Boot) | 25% | Clean REST controllers, services, JPA entities & relationships, validation, error handling |
| Common Skills | 20% | Core Java, SQL/JPA, SOLID basics, Git hygiene, HTML/CSS semantics |
| Testing | 15% | JUnit/Mockito for services/repos; Postman collection; ≥60% coverage |
| Generative AI Usage | 10% | Copilot/ChatGPT for boilerplate/refactor; cite prompts in README |
| Documentation & README | 5% | Setup, seed data, API list, constraints & trade-offs |

---

## 15. Stretch Goals (optional)
- Waitlist status when a class is fully booked.
- Station autocomplete dropdown.
- Downloadable ticket summary page.

---

## 16. Getting Started (commands)

**Backend**
```bash
cd backend
./mvnw spring-boot:run            # runs on :8080, profile dev (H2)
# H2 console: http://localhost:8080/h2-console
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**Frontend**
```bash
cd frontend/raileasy-ui
npm install
ng serve                          # runs on :4200
```

**Run order:** 1) Start Spring Boot (H2 auto-starts + seeds) → 2) `ng serve` → 3) open `http://localhost:4200`.

---

## 17. Build Order for AI Agents (checklist)

> Follow top-to-bottom. Each item should compile/run and be committed on a `feature/*` branch with a PR.

**Phase A — Backend core**
1. Spring Boot project + `application.yml`/`application-dev.yml` (H2, JPA, CORS, springdoc).
2. `common`: `ApiError`, `GlobalExceptionHandler`, `PnrGenerator`.
3. `train`: entity, repo, DTOs, service, controller (`GET /api/trains`).
4. `schedule`: entity, repo, DTOs, service (search + availability), controller (`GET /api/schedules`, `/{id}/seats`).
5. `data.sql` seed + verify search endpoint via Swagger/Postman.

**Phase B — Auth & Security**
6. `user`: entity, repo, DTOs.
7. `security`: `JwtService`, `JwtAuthFilter`, `CustomUserDetailsService`, `SecurityConfig`.
8. `AuthController`/`AuthService`: register/login/logout.
9. Lock down admin + booking endpoints.

**Phase C — Bookings & Admin CRUD**
10. `booking`: entity, repo, DTOs, service (create+PNR+conflict guard, mine, cancel), controller.
11. Train CRUD (POST/PUT/DELETE) + Schedule CRUD (POST/PUT/DELETE), admin-guarded.
12. Backend unit tests (≥60%).

**Phase D — Frontend**
13. `ng new` + Angular Material + core (models, api services, jwt interceptor, guards, error/toast).
14. Search feature (home, form, results).
15. Auth feature (login/register) + auth state service (signals).
16. Booking feature (class select → seat grid → confirm → PNR) + My Tickets.
17. Admin feature (trains + schedules management dialogs; conditional admin menu).

**Phase E — Polish**
18. Swagger annotations + export Postman collection.
19. READMEs (backend + frontend + root), cite AI prompts used.
20. Final test pass, coverage check, concurrency check, merge to `main`.

---

*End of plan. This document is the single source of truth for building RailEasy; update it as decisions evolve.*

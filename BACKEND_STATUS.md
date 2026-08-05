# RailEasy — Backend Runtime Status & Blocked-Work Tracker

> This file tracks what must be installed/running for the **backend** to work, and which
> tasks are **blocked until the backend is actually running** on `http://localhost:8080`.
> The frontend can be developed and previewed independently using **mock mode** (see bottom).

_Last updated: Sprint 2._

---

## A. What must be running for the backend

### 1. Tooling prerequisites (install once)
- [ ] **JDK 21** on `PATH` (`java -version` → 21). *(Not currently installed on this machine.)*
- [ ] **Maven 3.9+** or use the bundled wrapper `./mvnw`. *(Not currently installed.)*
- [ ] (Optional) An IDE with Spring support (IntelliJ/VS Code Java pack).

### 2. To start the backend
```bash
cd backend
./mvnw spring-boot:run        # profile: dev (H2 file DB)
```
This single process brings up **everything the backend needs**:
- [ ] **Embedded Tomcat** on port **8080**.
- [ ] **H2 database** (file `./data/raileasy.mv.db`, auto-created).
- [ ] **Hibernate schema generation** (`ddl-auto=create`) → builds `train`, `schedule`, `users`, `booking` tables.
- [ ] **Seed data** — trains & schedules from `data.sql` (`spring.sql.init.mode=always`); the **admin user** is seeded by `config/DataSeeder` (a `CommandLineRunner`) so the password is BCrypt-hashed at runtime (no hardcoded hash). Default admin: `admin@raileasy.com` / `Admin@123`.
- [ ] **Spring Security** filter chain (JWT).
- [ ] **Swagger UI** at `/swagger-ui.html`, **H2 console** at `/h2-console`.

### 3. Quick smoke checks (once running)
- [ ] `GET http://localhost:8080/api/health` → `{ "status": "UP" }`
- [ ] Swagger UI loads and lists all controllers.
- [ ] H2 console connects with JDBC `jdbc:h2:file:./data/raileasy`, user `sa`.

---

## B. Tasks BLOCKED until the backend is up & running

These are written in code and will work, but **cannot be verified/executed** without a running BE + JDK/Maven:

| Blocked task | Sprint | Needs |
|--------------|--------|-------|
| Run `./mvnw test` (JUnit + Mockito) & check ≥60% coverage (JaCoCo) | 1–3 | JDK + Maven |
| Verify `GET /api/schedules` search returns seeded trains | 1 | BE running |
| Verify `GET /api/schedules/{id}/seats` | 1–2 | BE running |
| Register/login → receive real JWT; exercise `Authorization: Bearer` | 2 | BE running |
| Admin-guarded endpoints return 401/403 correctly | 2–3 | BE running |
| Create booking → real PNR persisted; My Tickets; cancel frees seats | 3 | BE running |
| Concurrency double-booking test (Postman Runner) | 3 | BE running |
| End-to-end FE↔BE integration with **mock mode OFF** | 1–3 | BE running |
| Postman collection run against live API | 1–3 | BE running |

> **Meanwhile:** backend code keeps advancing per the plan. When JDK 21 + Maven are installed,
> run the smoke checks above, then flip the frontend to real mode (see below) and re-test.

---

## C. Frontend mock mode (work without the backend)

To preview and develop the UI while the backend is offline, the frontend has a **mock mode**
toggled in `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  useMocks: true,   // ← true = serve sample data locally; false = call real backend
};
```

- **`useMocks: true`** → an in-memory HTTP interceptor answers `/api/*` calls with sample
  fixtures (`src/app/core/mock/*`). No backend needed. Great for UI review & demos.
- **`useMocks: false`** → real `HttpClient` calls hit `http://localhost:8080`.

**When the backend is running**, set `useMocks: false` (or use the `environment.ts` real config)
to exercise the true API.

### What mock mode covers
- [x] Schedule search (`GET /api/schedules`)
- [x] Seat availability (`GET /api/schedules/{id}/seats`) — *added Sprint 2*
- [x] Auth register/login/logout — *added Sprint 2*
- [x] Bookings create/mine/cancel — *added Sprint 3*
- [x] Trains & schedules admin CRUD — *added Sprint 3*

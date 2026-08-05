# RailEasy — Frontend (Angular + Angular Material)

Angular single-page app for the RailEasy train ticket booking system.

- **Framework:** Angular 20 (standalone components + signals)
- **UI:** Angular Material (M3, azure/blue theme)
- **HTTP:** `HttpClient` with interceptor support (JWT from Sprint 2)

> **Note on version:** The plan targets Angular 21, but Angular 21's CLI requires Node.js ≥ 22.
> This machine runs Node 20, so the project is scaffolded with **Angular 20**, which shares the
> same standalone/signals architecture. Upgrade to 21 with `ng update` after installing Node 22+.

## Prerequisites

- Node.js 20.19+ (Node 22+ recommended for future Angular 21 upgrade)
- npm 10+

## Run

```bash
cd frontend/raileasy-ui
npm install
npm start            # ng serve → http://localhost:4200
```

The backend must be running on `http://localhost:8080` for the health check and
subsequent API calls. See `../backend/readme.md`.

## Build & Test

```bash
npm run build        # production build → dist/
npm test             # Karma + Jasmine unit tests
```

## Structure

```
src/app/
├── app.ts / app.html / app.scss   Root shell (Material toolbar + router outlet)
├── app.config.ts                  Providers (router, HttpClient, animations)
├── app.routes.ts                  Route table
├── core/                          Singletons: services, interceptors, guards, models
│   ├── models/api-models.ts
│   └── services/ (health, schedule)
├── shared/                        Reusable UI (added as needed)
└── features/
    ├── search/                    Landing page: search form + results (Sprint 1)
    ├── auth/      (Sprint 2)
    ├── booking/   (Sprint 3)
    └── admin/     (Sprint 3)
```

## Configuration

- `src/environments/environment.ts`:
  - `apiBaseUrl: http://localhost:8080/api`
  - `useMocks: true` → an in-memory interceptor (`core/mock/`) serves sample data so the whole
    app (search → seat grid → booking → my tickets → admin) works **without a backend**.
    Set `useMocks: false` to call the real API. See `../../BACKEND_STATUS.md`.

### Demo login (works in mock mode)

- **Admin:** `admin@raileasy.com` / `Admin@123` (sees the Admin console)
- Or register a passenger from the Register page.

## Routes

| Path | Guard | Description |
|------|-------|-------------|
| `/` | — | Search trains |
| `/login`, `/register` | — | Auth |
| `/book/:scheduleId` | auth | 8×8 seat grid, select 1–4 seats |
| `/booking-confirmation` | auth | PNR display after booking |
| `/my-tickets` | auth | Bookings table + cancel |
| `/admin` | admin | Trains + Schedules management (Material dialogs) |

## Sprint progress

- [x] **Sprint 0** — Scaffolding: Angular + Material, app shell, home landing page wired to backend `/api/health`.
- [x] **Sprint 1** — Search feature (From/To/Date reactive form + results cards with per-class fare & seats), API models, `ScheduleService` over `HttpClient`.
- [x] **Sprint 2** — Auth (login/register), signal-based `AuthService`, JWT interceptor, auth/admin guards, class selection + 8×8 seat grid; mock mode.
- [x] **Sprint 3** — Booking confirmation (PNR), My Tickets (+ cancel), Admin console (trains + schedules CRUD dialogs), conditional admin menu; mock coverage for bookings + admin CRUD.

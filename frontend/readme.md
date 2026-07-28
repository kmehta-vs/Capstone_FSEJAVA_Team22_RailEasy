# RailEasy Frontend - Angular

## Overview
Angular-based SPA for the RailEasy train ticket booking application. Provides train search, seat selection, booking management, and admin dashboard.

## Tech Stack
- **Angular 17+** (Standalone components, Signals optional)
- **TypeScript**
- **Angular Router** (lazy-loaded routes)
- **Angular Services** (state management, HTTP calls)
- **SCSS** for styling
- **RxJS** for async operations
- **Angular Forms** (Reactive Forms)

## Project Structure
```
frontend/
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.routes.ts
│   │   ├── app.config.ts
│   │   │
│   │   ├── core/
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts          (login, register, logout, token mgmt)
│   │   │   │   ├── train.service.ts         (CRUD trains - admin)
│   │   │   │   ├── schedule.service.ts      (search schedules, get seats)
│   │   │   │   └── booking.service.ts       (create booking, my tickets, cancel)
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts            (protect routes for logged-in users)
│   │   │   │   └── admin.guard.ts           (protect admin routes)
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts      (attach JWT to outgoing requests)
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       ├── train.model.ts
│   │   │       ├── schedule.model.ts
│   │   │       └── booking.model.ts
│   │   │
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   │   └── login.component.ts|html|scss
│   │   │   │   └── register/
│   │   │   │       └── register.component.ts|html|scss
│   │   │   │
│   │   │   ├── home/
│   │   │   │   └── home.component.ts|html|scss        (landing + search form)
│   │   │   │
│   │   │   ├── search/
│   │   │   │   └── search-results/
│   │   │   │       └── search-results.component.ts|html|scss
│   │   │   │
│   │   │   ├── booking/
│   │   │   │   ├── class-selection/
│   │   │   │   │   └── class-selection.component.ts|html|scss
│   │   │   │   ├── seat-map/
│   │   │   │   │   └── seat-map.component.ts|html|scss     (8x8 grid)
│   │   │   │   ├── booking-confirm/
│   │   │   │   │   └── booking-confirm.component.ts|html|scss
│   │   │   │   └── my-tickets/
│   │   │   │       └── my-tickets.component.ts|html|scss
│   │   │   │
│   │   │   └── admin/
│   │   │       ├── train-management/
│   │   │       │   └── train-management.component.ts|html|scss
│   │   │       └── schedule-management/
│   │   │           └── schedule-management.component.ts|html|scss
│   │   │
│   │   └── shared/
│   │       ├── components/
│   │       │   ├── navbar/
│   │       │   │   └── navbar.component.ts|html|scss
│   │       │   └── toast/
│   │       │       └── toast.component.ts|html|scss
│   │       └── pipes/
│   │           └── date-format.pipe.ts
│   │
│   ├── assets/
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── styles.scss
│   ├── index.html
│   └── main.ts
├── angular.json
├── package.json
├── tsconfig.json
└── readme.md
```

## Routes
| Path | Component | Guard | Description |
|------|-----------|-------|-------------|
| `/` | HomeComponent | - | Landing page with search form |
| `/search` | SearchResultsComponent | - | Train search results |
| `/booking/class/:scheduleId` | ClassSelectionComponent | - | Choose travel class |
| `/booking/seats/:scheduleId/:class` | SeatMapComponent | - | 8x8 seat grid |
| `/booking/confirm` | BookingConfirmComponent | AuthGuard | Review & confirm booking |
| `/my-tickets` | MyTicketsComponent | AuthGuard | User's bookings list |
| `/login` | LoginComponent | - | Login form |
| `/register` | RegisterComponent | - | Registration form |
| `/admin/trains` | TrainManagementComponent | AdminGuard | CRUD trains |
| `/admin/schedules` | ScheduleManagementComponent | AdminGuard | CRUD schedules |

## Core Services

### AuthService
- `register(email, password, name)` - POST `/api/auth/register`
- `login(email, password)` - POST `/api/auth/login`, stores JWT in localStorage
- `logout()` - Clears token from localStorage
- `isLoggedIn()` - Checks if valid token exists
- `isAdmin()` - Decodes token to check isAdmin flag
- `getCurrentUser()` - Returns decoded user info from token

### ScheduleService
- `searchSchedules(from, to, date)` - GET `/api/schedules?from=&to=&date=`
- `getBookedSeats(scheduleId, travelClass)` - GET `/api/schedules/{id}/seats?class=`
- `createSchedule(schedule)` - POST `/api/schedules` (admin)
- `updateSchedule(id, schedule)` - PUT `/api/schedules/{id}` (admin)
- `deleteSchedule(id)` - DELETE `/api/schedules/{id}` (admin)

### BookingService
- `createBooking(scheduleId, travelClass, seatNumbers[])` - POST `/api/bookings`
- `getMyTickets()` - GET `/api/bookings/mine`
- `cancelBooking(id)` - PUT `/api/bookings/{id}/cancel`

### TrainService
- `getAllTrains()` - GET `/api/trains`
- `createTrain(train)` - POST `/api/trains` (admin)
- `updateTrain(id, train)` - PUT `/api/trains/{id}` (admin)
- `deleteTrain(id)` - DELETE `/api/trains/{id}` (admin)

## Key UI Components

### Home / Search Form
- From Station input
- To Station input
- Journey Date picker
- "Search Trains" button
- Navigates to `/search` with query params

### Search Results
- Card for each train: train name, number, departure/arrival times
- Class buttons (SLEEPER | AC_3 | AC_2) showing fare and seats left
- Clicking a class navigates to seat map

### Seat Map (8x8 Grid)
- 64 seats per class, displayed as 8 rows x 8 columns
- Seat labels: 1A, 1B, 1C...8H
- Colors: Green = Available, Red = Booked, Blue = Selected
- Select 1-4 seats, shows total fare
- "Confirm Booking" button (redirects to login if not authenticated)

### My Tickets
- Table: PNR | Train Name | Journey Date | Class | Seats | Status
- Cancel button for CONFIRMED tickets (upcoming journeys)

### Admin Dashboard
- **Trains**: Table of all trains + Add/Edit form (trainNumber, trainName, seatsPerClass)
- **Schedules**: Table of all schedules + Add/Edit form (train dropdown, stations, times, fares)

## Setup & Run

### Prerequisites
- Node.js 18+
- Angular CLI (`npm install -g @angular/cli`)

### Install & Run
```bash
cd frontend
npm install
ng serve
```
App runs at `http://localhost:4200`

### Build for Production
```bash
ng build --configuration production
```

### Environment Config
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## Implementation Steps (Sprint-wise)

### Sprint 1 - Foundations & Train Search
1. Initialize Angular project: `ng new raileasy-ui --routing --style=scss --standalone`
2. Set up folder structure (core, features, shared)
3. Configure environment files with API base URL
4. Create TypeScript models/interfaces (User, Train, Schedule, Booking)
5. Build `NavbarComponent` (logo, nav links, login/register buttons)
6. Build `HomeComponent` with search form (From Station, To Station, Date)
7. Create `ScheduleService` with `searchSchedules()` method
8. Build `SearchResultsComponent` displaying train cards with class options
9. Set up Angular Router with initial routes
10. Style components with SCSS (responsive, clean layout)

### Sprint 2 - Auth & Seat Selection
1. Create `AuthService` (register, login, logout, token management)
2. Build `LoginComponent` with reactive form + validation
3. Build `RegisterComponent` with reactive form + validation
4. Create `AuthInterceptor` to attach JWT to HTTP requests
5. Create `AuthGuard` and `AdminGuard` for route protection
6. Build `ClassSelectionComponent` showing fare and availability per class
7. Build `SeatMapComponent` with 8x8 grid (available/booked/selected states)
8. Implement seat selection logic (max 4 seats, fare calculation)
9. Add `ToastComponent` for success/error notifications
10. Handle error responses from API with user-friendly messages

### Sprint 3 - Booking & Admin
1. Build `BookingConfirmComponent` (review selected seats, confirm, show PNR)
2. Build `MyTicketsComponent` (table of bookings, cancel button)
3. Implement `BookingService` (create, list, cancel)
4. Build `TrainManagementComponent` (CRUD table + form, admin only)
5. Build `ScheduleManagementComponent` (CRUD table + form, train dropdown, admin only)
6. Add admin nav menu (shown only when `isAdmin` is true)
7. Polish UI: loading spinners, empty states, responsive design
8. Optional: unit tests for 1-2 components (e.g., SeatMapComponent)
9. Final styling pass and cross-browser check
10. Update README with final setup instructions

## Design Decisions
- **Standalone components**: Angular 17+ standalone API, no NgModules
- **Reactive Forms**: Used for all forms (login, register, search, admin CRUD) for validation control
- **JWT in localStorage**: Simple approach; token attached via HTTP interceptor
- **State in services**: Selected class and seats held in service state, passed to booking confirmation
- **SCSS**: Component-scoped styles with a global `styles.scss` for resets and variables
- **Lazy loading**: Admin module loaded only when admin routes are accessed

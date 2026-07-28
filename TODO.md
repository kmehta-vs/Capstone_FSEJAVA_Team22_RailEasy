# RailEasy - Implementation Tracker

> **Tech Stack**: Angular (Frontend) | Spring Boot (Backend) | H2 In-Memory DB
> **Delivery Plan**: 3 Sprints (1-2 weeks each)

---

## Sprint 1 - Foundations & Train Search

### Backend
- [x] Initialize Spring Boot project with Maven (dependencies: spring-web, spring-data-jpa, h2, validation, lombok)
- [x] Configure `application.properties` and `application-dev.properties` for H2 database
- [x] Create enum: `TravelClass` (SLEEPER, AC_3, AC_2)
- [x] Create enum: `BookingStatus` (CONFIRMED, CANCELLED)
- [x] Create entity: `User` (id, email, passwordHash, name, isAdmin, createdAt)
- [x] Create entity: `Train` (id, trainNumber, trainName, seatsPerClass, createdAt)
- [x] Create entity: `Schedule` (id, train, fromStation, toStation, departureTime, arrivalTime, journeyDate, fareSleeper, fareAc3, fareAc2, createdAt)
- [x] Create entity: `Booking` (id, user, schedule, travelClass, seatNumbers, pnrNumber, status, totalFare, createdAt)
- [x] Create repositories: UserRepository, TrainRepository, ScheduleRepository, BookingRepository
- [x] Implement `ScheduleService` with search logic (by fromStation, toStation, journeyDate)
- [x] Implement `ScheduleController` - GET `/api/schedules?from=&to=&date=`
- [x] Create DTOs: ScheduleSearchResult, ClassAvailability, ErrorResponse
- [x] Create `data.sql` seed file (2 users, 3 trains, 4 schedules)
- [x] Configure CORS to allow requests from `http://localhost:4200`
- [x] Test search endpoint via Postman/curl

### Frontend
- [x] Initialize Angular project: `ng new raileasy-ui --routing --style=scss --standalone`
- [x] Set up project folder structure (core/services, core/models, core/guards, features/, shared/)
- [x] Configure environment files (`environment.ts` with `apiUrl: http://localhost:8080/api`)
- [x] Create TypeScript interfaces: User, Train, Schedule, Booking, SearchResult
- [x] Create `ScheduleService` with `searchSchedules(from, to, date)` method
- [x] Build `NavbarComponent` (app logo, nav links placeholder)
- [x] Build `HomeComponent` with search form (From Station, To Station, Date picker, Search button)
- [x] Build `SearchResultsComponent` (train cards with class buttons showing fare + seats available)
- [x] Set up routing: `/` -> Home, `/search` -> SearchResults
- [x] Basic SCSS styling (global variables, responsive layout)

### Milestone Checkpoint
- [x] User can search trains by station and date from UI
- [x] Backend returns matching schedules with class availability
- [x] Seed data loads correctly on app startup

---

## Sprint 2 - Auth & Seat Selection

### Backend
- [ ] Add JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson)
- [ ] Implement `JwtUtil` (generateToken, validateToken, extractEmail, extractIsAdmin)
- [ ] Implement `JwtFilter` (extract Bearer token, set SecurityContext)
- [ ] Configure `SecurityConfig` (permit `/api/auth/**` and GET `/api/schedules/**`; protect POST/PUT/DELETE)
- [ ] Implement `AuthService` (register with BCrypt, login with credential validation + token generation)
- [ ] Implement `AuthController` (POST `/api/auth/register`, POST `/api/auth/login`)
- [ ] Create DTOs: RegisterRequest, LoginRequest, LoginResponse
- [ ] Implement `GET /api/schedules/{id}/seats?class=` to return booked seat numbers
- [ ] Add `@Valid` annotations and validation constraints on DTOs
- [ ] Implement `GlobalExceptionHandler` with structured error JSON format
- [ ] Create custom exceptions: ResourceNotFoundException, SeatAlreadyBookedException
- [ ] Write unit tests: AuthServiceTest, ScheduleServiceTest (JUnit5 + Mockito)

### Frontend
- [ ] Create `AuthService` (register, login, logout, token storage, user state)
- [ ] Build `LoginComponent` with reactive form (email, password) + validation messages
- [ ] Build `RegisterComponent` with reactive form (name, email, password, confirm password)
- [ ] Create `AuthInterceptor` to attach `Authorization: Bearer <token>` to HTTP requests
- [ ] Create `AuthGuard` (redirect to `/login` if not authenticated)
- [ ] Create `AdminGuard` (redirect if not admin)
- [ ] Build `ClassSelectionComponent` (show 3 class cards with fare, seats available)
- [ ] Build `SeatMapComponent` - 8x8 seat grid:
  - [ ] Green = Available, Red = Booked, Blue = Selected
  - [ ] Click to select/deselect (max 4 seats)
  - [ ] Show selected seats list + total fare
  - [ ] "Confirm Booking" button
- [ ] Build `ToastComponent` for success/error notifications
- [ ] Update `NavbarComponent` (show Login/Register or username + Logout based on auth state)
- [ ] Add routes: `/login`, `/register`, `/booking/class/:scheduleId`, `/booking/seats/:scheduleId/:class`

### Milestone Checkpoint
- [ ] User can register and login
- [ ] JWT token is sent with authenticated requests
- [ ] User can select a class and see the 8x8 seat grid
- [ ] Booked seats are displayed as unavailable
- [ ] User can select 1-4 available seats

---

## Sprint 3 - Booking & Admin

### Backend
- [ ] Implement `BookingService`:
  - [ ] `createBooking()` - validate seats not already booked, generate PNR (UUID first 8 chars), calculate fare
  - [ ] `getMyBookings(userId)` - return user's bookings
  - [ ] `cancelBooking(bookingId, userId)` - set status to CANCELLED (frees seats)
- [ ] Implement `BookingController`:
  - [ ] POST `/api/bookings` - create booking, return PNR
  - [ ] GET `/api/bookings/mine` - list user's tickets
  - [ ] PUT `/api/bookings/{id}/cancel` - cancel ticket
- [ ] Implement `TrainService` (CRUD operations)
- [ ] Implement `TrainController` (GET, POST, PUT, DELETE `/api/trains`) - admin only
- [ ] Add schedule CRUD to `ScheduleController` (POST, PUT, DELETE) - admin only
- [ ] Add `isAdmin` authorization check (in SecurityConfig or service layer)
- [ ] Write unit tests: BookingServiceTest, TrainServiceTest
- [ ] Verify test coverage >= 60%
- [ ] Set up Swagger/OpenAPI documentation (`springdoc-openapi-starter-webmvc-ui`)
- [ ] Create Postman collection for all API endpoints

### Frontend
- [ ] Build `BookingConfirmComponent`:
  - [ ] Show booking summary (train, date, class, seats, total fare)
  - [ ] Confirm button -> POST to API -> show PNR on success
  - [ ] Redirect to login if not authenticated
- [ ] Build `MyTicketsComponent`:
  - [ ] Table: PNR, Train Name, Journey Date, Class, Seats, Status
  - [ ] Cancel button for CONFIRMED tickets
  - [ ] Confirmation dialog before cancelling
- [ ] Implement `BookingService` (createBooking, getMyTickets, cancelBooking)
- [ ] Implement `TrainService` (getAllTrains, createTrain, updateTrain, deleteTrain)
- [ ] Build `TrainManagementComponent` (admin):
  - [ ] Table of all trains
  - [ ] Add/Edit form (trainNumber, trainName, seatsPerClass)
  - [ ] Delete with confirmation
- [ ] Build `ScheduleManagementComponent` (admin):
  - [ ] Table of all schedules
  - [ ] Add/Edit form (train dropdown, from/to station, times, fares)
  - [ ] Delete with confirmation
- [ ] Update `NavbarComponent` to show "Admin" menu when `isAdmin` is true
- [ ] Add routes: `/booking/confirm`, `/my-tickets`, `/admin/trains`, `/admin/schedules`
- [ ] Loading spinners and empty-state messages
- [ ] Final UI polish (responsive, consistent styling)

### Milestone Checkpoint
- [ ] User can complete full booking flow: search -> select class -> pick seats -> confirm -> see PNR
- [ ] User can view their tickets and cancel bookings
- [ ] Admin can manage trains and schedules via UI
- [ ] Backend tests pass with >= 60% coverage
- [ ] API docs available via Swagger UI

---

## Final Checklist (Before Submission)

### Documentation
- [ ] Backend README updated with setup steps and run commands
- [ ] Frontend README updated with setup steps and run commands
- [ ] API endpoints documented (Swagger + Postman collection)
- [ ] Seed data documented
- [ ] Design decisions and trade-offs documented
- [ ] AI tool usage documented (cite prompts in README)

### Code Quality
- [ ] No critical lint errors (ESLint for Angular)
- [ ] SOLID principles followed (service layer, DTOs, separation of concerns)
- [ ] Input validation on both frontend and backend
- [ ] Consistent error handling (structured error JSON, user-friendly toasts)
- [ ] Passwords hashed with BCrypt
- [ ] JWT stored and transmitted securely

### Testing
- [ ] Backend unit tests >= 60% coverage (JUnit5 + Mockito)
- [ ] Manual API testing completed (Postman)
- [ ] Manual UI testing completed (all user flows)
- [ ] Tested with 5-10 concurrent requests (Postman Runner or browser tabs)

### Git
- [ ] main branch is stable
- [ ] feature/* branches used for development
- [ ] Meaningful commit messages
- [ ] PR reviews completed

---

## Stretch Goals (Optional)
- [ ] Waitlist status when all seats in a class are booked
- [ ] Station autocomplete dropdown (search-as-you-type)
- [ ] Downloadable ticket summary page (PDF or print-friendly)

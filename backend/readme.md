# RailEasy Backend - Spring Boot

## Overview
REST API backend for the RailEasy train ticket booking application. Built with Spring Boot, Spring Data JPA, and H2 in-memory database.

## Tech Stack
- **Java 17+**
- **Spring Boot 3.x** (Web, Data JPA, Validation, Security)
- **H2 Database** (in-memory, dev profile)
- **BCrypt** for password hashing
- **JWT** for stateless authentication
- **JUnit 5 + Mockito** for testing
- **Swagger/OpenAPI** for API documentation
- **Maven** as build tool

## Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/raileasy/
│   │   │   ├── RailEasyApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtFilter.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── TrainController.java
│   │   │   │   ├── ScheduleController.java
│   │   │   │   └── BookingController.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── TrainDTO.java
│   │   │   │   ├── ScheduleDTO.java
│   │   │   │   ├── ScheduleSearchResult.java
│   │   │   │   ├── BookingRequest.java
│   │   │   │   ├── BookingResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Train.java
│   │   │   │   ├── Schedule.java
│   │   │   │   └── Booking.java
│   │   │   ├── enums/
│   │   │   │   ├── TravelClass.java       (SLEEPER, AC_3, AC_2)
│   │   │   │   └── BookingStatus.java     (CONFIRMED, CANCELLED)
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TrainRepository.java
│   │   │   │   ├── ScheduleRepository.java
│   │   │   │   └── BookingRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── TrainService.java
│   │   │   │   ├── ScheduleService.java
│   │   │   │   └── BookingService.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── SeatAlreadyBookedException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── data.sql                   (seed data)
│   └── test/
│       └── java/com/raileasy/
│           ├── service/
│           │   ├── AuthServiceTest.java
│           │   ├── TrainServiceTest.java
│           │   ├── ScheduleServiceTest.java
│           │   └── BookingServiceTest.java
│           └── controller/
│               ├── AuthControllerTest.java
│               ├── TrainControllerTest.java
│               └── BookingControllerTest.java
├── pom.xml
└── readme.md
```

## Data Model

### User
| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| email | String | Unique, not null |
| passwordHash | String | BCrypt hashed |
| name | String | Not null |
| isAdmin | boolean | Defaults to false |
| createdAt | LocalDateTime | Auto-set |

### Train
| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| trainNumber | String | Unique, not null |
| trainName | String | Not null |
| seatsPerClass | int | Default 64 (8x8 grid) |
| createdAt | LocalDateTime | Auto-set |

### Schedule
| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| train | Train | ManyToOne FK |
| fromStation | String | Not null |
| toStation | String | Not null |
| departureTime | LocalDateTime | Not null |
| arrivalTime | LocalDateTime | Not null |
| journeyDate | LocalDate | Not null |
| fareSleeper | BigDecimal | Not null |
| fareAc3 | BigDecimal | Not null |
| fareAc2 | BigDecimal | Not null |
| createdAt | LocalDateTime | Auto-set |

### Booking
| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| user | User | ManyToOne FK |
| schedule | Schedule | ManyToOne FK |
| travelClass | TravelClass | ENUM (SLEEPER, AC_3, AC_2) |
| seatNumbers | String | CSV e.g. "1A,1B,2A" |
| pnrNumber | String | UUID first 8 chars, unique |
| status | BookingStatus | ENUM (CONFIRMED, CANCELLED) |
| totalFare | BigDecimal | Calculated |
| createdAt | LocalDateTime | Auto-set |

## API Endpoints

### Auth
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login, returns JWT | No |
| POST | `/api/auth/logout` | Logout (client-side token removal) | Yes |

### Schedules (Search)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/schedules?from=&to=&date=` | Search schedules with availability | No |
| GET | `/api/schedules/{id}/seats?class=` | Get booked seat numbers for a class | No |
| POST | `/api/schedules` | Create schedule | Admin |
| PUT | `/api/schedules/{id}` | Update schedule | Admin |
| DELETE | `/api/schedules/{id}` | Delete schedule | Admin |

### Trains (Admin)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/trains` | List all trains | Admin |
| POST | `/api/trains` | Create train | Admin |
| PUT | `/api/trains/{id}` | Update train | Admin |
| DELETE | `/api/trains/{id}` | Delete train | Admin |

### Bookings
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/bookings` | Create booking (returns PNR) | User |
| GET | `/api/bookings/mine` | Get current user's bookings | User |
| PUT | `/api/bookings/{id}/cancel` | Cancel a booking | User |

### Error Response Format
```json
{
  "timestamp": "2025-10-12T12:00:00Z",
  "path": "/api/...",
  "error": "VALIDATION_ERROR",
  "message": "Field is required"
}
```

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Run (H2 Dev Profile)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:raileasydb`
- Username: `sa`
- Password: *(empty)*

### Seed Data
The `data.sql` file auto-loads on startup with:
- 1 admin user (`admin@raileasy.com` / `admin123`)
- 2 trains (Chennai Express, Rajdhani Express)
- 2 schedules (Chennai Central -> Mumbai CSMT)

### Run Tests
```bash
mvn test
```

### Build
```bash
mvn clean package
```

## Implementation Steps (Sprint-wise)

### Sprint 1 - Foundations & Train Search
1. Initialize Spring Boot project with Maven (spring-web, spring-data-jpa, h2, validation, lombok)
2. Configure `application-dev.properties` for H2
3. Create entity classes: `User`, `Train`, `Schedule`, `Booking`
4. Create enums: `TravelClass`, `BookingStatus`
5. Create repositories for all entities
6. Implement `TrainService` and `ScheduleService`
7. Implement `ScheduleController` with search endpoint (`GET /api/schedules?from=&to=&date=`)
8. Create `data.sql` with seed data (admin user, trains, schedules)
9. Configure CORS for Angular dev server (port 4200)
10. Test search endpoint via Postman

### Sprint 2 - Auth & Seat Selection
1. Add Spring Security + JWT dependencies
2. Implement `JwtUtil` (token generation, validation)
3. Implement `JwtFilter` (extract token from header, set auth context)
4. Configure `SecurityConfig` (permit auth endpoints, protect booking/admin routes)
5. Implement `AuthService` (register with BCrypt, login with token response)
6. Implement `AuthController` (register, login, logout endpoints)
7. Implement `GET /api/schedules/{id}/seats?class=` to return booked seats
8. Add input validation (`@Valid`, `@NotBlank`, `@Email`, etc.)
9. Implement `GlobalExceptionHandler` with structured error responses
10. Write JUnit tests for AuthService and ScheduleService

### Sprint 3 - Booking & Admin
1. Implement `BookingService` (create booking with PNR, validate seats not taken, cancel)
2. Implement `BookingController` (POST create, GET mine, PUT cancel)
3. Implement `TrainController` with full CRUD (admin-only)
4. Implement schedule CRUD endpoints in `ScheduleController` (admin-only)
5. Add `isAdmin` check in security filter or service layer
6. Write JUnit tests for BookingService, TrainService
7. Achieve >= 60% test coverage
8. Set up Swagger/OpenAPI documentation
9. Create Postman collection for all endpoints
10. Final cleanup and README update

## Key Design Decisions
- **H2 in-memory DB**: No external DB setup needed; data resets on restart; seed data auto-loaded via `data.sql`
- **JWT Auth**: Stateless; token sent in `Authorization: Bearer <token>` header
- **Seat storage**: Booked seats stored as CSV string per booking; availability derived by querying all CONFIRMED bookings for a schedule+class
- **PNR**: First 8 characters of a UUID
- **Admin role**: Simple `isAdmin` boolean flag on User entity; checked server-side before CRUD operations

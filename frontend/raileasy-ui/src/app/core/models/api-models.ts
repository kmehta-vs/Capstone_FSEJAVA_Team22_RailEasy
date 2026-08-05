export type TravelClass = 'SLEEPER' | 'AC_3' | 'AC_2';

export interface ClassAvailability {
  travelClass: TravelClass;
  fare: number;
  seatsAvailable: number;
}

export interface ScheduleSearchResult {
  scheduleId: string;
  trainName: string;
  trainNumber: string;
  fromStation: string;
  toStation: string;
  departureTime: string; // ISO LocalDateTime
  arrivalTime: string;
  journeyDate: string; // ISO LocalDate
  classes: ClassAvailability[];
}

export interface SeatAvailability {
  scheduleId: string;
  travelClass: TravelClass;
  seatsPerClass: number;
  bookedSeats: string[];
}

export interface Train {
  id: string;
  trainNumber: string;
  trainName: string;
  seatsPerClass: number;
}

/** Human-friendly labels for travel classes. */
export const TRAVEL_CLASS_LABELS: Record<TravelClass, string> = {
  SLEEPER: 'Sleeper',
  AC_3: 'AC 3-Tier',
  AC_2: 'AC 2-Tier',
};

// --- Auth ---

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  isAdmin: boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}

// --- Bookings ---

export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface CreateBookingRequest {
  scheduleId: string;
  travelClass: TravelClass;
  seatNumbers: string[];
}

export interface Booking {
  id: string;
  pnrNumber: string;
  scheduleId: string;
  trainName: string;
  trainNumber: string;
  fromStation: string;
  toStation: string;
  journeyDate: string;
  travelClass: TravelClass;
  seatNumbers: string[];
  farePerSeat: number;
  totalFare: number;
  status: BookingStatus;
  createdAt: string;
}

// --- Admin CRUD ---

export interface TrainRequest {
  trainNumber: string;
  trainName: string;
  seatsPerClass: number;
}

export interface AdminSchedule {
  id: string;
  trainId: string;
  trainName: string;
  trainNumber: string;
  fromStation: string;
  toStation: string;
  departureTime: string;
  arrivalTime: string;
  journeyDate: string;
  fareSleeper: number;
  fareAc3: number;
  fareAc2: number;
}

export interface ScheduleRequest {
  trainId: string;
  fromStation: string;
  toStation: string;
  departureTime: string;
  arrivalTime: string;
  journeyDate: string;
  fareSleeper: number;
  fareAc3: number;
  fareAc2: number;
}

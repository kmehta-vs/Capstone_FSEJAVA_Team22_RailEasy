import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import {
  MOCK_ADMIN_SCHEDULES,
  MOCK_BOOKED_SEATS,
  MOCK_SCHEDULES,
  MOCK_SEATS_PER_CLASS,
  MOCK_TRAINS,
  MockAdminSchedule,
  MockTrain,
} from './mock-data';
import { Booking, TravelClass } from '../models/api-models';

/**
 * Development-only interceptor. When `environment.useMocks` is true, it answers
 * `/api/*` requests with local sample data so the UI works without a backend.
 *
 * Set `environment.useMocks = false` to hit the real backend on :8080.
 */

interface MockUser {
  id: string;
  email: string;
  name: string;
  password: string;
  isAdmin: boolean;
}

// Seeded in-memory users (mirrors backend seed: one admin). Passwords are plain in mock only.
const mockUsers: MockUser[] = [
  {
    id: 'admin-0001',
    email: 'admin@raileasy.com',
    name: 'Admin',
    password: 'Admin@123',
    isAdmin: true,
  },
];

// In-memory mutable stores for mock mode.
const mockBookings: Booking[] = [];
const mockTrains: MockTrain[] = [...MOCK_TRAINS];
const mockAdminSchedules: MockAdminSchedule[] = [...MOCK_ADMIN_SCHEDULES];

function fakeToken(email: string): string {
  // Not a real JWT — just a base64 stub so the app can store/echo something in mock mode.
  return 'mock.' + btoa(email) + '.token';
}

function uuid(): string {
  return crypto.randomUUID();
}

function fareFor(scheduleId: string, travelClass: TravelClass): number {
  const s = mockAdminSchedules.find((x) => x.id === scheduleId);
  if (!s) {
    return 0;
  }
  return travelClass === 'SLEEPER' ? s.fareSleeper : travelClass === 'AC_3' ? s.fareAc3 : s.fareAc2;
}

function ok<T>(body: T, status = 200) {
  return of(new HttpResponse<T>({ status, body })).pipe(delay(300));
}

function fail(status: number, error: string, message: string, path: string) {
  return throwError(() => ({
    status,
    error: {
      timestamp: new Date().toISOString(),
      path,
      error,
      message,
    },
  })).pipe(delay(300));
}

export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.useMocks || !req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  const path = req.url.substring(environment.apiBaseUrl.length).split('?')[0];
  const method = req.method.toUpperCase();

  // --- Health ---
  if (method === 'GET' && path === '/health') {
    return ok({ status: 'UP', service: 'raileasy-mock', timestamp: new Date().toISOString() });
  }

  // --- Auth ---
  if (method === 'POST' && path === '/auth/register') {
    const { email, password, name } = (req.body ?? {}) as {
      email: string;
      password: string;
      name: string;
    };
    if (mockUsers.some((u) => u.email.toLowerCase() === email?.toLowerCase())) {
      return fail(409, 'CONFLICT', 'Email already registered', path);
    }
    const user: MockUser = {
      id: 'user-' + (mockUsers.length + 1),
      email,
      name,
      password,
      isAdmin: false,
    };
    mockUsers.push(user);
    return ok({ id: user.id, email: user.email, name: user.name, isAdmin: user.isAdmin }, 201);
  }

  if (method === 'POST' && path === '/auth/login') {
    const { email, password } = (req.body ?? {}) as { email: string; password: string };
    const user = mockUsers.find(
      (u) => u.email.toLowerCase() === email?.toLowerCase() && u.password === password,
    );
    if (!user) {
      return fail(401, 'UNAUTHORIZED', 'Invalid email or password', path);
    }
    return ok({
      token: fakeToken(user.email),
      user: { id: user.id, email: user.email, name: user.name, isAdmin: user.isAdmin },
    });
  }

  if (method === 'POST' && path === '/auth/logout') {
    return ok(null, 204);
  }

  // --- Bookings ---
  if (method === 'POST' && path === '/bookings') {
    const body = (req.body ?? {}) as {
      scheduleId: string;
      travelClass: TravelClass;
      seatNumbers: string[];
    };
    const key = `${body.scheduleId}|${body.travelClass}`;
    const already = MOCK_BOOKED_SEATS[key] ?? [];
    const clash = body.seatNumbers.filter((s) => already.includes(s));
    if (clash.length > 0) {
      return fail(409, 'CONFLICT', `Seat(s) already booked: ${clash.join(', ')}`, path);
    }
    MOCK_BOOKED_SEATS[key] = [...already, ...body.seatNumbers];

    const s = mockAdminSchedules.find((x) => x.id === body.scheduleId);
    const farePerSeat = fareFor(body.scheduleId, body.travelClass);
    const booking: Booking = {
      id: uuid(),
      pnrNumber: uuid().substring(0, 8).toUpperCase(),
      scheduleId: body.scheduleId,
      trainName: s?.trainName ?? 'Train',
      trainNumber: s?.trainNumber ?? '',
      fromStation: s?.fromStation ?? '',
      toStation: s?.toStation ?? '',
      journeyDate: s?.journeyDate ?? '',
      travelClass: body.travelClass,
      seatNumbers: body.seatNumbers,
      farePerSeat,
      totalFare: farePerSeat * body.seatNumbers.length,
      status: 'CONFIRMED',
      createdAt: new Date().toISOString(),
    };
    mockBookings.unshift(booking);
    return ok(booking, 201);
  }

  if (method === 'GET' && path === '/bookings/mine') {
    return ok(mockBookings);
  }

  const cancelMatch = path.match(/^\/bookings\/([^/]+)\/cancel$/);
  if (method === 'PUT' && cancelMatch) {
    const id = cancelMatch[1];
    const booking = mockBookings.find((b) => b.id === id);
    if (!booking) {
      return fail(404, 'NOT_FOUND', 'Booking not found', path);
    }
    if (booking.status === 'CANCELLED') {
      return fail(409, 'CONFLICT', 'Booking is already cancelled', path);
    }
    booking.status = 'CANCELLED';
    const key = `${booking.scheduleId}|${booking.travelClass}`;
    MOCK_BOOKED_SEATS[key] = (MOCK_BOOKED_SEATS[key] ?? []).filter(
      (s) => !booking.seatNumbers.includes(s),
    );
    return ok(booking);
  }

  // --- Trains (list public; mutations admin) ---
  if (method === 'GET' && path === '/trains') {
    return ok(mockTrains);
  }
  if (method === 'POST' && path === '/trains') {
    const body = req.body as { trainNumber: string; trainName: string; seatsPerClass: number };
    if (mockTrains.some((t) => t.trainNumber === body.trainNumber)) {
      return fail(409, 'CONFLICT', `Train number already exists: ${body.trainNumber}`, path);
    }
    const train: MockTrain = { id: uuid(), ...body };
    mockTrains.push(train);
    return ok(train, 201);
  }
  const trainIdMatch = path.match(/^\/trains\/([^/]+)$/);
  if (method === 'PUT' && trainIdMatch) {
    const train = mockTrains.find((t) => t.id === trainIdMatch[1]);
    if (!train) {
      return fail(404, 'NOT_FOUND', 'Train not found', path);
    }
    Object.assign(train, req.body);
    return ok(train);
  }
  if (method === 'DELETE' && trainIdMatch) {
    const idx = mockTrains.findIndex((t) => t.id === trainIdMatch[1]);
    if (idx === -1) {
      return fail(404, 'NOT_FOUND', 'Train not found', path);
    }
    mockTrains.splice(idx, 1);
    return ok(null, 204);
  }

  // --- Schedules admin list ---
  if (method === 'GET' && path === '/schedules/all') {
    return ok(mockAdminSchedules);
  }

  // --- Schedules search ---
  if (method === 'GET' && path === '/schedules') {
    // Mock ignores filters and returns the sample set (date/stations already match seed).
    return ok(MOCK_SCHEDULES);
  }

  if (method === 'POST' && path === '/schedules') {
    const body = req.body as Omit<MockAdminSchedule, 'id' | 'trainName' | 'trainNumber'>;
    const train = mockTrains.find((t) => t.id === body.trainId);
    const created: MockAdminSchedule = {
      id: uuid(),
      trainName: train?.trainName ?? 'Train',
      trainNumber: train?.trainNumber ?? '',
      ...body,
    };
    mockAdminSchedules.push(created);
    return ok(created, 201);
  }

  const scheduleIdMatch = path.match(/^\/schedules\/([^/]+)$/);
  if (method === 'PUT' && scheduleIdMatch) {
    const schedule = mockAdminSchedules.find((s) => s.id === scheduleIdMatch[1]);
    if (!schedule) {
      return fail(404, 'NOT_FOUND', 'Schedule not found', path);
    }
    const body = req.body as Partial<MockAdminSchedule>;
    const train = mockTrains.find((t) => t.id === body.trainId);
    Object.assign(schedule, body, {
      trainName: train?.trainName ?? schedule.trainName,
      trainNumber: train?.trainNumber ?? schedule.trainNumber,
    });
    return ok(schedule);
  }
  if (method === 'DELETE' && scheduleIdMatch) {
    const idx = mockAdminSchedules.findIndex((s) => s.id === scheduleIdMatch[1]);
    if (idx === -1) {
      return fail(404, 'NOT_FOUND', 'Schedule not found', path);
    }
    mockAdminSchedules.splice(idx, 1);
    return ok(null, 204);
  }

  // --- Seat availability: /schedules/{id}/seats?travelClass= ---
  const seatsMatch = path.match(/^\/schedules\/([^/]+)\/seats$/);
  if (method === 'GET' && seatsMatch) {
    const scheduleId = seatsMatch[1];
    const travelClass = req.params.get('travelClass') ?? 'SLEEPER';
    const booked = MOCK_BOOKED_SEATS[`${scheduleId}|${travelClass}`] ?? [];
    return ok({
      scheduleId,
      travelClass,
      seatsPerClass: MOCK_SEATS_PER_CLASS,
      bookedSeats: booked,
    });
  }

  // Unhandled mock path
  return fail(404, 'NOT_FOUND', `No mock handler for ${method} ${path}`, path);
};

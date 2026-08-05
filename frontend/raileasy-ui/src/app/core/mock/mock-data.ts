import { ScheduleSearchResult } from '../models/api-models';
/**
 * Sample search results mirroring the backend seed data
 * (Chennai Central → Mumbai CSMT on 2025-10-21). Used only in mock mode.
 */
export const MOCK_SCHEDULES: ScheduleSearchResult[] = [
  {
    scheduleId: '11111111-1111-1111-1111-111111111111',
    trainName: 'Chennai Express',
    trainNumber: '12163',
    fromStation: 'Chennai Central',
    toStation: 'Mumbai CSMT',
    departureTime: '2025-10-21T06:00:00',
    arrivalTime: '2025-10-22T05:30:00',
    journeyDate: '2025-10-21',
    classes: [
      { travelClass: 'SLEEPER', fare: 450.0, seatsAvailable: 62 },
      { travelClass: 'AC_3', fare: 1200.0, seatsAvailable: 64 },
      { travelClass: 'AC_2', fare: 1800.0, seatsAvailable: 60 },
    ],
  },
  {
    scheduleId: '22222222-2222-2222-2222-222222222222',
    trainName: 'Rajdhani Express',
    trainNumber: '22691',
    fromStation: 'Chennai Central',
    toStation: 'Mumbai CSMT',
    departureTime: '2025-10-21T08:00:00',
    arrivalTime: '2025-10-22T07:45:00',
    journeyDate: '2025-10-21',
    classes: [
      { travelClass: 'SLEEPER', fare: 500.0, seatsAvailable: 64 },
      { travelClass: 'AC_3', fare: 1350.0, seatsAvailable: 61 },
      { travelClass: 'AC_2', fare: 2000.0, seatsAvailable: 64 },
    ],
  },
];

/**
 * Pre-booked seats per `${scheduleId}|${travelClass}` so the seat grid shows some red seats.
 */
export const MOCK_BOOKED_SEATS: Record<string, string[]> = {
  '11111111-1111-1111-1111-111111111111|SLEEPER': ['1A', '1B'],
  '11111111-1111-1111-1111-111111111111|AC_2': ['3C', '3D', '5F', '8H'],
  '22222222-2222-2222-2222-222222222222|AC_3': ['2C', '2D', '4A'],
};

export const MOCK_SEATS_PER_CLASS = 64;

/** Trains for the admin console (mock mode). */
export interface MockTrain {
  id: string;
  trainNumber: string;
  trainName: string;
  seatsPerClass: number;
}

export const MOCK_TRAINS: MockTrain[] = [
  {
    id: 'aaaaaaaa-0000-0000-0000-000000000001',
    trainNumber: '12163',
    trainName: 'Chennai Express',
    seatsPerClass: 64,
  },
  {
    id: 'aaaaaaaa-0000-0000-0000-000000000002',
    trainNumber: '22691',
    trainName: 'Rajdhani Express',
    seatsPerClass: 64,
  },
];

/** Admin schedule rows (mock mode) — mirrors the search schedules with fares + trainId. */
export interface MockAdminSchedule {
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

export const MOCK_ADMIN_SCHEDULES: MockAdminSchedule[] = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    trainId: 'aaaaaaaa-0000-0000-0000-000000000001',
    trainName: 'Chennai Express',
    trainNumber: '12163',
    fromStation: 'Chennai Central',
    toStation: 'Mumbai CSMT',
    departureTime: '2025-10-21T06:00:00',
    arrivalTime: '2025-10-22T05:30:00',
    journeyDate: '2025-10-21',
    fareSleeper: 450,
    fareAc3: 1200,
    fareAc2: 1800,
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    trainId: 'aaaaaaaa-0000-0000-0000-000000000002',
    trainName: 'Rajdhani Express',
    trainNumber: '22691',
    fromStation: 'Chennai Central',
    toStation: 'Mumbai CSMT',
    departureTime: '2025-10-21T08:00:00',
    arrivalTime: '2025-10-22T07:45:00',
    journeyDate: '2025-10-21',
    fareSleeper: 500,
    fareAc3: 1350,
    fareAc2: 2000,
  },
];
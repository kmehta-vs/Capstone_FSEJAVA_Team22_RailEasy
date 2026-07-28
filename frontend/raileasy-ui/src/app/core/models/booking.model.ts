export interface Booking {
  id: string;
  scheduleId: string;
  travelClass: 'SLEEPER' | 'AC_3' | 'AC_2';
  seatNumbers: string;
  pnrNumber: string;
  status: 'CONFIRMED' | 'CANCELLED';
  totalFare: number;
  createdAt: string;
  trainName?: string;
  trainNumber?: string;
  journeyDate?: string;
  fromStation?: string;
  toStation?: string;
}

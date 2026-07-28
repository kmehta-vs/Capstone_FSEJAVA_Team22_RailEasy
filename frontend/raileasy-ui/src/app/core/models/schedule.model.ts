export interface ClassAvailability {
  travelClass: 'SLEEPER' | 'AC_3' | 'AC_2';
  fare: number;
  seatsAvailable: number;
}

export interface ScheduleSearchResult {
  scheduleId: string;
  trainName: string;
  trainNumber: string;
  fromStation: string;
  toStation: string;
  departureTime: string;
  arrivalTime: string;
  journeyDate: string;
  classAvailabilities: ClassAvailability[];
}

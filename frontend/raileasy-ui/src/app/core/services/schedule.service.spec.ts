import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ScheduleService } from './schedule.service';
import { environment } from '../../../environments/environment';
import { ScheduleSearchResult, SeatAvailability } from '../models/api-models';

describe('ScheduleService', () => {
  let service: ScheduleService;
  let httpMock: HttpTestingController;

  const mockResult: ScheduleSearchResult[] = [
    {
      scheduleId: 's1',
      trainName: 'Chennai Express',
      trainNumber: '12163',
      fromStation: 'Chennai Central',
      toStation: 'Mumbai CSMT',
      departureTime: '2025-10-21T06:00:00',
      arrivalTime: '2025-10-22T05:30:00',
      journeyDate: '2025-10-21',
      classes: [{ travelClass: 'SLEEPER', fare: 450, seatsAvailable: 64 }],
    },
  ];

  const mockSeats: SeatAvailability = {
    scheduleId: 's1',
    travelClass: 'SLEEPER',
    seatsPerClass: 64,
    bookedSeats: ['1A', '1B'],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ScheduleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search() should GET /schedules with from/to/date query params', () => {
    service.search('Chennai Central', 'Mumbai CSMT', '2025-10-21').subscribe((res) => {
      expect(res).toEqual(mockResult);
    });

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/schedules` &&
        r.params.get('from') === 'Chennai Central' &&
        r.params.get('to') === 'Mumbai CSMT' &&
        r.params.get('date') === '2025-10-21'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockResult);
  });

  it('seats() should GET /schedules/{id}/seats with travelClass param', () => {
    service.seats('s1', 'SLEEPER').subscribe((res) => {
      expect(res).toEqual(mockSeats);
    });

    const req = httpMock.expectOne(
      (r) => r.url === `${environment.apiBaseUrl}/schedules/s1/seats` && r.params.get('travelClass') === 'SLEEPER'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockSeats);
  });
});

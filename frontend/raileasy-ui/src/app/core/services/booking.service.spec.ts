import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { BookingService } from './booking.service';
import { environment } from '../../../environments/environment';
import { Booking, CreateBookingRequest } from '../models/api-models';

describe('BookingService', () => {
  let service: BookingService;
  let httpMock: HttpTestingController;

  const mockBooking: Booking = {
    id: 'b1',
    pnrNumber: 'ABCD1234',
    scheduleId: 's1',
  } as Booking;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('create() should POST the booking request', () => {
    const body: CreateBookingRequest = {
      scheduleId: 's1',
      travelClass: 'SLEEPER',
      seatNumbers: ['1A', '1B'],
    };

    service.create(body).subscribe((res) => expect(res).toEqual(mockBooking));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/bookings`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(mockBooking);
  });

  it('mine() should GET the caller bookings', () => {
    service.mine().subscribe((res) => expect(res).toEqual([mockBooking]));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/bookings/mine`);
    expect(req.request.method).toBe('GET');
    req.flush([mockBooking]);
  });

  it('cancel() should PUT to /bookings/{id}/cancel', () => {
    service.cancel('b1').subscribe((res) => expect(res).toEqual(mockBooking));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/bookings/b1/cancel`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockBooking);
  });
});

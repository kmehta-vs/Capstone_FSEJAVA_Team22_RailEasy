import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Booking, CreateBookingRequest } from '../models/api-models';

/**
 * Client for the booking endpoints (all require authentication).
 */
@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/bookings`;

  /** POST /api/bookings */
  create(body: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.baseUrl, body);
  }

  /** GET /api/bookings/mine */
  mine(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.baseUrl}/mine`);
  }

  /** PUT /api/bookings/{id}/cancel */
  cancel(id: string): Observable<Booking> {
    return this.http.put<Booking>(`${this.baseUrl}/${id}/cancel`, {});
  }
}

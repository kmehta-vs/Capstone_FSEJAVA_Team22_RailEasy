import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ScheduleSearchResult, SeatAvailability, TravelClass } from '../models/api-models';

/**
 * Client for the schedule search + seat-availability endpoints.
 */
@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/schedules`;

  /** GET /api/schedules?from=&to=&date= */
  search(from: string, to: string, date: string): Observable<ScheduleSearchResult[]> {
    const params = new HttpParams().set('from', from).set('to', to).set('date', date);
    return this.http.get<ScheduleSearchResult[]>(this.baseUrl, { params });
  }

  /** GET /api/schedules/{id}/seats?travelClass= */
  seats(scheduleId: string, travelClass: TravelClass): Observable<SeatAvailability> {
    const params = new HttpParams().set('travelClass', travelClass);
    return this.http.get<SeatAvailability>(`${this.baseUrl}/${scheduleId}/seats`, { params });
  }
}

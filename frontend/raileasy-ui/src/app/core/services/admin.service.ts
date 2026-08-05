import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AdminSchedule,
  ScheduleRequest,
  Train,
  TrainRequest,
} from '../models/api-models';

/**
 * Client for admin train + schedule management (all mutations require ROLE_ADMIN).
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // --- Trains ---
  listTrains(): Observable<Train[]> {
    return this.http.get<Train[]>(`${this.base}/trains`);
  }

  createTrain(body: TrainRequest): Observable<Train> {
    return this.http.post<Train>(`${this.base}/trains`, body);
  }

  updateTrain(id: string, body: TrainRequest): Observable<Train> {
    return this.http.put<Train>(`${this.base}/trains/${id}`, body);
  }

  deleteTrain(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/trains/${id}`);
  }

  // --- Schedules ---
  listSchedules(): Observable<AdminSchedule[]> {
    return this.http.get<AdminSchedule[]>(`${this.base}/schedules/all`);
  }

  createSchedule(body: ScheduleRequest): Observable<AdminSchedule> {
    return this.http.post<AdminSchedule>(`${this.base}/schedules`, body);
  }

  updateSchedule(id: string, body: ScheduleRequest): Observable<AdminSchedule> {
    return this.http.put<AdminSchedule>(`${this.base}/schedules/${id}`, body);
  }

  deleteSchedule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/schedules/${id}`);
  }
}

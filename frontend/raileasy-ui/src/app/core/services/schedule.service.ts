import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ScheduleSearchResult } from '../models/schedule.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  private apiUrl = `${environment.apiUrl}/schedules`;

  constructor(private http: HttpClient) {}

  searchSchedules(from: string, to: string, date: string): Observable<ScheduleSearchResult[]> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('date', date);
    return this.http.get<ScheduleSearchResult[]>(this.apiUrl, { params });
  }
}

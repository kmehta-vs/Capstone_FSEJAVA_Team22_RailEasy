import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ScheduleSearchResult } from '../../../core/models/schedule.model';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.scss'
})
export class SearchResultsComponent implements OnInit {
  results: ScheduleSearchResult[] = [];
  loading = false;
  error = '';
  from = '';
  to = '';
  date = '';

  constructor(
    private route: ActivatedRoute,
    private scheduleService: ScheduleService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.from = params['from'] || '';
      this.to = params['to'] || '';
      this.date = params['date'] || '';

      if (this.from && this.to && this.date) {
        this.searchTrains();
      }
    });
  }

  searchTrains(): void {
    this.loading = true;
    this.error = '';
    this.scheduleService.searchSchedules(this.from, this.to, this.date).subscribe({
      next: (data) => {
        this.results = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch train schedules. Please try again.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getClassLabel(travelClass: string): string {
    switch (travelClass) {
      case 'SLEEPER': return 'Sleeper';
      case 'AC_3': return 'AC 3-Tier';
      case 'AC_2': return 'AC 2-Tier';
      default: return travelClass;
    }
  }

  formatTime(dateTime: string): string {
    return new Date(dateTime).toLocaleTimeString('en-IN', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  }

  formatDate(dateTime: string): string {
    return new Date(dateTime).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short'
    });
  }
}

import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ScheduleService } from '../../core/services/schedule.service';
import {
  ClassAvailability,
  ScheduleSearchResult,
  TRAVEL_CLASS_LABELS,
} from '../../core/models/api-models';

@Component({
  selector: 'app-search',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './search.html',
  styleUrl: './search.scss',
})
export class Search {
  private readonly fb = inject(FormBuilder);
  private readonly scheduleService = inject(ScheduleService);
  private readonly router = inject(Router);

  protected readonly labels = TRAVEL_CLASS_LABELS;

  protected readonly loading = signal(false);
  protected readonly searched = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly results = signal<ScheduleSearchResult[]>([]);

  protected readonly form = this.fb.group({
    from: ['Chennai Central', Validators.required],
    to: ['Mumbai CSMT', Validators.required],
    date: [new Date('2025-10-21'), Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { from, to, date } = this.form.getRawValue();
    const isoDate = this.toIsoDate(date as Date);

    this.loading.set(true);
    this.error.set(null);
    this.scheduleService.search(from!, to!, isoDate).subscribe({
      next: (res) => {
        this.results.set(res);
        this.searched.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load trains. Is the backend running on :8080?');
        this.results.set([]);
        this.searched.set(true);
        this.loading.set(false);
      },
    });
  }

  private toIsoDate(date: Date): string {
    const y = date.getFullYear();
    const m = `${date.getMonth() + 1}`.padStart(2, '0');
    const d = `${date.getDate()}`.padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  /** Navigate to seat selection for a schedule + chosen class. */
  selectClass(schedule: ScheduleSearchResult, cls: ClassAvailability): void {
    this.router.navigate(['/book', schedule.scheduleId], {
      state: {
        travelClass: cls.travelClass,
        trainName: `${schedule.trainName} · ${schedule.trainNumber}`,
        fare: cls.fare,
      },
    });
  }
}

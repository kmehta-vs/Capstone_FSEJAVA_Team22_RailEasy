import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ScheduleService } from '../../core/services/schedule.service';
import { BookingService } from '../../core/services/booking.service';
import { TravelClass, TRAVEL_CLASS_LABELS } from '../../core/models/api-models';

const ROWS = 8;
const COLS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
const MAX_SEATS = 4;

interface Seat {
  label: string;
  booked: boolean;
}

/**
 * Seat selection for a schedule + travel class. Renders an 8x8 grid
 * (green = available, red = booked) and lets the user pick 1–4 seats.
 * Booking confirmation is wired up in Sprint 3.
 */
@Component({
  selector: 'app-seat-selection',
  imports: [
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './seat-selection.html',
  styleUrl: './seat-selection.scss',
})
export class SeatSelection {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly scheduleService = inject(ScheduleService);
  private readonly bookingService = inject(BookingService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly labels = TRAVEL_CLASS_LABELS;
  protected readonly maxSeats = MAX_SEATS;

  protected readonly loading = signal(false);
  protected readonly booking = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly seats = signal<Seat[]>([]);
  protected readonly selected = signal<string[]>([]);

  // Journey context (passed via router state from search results).
  protected readonly scheduleId: string;
  protected readonly travelClass: TravelClass;
  protected readonly trainName: string;
  protected readonly fare: number;

  protected readonly totalFare = computed(() => this.fare * this.selected().length);

  constructor() {
    this.scheduleId = this.route.snapshot.paramMap.get('scheduleId') ?? '';
    const nav = this.router.getCurrentNavigation()?.extras.state ?? history.state ?? {};
    this.travelClass = (nav['travelClass'] as TravelClass) ?? 'SLEEPER';
    this.trainName = (nav['trainName'] as string) ?? 'Train';
    this.fare = (nav['fare'] as number) ?? 0;
    this.loadSeats();
  }

  private loadSeats(): void {
    this.loading.set(true);
    this.error.set(null);
    this.scheduleService.seats(this.scheduleId, this.travelClass).subscribe({
      next: (res) => {
        const booked = new Set(res.bookedSeats);
        const grid: Seat[] = [];
        for (let r = 1; r <= ROWS; r++) {
          for (const c of COLS) {
            const label = `${r}${c}`;
            grid.push({ label, booked: booked.has(label) });
          }
        }
        this.seats.set(grid);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load seats. Is the backend running on :8080?');
        this.loading.set(false);
      },
    });
  }

  isSelected(label: string): boolean {
    return this.selected().includes(label);
  }

  toggle(seat: Seat): void {
    if (seat.booked) {
      return;
    }
    const current = this.selected();
    if (current.includes(seat.label)) {
      this.selected.set(current.filter((s) => s !== seat.label));
      return;
    }
    if (current.length >= MAX_SEATS) {
      this.snackBar.open(`You can select up to ${MAX_SEATS} seats`, 'Dismiss', { duration: 3000 });
      return;
    }
    this.selected.set([...current, seat.label]);
  }

  proceed(): void {
    if (this.selected().length === 0) {
      this.snackBar.open('Please select at least one seat', 'Dismiss', { duration: 3000 });
      return;
    }
    this.booking.set(true);
    this.bookingService
      .create({
        scheduleId: this.scheduleId,
        travelClass: this.travelClass,
        seatNumbers: this.selected(),
      })
      .subscribe({
        next: (created) => {
          this.booking.set(false);
          this.router.navigate(['/booking-confirmation'], { state: { booking: created } });
        },
        error: (err) => {
          this.booking.set(false);
          const message = err?.error?.message ?? 'Booking failed. Please try again.';
          this.snackBar.open(message, 'Dismiss', { duration: 4000 });
          // A conflict likely means someone just booked a seat — refresh the grid.
          if (err?.status === 409) {
            this.selected.set([]);
            this.loadSeats();
          }
        },
      });
  }

  back(): void {
    this.router.navigate(['/']);
  }
}

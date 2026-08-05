import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { BookingService } from '../../core/services/booking.service';
import { Booking, TRAVEL_CLASS_LABELS } from '../../core/models/api-models';

/**
 * "My Tickets" — lists the current user's bookings with a cancel action.
 */
@Component({
  selector: 'app-my-tickets',
  imports: [
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
  ],
  templateUrl: './my-tickets.html',
  styleUrl: './my-tickets.scss',
})
export class MyTickets {
  private readonly bookingService = inject(BookingService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly labels = TRAVEL_CLASS_LABELS;
  protected readonly columns = ['pnr', 'train', 'date', 'class', 'seats', 'total', 'status', 'actions'];

  protected readonly loading = signal(false);
  protected readonly cancellingId = signal<string | null>(null);
  protected readonly tickets = signal<Booking[]>([]);
  protected readonly isEmpty = computed(() => !this.loading() && this.tickets().length === 0);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.bookingService.mine().subscribe({
      next: (res) => {
        this.tickets.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Could not load your tickets.', 'Dismiss', { duration: 4000 });
      },
    });
  }

  cancel(ticket: Booking): void {
    this.cancellingId.set(ticket.id);
    this.bookingService.cancel(ticket.id).subscribe({
      next: (updated) => {
        this.cancellingId.set(null);
        this.tickets.set(this.tickets().map((t) => (t.id === updated.id ? updated : t)));
        this.snackBar.open(`Ticket ${updated.pnrNumber} cancelled.`, 'Dismiss', { duration: 3000 });
      },
      error: (err) => {
        this.cancellingId.set(null);
        const message = err?.error?.message ?? 'Could not cancel this ticket.';
        this.snackBar.open(message, 'Dismiss', { duration: 4000 });
      },
    });
  }
}

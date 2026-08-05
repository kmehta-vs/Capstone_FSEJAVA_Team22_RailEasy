import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { Booking, TRAVEL_CLASS_LABELS } from '../../core/models/api-models';

/**
 * Post-booking confirmation screen. Receives the created {@link Booking} via
 * router state and shows the PNR prominently.
 */
@Component({
  selector: 'app-booking-confirmation',
  imports: [DatePipe, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './booking-confirmation.html',
  styleUrl: './booking-confirmation.scss',
})
export class BookingConfirmation {
  private readonly router = inject(Router);

  protected readonly labels = TRAVEL_CLASS_LABELS;
  protected readonly booking = signal<Booking | null>(
    (history.state?.['booking'] as Booking) ?? null,
  );

  constructor() {
    // If navigated here directly (no booking in state), go home.
    if (!this.booking()) {
      this.router.navigate(['/']);
    }
  }
}

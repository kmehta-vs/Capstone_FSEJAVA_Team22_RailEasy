import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AdminService } from '../../core/services/admin.service';
import { AdminSchedule, Train } from '../../core/models/api-models';
import { TrainDialog, TrainDialogData } from './train-dialog';
import { ScheduleDialog, ScheduleDialogData } from './schedule-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/confirm-dialog/confirm-dialog';

/**
 * Admin console — manage trains and schedules (create/edit/delete) via Material dialogs.
 */
@Component({
  selector: 'app-admin',
  imports: [
    DatePipe,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  private readonly adminService = inject(AdminService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly trainColumns = ['number', 'name', 'seats', 'actions'];
  protected readonly scheduleColumns = ['train', 'route', 'departure', 'fares', 'actions'];

  protected readonly loading = signal(false);
  protected readonly trains = signal<Train[]>([]);
  protected readonly schedules = signal<AdminSchedule[]>([]);
  protected readonly hasTrains = computed(() => this.trains().length > 0);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.adminService.listTrains().subscribe({
      next: (t) => this.trains.set(t),
      error: () => this.toast('Could not load trains.'),
    });
    this.adminService.listSchedules().subscribe({
      next: (s) => {
        this.schedules.set(s);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast('Could not load schedules.');
      },
    });
  }

  // --- Trains ---
  addTrain(): void {
    this.openTrainDialog();
  }

  editTrain(train: Train): void {
    this.openTrainDialog(train);
  }

  private openTrainDialog(train?: Train): void {
    const ref = this.dialog.open(TrainDialog, {
      width: '420px',
      data: { train } as TrainDialogData,
    });
    ref.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }
      const obs = train
        ? this.adminService.updateTrain(train.id, result)
        : this.adminService.createTrain(result);
      obs.subscribe({
        next: () => {
          this.toast(train ? 'Train updated.' : 'Train created.');
          this.reload();
        },
        error: (err) => this.toast(err?.error?.message ?? 'Save failed.'),
      });
    });
  }

  deleteTrain(train: Train): void {
    this.confirm({
      title: 'Delete this train?',
      message: `${train.trainName} (${train.trainNumber}) and any schedules that depend on it will be removed. This can't be undone.`,
      confirmLabel: 'Delete train',
      tone: 'danger',
    }).subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.adminService.deleteTrain(train.id).subscribe({
        next: () => {
          this.toast('Train deleted.');
          this.reload();
        },
        error: (err) => this.toast(err?.error?.message ?? 'Delete failed.'),
      });
    });
  }

  // --- Schedules ---
  addSchedule(): void {
    this.openScheduleDialog();
  }

  editSchedule(schedule: AdminSchedule): void {
    this.openScheduleDialog(schedule);
  }

  private openScheduleDialog(schedule?: AdminSchedule): void {
    const ref = this.dialog.open(ScheduleDialog, {
      width: '640px',
      data: { schedule, trains: this.trains() } as ScheduleDialogData,
    });
    ref.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }
      const obs = schedule
        ? this.adminService.updateSchedule(schedule.id, result)
        : this.adminService.createSchedule(result);
      obs.subscribe({
        next: () => {
          this.toast(schedule ? 'Schedule updated.' : 'Schedule created.');
          this.reload();
        },
        error: (err) => this.toast(err?.error?.message ?? 'Save failed.'),
      });
    });
  }

  deleteSchedule(schedule: AdminSchedule): void {
    this.confirm({
      title: 'Delete this schedule?',
      message: `${schedule.trainName} · ${schedule.fromStation} → ${schedule.toStation} will no longer be bookable. This can't be undone.`,
      confirmLabel: 'Delete schedule',
      tone: 'danger',
    }).subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.adminService.deleteSchedule(schedule.id).subscribe({
        next: () => {
          this.toast('Schedule deleted.');
          this.reload();
        },
        error: (err) => this.toast(err?.error?.message ?? 'Delete failed.'),
      });
    });
  }

  private confirm(data: ConfirmDialogData) {
    return this.dialog.open(ConfirmDialog, { width: '400px', data }).afterClosed();
  }

  private toast(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: 3000 });
  }
}

import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import {
  AdminSchedule,
  ScheduleRequest,
  Train,
} from '../../core/models/api-models';

export interface ScheduleDialogData {
  schedule?: AdminSchedule;
  trains: Train[];
}

/**
 * Add/Edit dialog for a schedule. Returns a {@link ScheduleRequest} on save.
 * Datetime fields use `datetime-local`; the journey date is derived from departure.
 */
@Component({
  selector: 'app-schedule-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './schedule-dialog.html',
})
export class ScheduleDialog {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ScheduleDialog>);
  protected readonly data = inject<ScheduleDialogData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.schedule;
  protected readonly trains = this.data?.trains ?? [];

  private readonly s = this.data?.schedule;

  protected readonly form = this.fb.group({
    trainId: [this.s?.trainId ?? '', Validators.required],
    fromStation: [this.s?.fromStation ?? '', Validators.required],
    toStation: [this.s?.toStation ?? '', Validators.required],
    departureTime: [this.trim(this.s?.departureTime) ?? '', Validators.required],
    arrivalTime: [this.trim(this.s?.arrivalTime) ?? '', Validators.required],
    fareSleeper: [this.s?.fareSleeper ?? 0, [Validators.required, Validators.min(1)]],
    fareAc3: [this.s?.fareAc3 ?? 0, [Validators.required, Validators.min(1)]],
    fareAc2: [this.s?.fareAc2 ?? 0, [Validators.required, Validators.min(1)]],
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const request: ScheduleRequest = {
      trainId: v.trainId!,
      fromStation: v.fromStation!,
      toStation: v.toStation!,
      departureTime: this.withSeconds(v.departureTime!),
      arrivalTime: this.withSeconds(v.arrivalTime!),
      journeyDate: v.departureTime!.substring(0, 10),
      fareSleeper: Number(v.fareSleeper),
      fareAc3: Number(v.fareAc3),
      fareAc2: Number(v.fareAc2),
    };
    this.dialogRef.close(request);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  /** ISO string → value accepted by datetime-local input (yyyy-MM-ddTHH:mm). */
  private trim(iso?: string): string | undefined {
    return iso ? iso.substring(0, 16) : undefined;
  }

  /** datetime-local value → ISO LocalDateTime with seconds. */
  private withSeconds(value: string): string {
    return value.length === 16 ? `${value}:00` : value;
  }
}

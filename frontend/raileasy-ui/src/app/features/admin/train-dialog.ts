import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { Train, TrainRequest } from '../../core/models/api-models';

export interface TrainDialogData {
  train?: Train;
}

/**
 * Add/Edit dialog for a train. Returns a {@link TrainRequest} on save.
 */
@Component({
  selector: 'app-train-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './train-dialog.html',
})
export class TrainDialog {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TrainDialog>);
  protected readonly data = inject<TrainDialogData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.train;

  protected readonly form = this.fb.group({
    trainNumber: [this.data?.train?.trainNumber ?? '', Validators.required],
    trainName: [this.data?.train?.trainName ?? '', Validators.required],
    seatsPerClass: [
      this.data?.train?.seatsPerClass ?? 64,
      [Validators.required, Validators.min(8), Validators.max(64)],
    ],
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue() as TrainRequest);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

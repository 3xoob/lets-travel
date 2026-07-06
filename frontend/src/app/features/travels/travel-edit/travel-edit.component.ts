import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TravelService } from '../../../core/services/travel.service';
import { TravelResponse } from '../../../core/models';

@Component({
  selector: 'app-travel-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatSelectModule],
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header><mat-card-title>Edit Travel</mat-card-title></mat-card-header>
        <mat-card-content>
          @if (travel()) {
            <form [formGroup]="form" (ngSubmit)="submit()">
              <div class="two-col">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Title</mat-label>
                  <input matInput formControlName="title">
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Status</mat-label>
                  <mat-select formControlName="status">
                    <mat-option value="DRAFT">Draft</mat-option>
                    <mat-option value="PUBLISHED">Published</mat-option>
                    <mat-option value="CANCELLED">Cancelled</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="5"></textarea>
              </mat-form-field>
              <div class="three-col">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Price (USD)</mat-label>
                  <input matInput type="number" formControlName="price">
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Start Date</mat-label>
                  <input matInput type="date" formControlName="startDate">
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>End Date</mat-label>
                  <input matInput type="date" formControlName="endDate">
                </mat-form-field>
              </div>
              <div class="form-actions">
                <button mat-flat-button color="primary" type="submit" [disabled]="form.pristine || saving()">
                  {{ saving() ? 'Saving...' : 'Save Changes' }}
                </button>
                <button mat-button type="button" (click)="router.navigate(['/manager/travels'])">Cancel</button>
              </div>
            </form>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-container { max-width: 800px; margin: 0 auto; }
    .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .three-col { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
    @media (max-width: 600px) { .two-col, .three-col { grid-template-columns: 1fr; } }
    .full-width { width: 100%; margin-bottom: 8px; }
    .form-actions { display: flex; gap: 12px; }
  `],
})
export class TravelEditComponent implements OnInit {
  @Input() id!: string;
  router = inject(Router);
  private travelService = inject(TravelService);
  private snack = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  travel = signal<TravelResponse | null>(null);
  saving = signal(false);

  form = this.fb.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    price: [null as number | null],
    status: [''],
    startDate: [''],
    endDate: [''],
  });

  ngOnInit() {
    this.travelService.getById(this.id).subscribe(t => {
      this.travel.set(t);
      this.form.patchValue({
        title: t.title, description: t.description, price: t.price,
        status: t.status, startDate: t.startDate, endDate: t.endDate,
      });
    });
  }

  submit() {
    if (this.form.pristine || this.form.invalid) return;
    this.saving.set(true);
    this.travelService.update(this.id, this.form.value as any).subscribe({
      next: t => { this.saving.set(false); this.snack.open('Saved!', 'OK', { duration: 3000 }); this.router.navigate(['/travels', t.id]); },
      error: err => { this.saving.set(false); this.snack.open(err.error?.message ?? 'Failed', 'Close'); },
    });
  }
}

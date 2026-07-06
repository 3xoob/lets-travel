import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TravelService } from '../../../core/services/travel.service';

@Component({
  selector: 'app-travel-create',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatChipsModule, MatIconModule,
  ],
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header><mat-card-title>Create New Travel</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <div class="two-col">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Title</mat-label>
                <input matInput formControlName="title">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Category</mat-label>
                <mat-select formControlName="category">
                  @for (c of categories; track c) {
                    <mat-option [value]="c">{{ c | titlecase }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            </div>

            <div class="two-col">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Destination (City)</mat-label>
                <input matInput formControlName="destination">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Country</mat-label>
                <input matInput formControlName="country">
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description</mat-label>
              <textarea matInput formControlName="description" rows="5"></textarea>
            </mat-form-field>

            <div class="three-col">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Price (USD)</mat-label>
                <input matInput type="number" formControlName="price" min="0">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Capacity</mat-label>
                <input matInput type="number" formControlName="capacity" min="1">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Status</mat-label>
                <mat-select formControlName="status">
                  <mat-option value="DRAFT">Draft</mat-option>
                  <mat-option value="PUBLISHED">Published</mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <div class="two-col">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Start Date</mat-label>
                <input matInput type="date" formControlName="startDate">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>End Date</mat-label>
                <input matInput type="date" formControlName="endDate">
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Tags (comma-separated)</mat-label>
              <input matInput formControlName="tagsInput" placeholder="beach, adventure, family">
            </mat-form-field>

            <mat-card-actions>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">
                {{ saving() ? 'Creating...' : 'Create Travel' }}
              </button>
              <button mat-button type="button" (click)="router.navigate(['/manager/travels'])">Cancel</button>
            </mat-card-actions>
          </form>
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
  `],
})
export class TravelCreateComponent {
  router = inject(Router);
  private travelService = inject(TravelService);
  private snack = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  saving = signal(false);

  categories = ['ADVENTURE', 'CULTURAL', 'RELAXATION', 'BUSINESS', 'SPORT', 'NATURE', 'CITY_BREAK', 'CRUISE'];

  form = this.fb.group({
    title: ['', Validators.required],
    category: ['', Validators.required],
    destination: ['', Validators.required],
    country: ['', Validators.required],
    description: ['', Validators.required],
    price: [null as number | null, [Validators.required, Validators.min(0)]],
    capacity: [null as number | null, [Validators.required, Validators.min(1)]],
    status: ['DRAFT'],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    tagsInput: [''],
  });

  submit() {
    if (this.form.invalid) return;
    const v = this.form.value;
    const tags = (v.tagsInput ?? '').split(',').map((t: string) => t.trim()).filter(Boolean);
    this.saving.set(true);
    this.travelService.create({
      title: v.title!, category: v.category as any, destination: v.destination!,
      country: v.country!, description: v.description!, price: v.price!,
      capacity: v.capacity!, status: v.status as any,
      startDate: v.startDate!, endDate: v.endDate!, tags,
    }).subscribe({
      next: t => { this.saving.set(false); this.router.navigate(['/travels', t.id]); },
      error: err => { this.saving.set(false); this.snack.open(err.error?.message ?? 'Failed', 'Close'); },
    });
  }
}

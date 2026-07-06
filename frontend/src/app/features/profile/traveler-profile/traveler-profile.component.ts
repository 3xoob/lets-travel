import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { TravelerStatsResponse } from '../../../core/models';

@Component({
  selector: 'app-traveler-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CurrencyPipe, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <h1>My Profile</h1>
    <div class="profile-layout">
      <mat-card class="profile-card">
        <mat-card-header>
          <mat-card-title>{{ auth.currentUser?.firstName }} {{ auth.currentUser?.lastName }}</mat-card-title>
          <mat-card-subtitle>{{ auth.currentUser?.email }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="save()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>First Name</mat-label>
              <input matInput formControlName="firstName">
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Last Name</mat-label>
              <input matInput formControlName="lastName">
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" [disabled]="form.pristine || saving()">
              {{ saving() ? 'Saving...' : 'Save Changes' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>

      @if (stats()) {
        <mat-card class="stats-card">
          <mat-card-header><mat-card-title>Travel Stats</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="stat"><mat-icon>check_circle</mat-icon><span>{{ stats()!.pastTrips }} completed trips</span></div>
            <div class="stat"><mat-icon>upcoming</mat-icon><span>{{ stats()!.upcomingTrips }} upcoming trips</span></div>
            <div class="stat"><mat-icon>payments</mat-icon><span>{{ stats()!.totalSpend | currency }} total spent</span></div>
            <div class="stat"><mat-icon>cancel</mat-icon><span>{{ stats()!.cancellations }} cancellations</span></div>
            <div class="stat"><mat-icon>star</mat-icon><span>{{ stats()!.reviewsGiven }} reviews given</span></div>
            <div class="stat"><mat-icon>flag</mat-icon><span>{{ stats()!.reportsFiled }} reports filed</span></div>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 24px; }
    .profile-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    @media (max-width: 768px) { .profile-layout { grid-template-columns: 1fr; } }
    .full-width { width: 100%; margin-bottom: 12px; }
    .stat { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; color: #475569; }
  `],
})
export class TravelerProfileComponent implements OnInit {
  private userService = inject(UserService);
  auth = inject(AuthService);
  private snack = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  stats = signal<TravelerStatsResponse | null>(null);
  saving = signal(false);

  form = this.fb.group({
    firstName: [this.auth.currentUser?.firstName ?? '', Validators.required],
    lastName: [this.auth.currentUser?.lastName ?? '', Validators.required],
  });

  ngOnInit() {
    this.userService.getMyStats().subscribe(s => this.stats.set(s));
  }

  save() {
    this.saving.set(true);
    this.userService.updateProfile(this.form.value as any).subscribe({
      next: () => { this.saving.set(false); this.snack.open('Saved!', 'OK', { duration: 3000 }); this.form.markAsPristine(); },
      error: () => { this.saving.set(false); this.snack.open('Save failed', 'Close'); },
    });
  }
}

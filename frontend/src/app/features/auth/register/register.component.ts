import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { Store } from '@ngrx/store';
import { register } from '../../../store/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../../store/auth/auth.selectors';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, MatIconModule,
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title>Create account</mat-card-title>
          <mat-card-subtitle>Join Let's Travel today</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <div class="name-row">
              <mat-form-field appearance="outline">
                <mat-label>First name</mat-label>
                <input matInput formControlName="firstName">
                @if (form.get('firstName')?.hasError('required')) {
                  <mat-error>Required</mat-error>
                }
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Last name</mat-label>
                <input matInput formControlName="lastName">
                @if (form.get('lastName')?.hasError('required')) {
                  <mat-error>Required</mat-error>
                }
              </mat-form-field>
            </div>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email">
              @if (form.get('email')?.hasError('required')) {
                <mat-error>Email is required</mat-error>
              }
              @if (form.get('email')?.hasError('email')) {
                <mat-error>Enter a valid email</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [type]="hidePass ? 'password' : 'text'" formControlName="password" autocomplete="new-password">
              <button mat-icon-button matSuffix type="button" (click)="hidePass=!hidePass">
                <mat-icon>{{ hidePass ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (form.get('password')?.hasError('minlength')) {
                <mat-error>Password must be at least 8 characters</mat-error>
              }
            </mat-form-field>
            @if (error$ | async; as err) {
              <div class="error-banner">{{ err }}</div>
            }
            <button mat-flat-button color="primary" type="submit" class="submit-btn"
                    [disabled]="form.invalid || (loading$ | async)">
              @if (loading$ | async) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                Create Account
              }
            </button>
          </form>
        </mat-card-content>
        <mat-card-actions>
          <p class="link-row">Already have an account? <a routerLink="/login">Sign in</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .auth-container { min-height: calc(100vh - 120px); display: flex; align-items: center; justify-content: center; }
    .auth-card { width: 100%; max-width: 440px; padding: 16px; }
    .name-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .submit-btn { width: 100%; margin-top: 8px; height: 44px; }
    .error-banner { background: #fee2e2; color: #b91c1c; padding: 10px 14px; border-radius: 8px; font-size: 14px; margin-bottom: 12px; }
    .link-row { text-align: center; font-size: 14px; margin: 0; }
    .link-row a { color: #4f46e5; font-weight: 500; }
  `],
})
export class RegisterComponent {
  private store = inject(Store);
  private fb = inject(FormBuilder);

  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  loading$ = this.store.select(selectAuthLoading);
  error$ = this.store.select(selectAuthError);
  hidePass = true;

  submit() {
    if (this.form.invalid) return;
    const { email, password, firstName, lastName } = this.form.value;
    this.store.dispatch(register({ email: email!, password: password!, firstName: firstName!, lastName: lastName! }));
  }
}

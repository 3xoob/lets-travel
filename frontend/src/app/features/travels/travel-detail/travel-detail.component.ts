import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TravelService } from '../../../core/services/travel.service';
import { FeedbackService } from '../../../core/services/feedback.service';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { AuthService } from '../../../core/services/auth.service';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';
import { TravelResponse, FeedbackResponse, PaymentMethod } from '../../../core/models';

@Component({
  selector: 'app-travel-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, CurrencyPipe, DatePipe,
    MatCardModule, MatButtonModule, MatChipsModule, MatIconModule,
    MatDialogModule, MatDividerModule, MatSelectModule, MatFormFieldModule,
    StarRatingComponent,
  ],
  template: `
    @if (travel()) {
      <div class="detail-layout">
        <div class="gallery">
          <img [src]="mainImage()" [alt]="travel()!.title" class="main-img">
          @if (travel()!.imageUrls.length > 1) {
            <div class="thumbs">
              @for (url of travel()!.imageUrls; track url) {
                <img [src]="url" (click)="mainImage.set(url)" [class.active]="mainImage() === url">
              }
            </div>
          }
        </div>

        <div class="info">
          <div class="badges">
            <span class="badge badge-info">{{ travel()!.category }}</span>
            <span class="badge" [class]="statusClass(travel()!.status)">{{ travel()!.status }}</span>
          </div>

          <h1>{{ travel()!.title }}</h1>
          <div class="destination"><mat-icon>place</mat-icon> {{ travel()!.destination }}, {{ travel()!.country }}</div>

          <div class="price-row">
            <span class="price">{{ travel()!.price | currency }}</span>
            <span class="capacity">
              <mat-icon>group</mat-icon>
              {{ travel()!.currentEnrollment }}/{{ travel()!.capacity }} enrolled
            </span>
          </div>

          <div class="dates">
            <mat-icon>calendar_today</mat-icon>
            {{ travel()!.startDate | date:'mediumDate' }} — {{ travel()!.endDate | date:'mediumDate' }}
          </div>

          @if (travel()!.tags.length > 0) {
            <mat-chip-set>
              @for (tag of travel()!.tags; track tag) {
                <mat-chip>{{ tag }}</mat-chip>
              }
            </mat-chip-set>
          }

          <mat-divider></mat-divider>

          <div class="manager-box">
            <strong>Your Manager</strong>
            <a [routerLink]="['/managers', travel()!.manager.id]" class="manager-link">
              {{ travel()!.manager.firstName }} {{ travel()!.manager.lastName }}
            </a>
            <app-star-rating [value]="travel()!.manager.averageRating" />
          </div>

          @if (auth.isLoggedIn && travel()!.canSubscribe) {
            <div class="subscribe-row">
              <mat-form-field appearance="outline">
                <mat-label>Payment</mat-label>
                <mat-select [(ngModel)]="selectedMethod">
                  <mat-option value="STRIPE">Credit Card (Stripe)</mat-option>
                  <mat-option value="PAYPAL">PayPal</mat-option>
                </mat-select>
              </mat-form-field>
              <button mat-flat-button color="accent" (click)="subscribe()" [disabled]="subscribing()">
                {{ subscribing() ? 'Processing...' : 'Subscribe' }}
              </button>
            </div>
          }

          @if (!auth.isLoggedIn) {
            <a mat-flat-button color="primary" routerLink="/login">Login to Subscribe</a>
          }
        </div>
      </div>

      <mat-divider></mat-divider>

      <section class="description">
        <h2>About this trip</h2>
        <p>{{ travel()!.description }}</p>
      </section>

      <section class="feedback">
        <h2>Reviews ({{ feedbackTotal() }})</h2>
        @for (fb of feedbacks(); track fb.id) {
          <mat-card class="feedback-card">
            <mat-card-header>
              <mat-card-title>{{ fb.travelerDisplayName }}</mat-card-title>
              <mat-card-subtitle>{{ fb.createdAt | date:'mediumDate' }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <app-star-rating [value]="fb.rating" [showValue]="false" />
              <p>{{ fb.comment }}</p>
            </mat-card-content>
          </mat-card>
        }
      </section>
    }
  `,
  styles: [`
    .detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; margin-bottom: 32px; }
    @media (max-width: 768px) { .detail-layout { grid-template-columns: 1fr; } }
    .main-img { width: 100%; height: 400px; object-fit: cover; border-radius: 12px; }
    .thumbs { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
    .thumbs img { width: 80px; height: 60px; object-fit: cover; border-radius: 6px; cursor: pointer; opacity: .7; }
    .thumbs img.active { opacity: 1; outline: 2px solid #4f46e5; }
    h1 { font-size: 28px; font-weight: 700; margin: 8px 0; }
    .destination { display: flex; align-items: center; gap: 4px; color: #64748b; margin-bottom: 16px; }
    .price-row { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
    .price { font-size: 28px; font-weight: 700; color: #4f46e5; }
    .capacity { display: flex; align-items: center; gap: 4px; color: #64748b; }
    .dates { display: flex; align-items: center; gap: 4px; color: #64748b; margin-bottom: 16px; }
    .manager-box { margin: 16px 0; display: flex; flex-direction: column; gap: 4px; }
    .manager-link { color: #4f46e5; font-weight: 500; }
    .subscribe-row { display: flex; align-items: center; gap: 12px; margin-top: 16px; }
    .feedback { margin-top: 32px; }
    .feedback-card { margin-bottom: 12px; }
  `],
})
export class TravelDetailComponent implements OnInit {
  @Input() id!: string;
  private travelService = inject(TravelService);
  private feedbackService = inject(FeedbackService);
  private subscriptionService = inject(SubscriptionService);
  private snack = inject(MatSnackBar);
  auth = inject(AuthService);

  travel = signal<TravelResponse | null>(null);
  mainImage = signal<string>('');
  feedbacks = signal<FeedbackResponse[]>([]);
  feedbackTotal = signal(0);
  subscribing = signal(false);
  selectedMethod: PaymentMethod = 'STRIPE';

  ngOnInit() {
    this.travelService.getById(this.id).subscribe(t => {
      this.travel.set(t);
      this.mainImage.set(t.thumbnailUrl || t.imageUrls[0] || '/assets/placeholder-travel.jpg');
    });
    this.feedbackService.getTravelFeedback(this.id, 0, 10).subscribe(r => {
      this.feedbacks.set(r.content);
      this.feedbackTotal.set(r.totalElements);
    });
  }

  statusClass(s: string) {
    const map: Record<string, string> = {
      PUBLISHED: 'badge-success', DRAFT: 'badge-default',
      COMPLETED: 'badge-info', CANCELLED: 'badge-error',
    };
    return `badge ${map[s] ?? 'badge-default'}`;
  }

  subscribe() {
    this.subscribing.set(true);
    this.subscriptionService.subscribe(this.id, this.selectedMethod).subscribe({
      next: res => {
        this.subscribing.set(false);
        if (res.approvalUrl) window.location.href = res.approvalUrl;
        else if (res.clientSecret) this.snack.open('Stripe payment initiated — implement Stripe.js here', 'OK');
        else this.snack.open('Subscribed!', 'OK', { duration: 3000 });
      },
      error: err => {
        this.subscribing.set(false);
        this.snack.open(err.error?.message ?? 'Subscription failed', 'Close', { duration: 4000 });
      },
    });
  }
}

import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../../core/services/user.service';
import { FeedbackService } from '../../../core/services/feedback.service';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';
import { ManagerProfileResponse, FeedbackResponse } from '../../../core/models';

@Component({
  selector: 'app-manager-public-profile',
  standalone: true,
  imports: [CommonModule, DecimalPipe, MatCardModule, MatIconModule, StarRatingComponent],
  template: `
    @if (profile()) {
      <div class="profile-layout">
        <mat-card class="profile-info">
          <mat-card-header>
            <mat-card-title>{{ profile()!.user.firstName }} {{ profile()!.user.lastName }}</mat-card-title>
            <mat-card-subtitle>Travel Manager</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            @if (profile()!.bio) {
              <p>{{ profile()!.bio }}</p>
            }
            <div class="stats-row">
              <div class="stat">
                <mat-icon>star</mat-icon>
                <app-star-rating [value]="profile()!.averageRating" />
              </div>
              <div class="stat">
                <mat-icon>flight_takeoff</mat-icon>
                <span>{{ profile()!.totalTrips }} trips organized</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <div class="feedback-section">
          <h2>Reviews</h2>
          @for (fb of feedbacks(); track fb.id) {
            <mat-card class="feedback-card">
              <mat-card-header>
                <mat-card-title>{{ fb.travelerDisplayName }}</mat-card-title>
                <mat-card-subtitle>{{ fb.travelTitle }}</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <app-star-rating [value]="fb.rating" [showValue]="false" />
                @if (fb.comment) { <p>{{ fb.comment }}</p> }
              </mat-card-content>
            </mat-card>
          }
          @if (feedbacks().length === 0) {
            <p class="no-reviews">No reviews yet.</p>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .profile-layout { display: grid; grid-template-columns: 320px 1fr; gap: 24px; }
    @media (max-width: 768px) { .profile-layout { grid-template-columns: 1fr; } }
    .stats-row { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
    .stat { display: flex; align-items: center; gap: 8px; color: #475569; }
    .feedback-card { margin-bottom: 12px; }
    .no-reviews { color: #94a3b8; }
  `],
})
export class ManagerPublicProfileComponent implements OnInit {
  @Input() id!: string;
  private userService = inject(UserService);
  private feedbackService = inject(FeedbackService);
  profile = signal<ManagerProfileResponse | null>(null);
  feedbacks = signal<FeedbackResponse[]>([]);

  ngOnInit() {
    this.userService.getManagerProfile(this.id).subscribe(p => this.profile.set(p));
    this.feedbackService.getManagerFeedback(0).subscribe(r => this.feedbacks.set(r.content));
  }
}

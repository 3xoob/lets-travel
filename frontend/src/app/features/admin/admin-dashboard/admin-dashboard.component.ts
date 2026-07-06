import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';
import { TravelCardComponent } from '../../../shared/components/travel-card/travel-card.component';
import { AdminDashboardResponse } from '../../../core/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPipe, DecimalPipe,
    MatCardModule, MatButtonModule, MatIconModule, MatTableModule,
    StarRatingComponent, TravelCardComponent],
  template: `
    <h1>Admin Dashboard</h1>
    @if (dash()) {
      <div class="kpi-grid">
        <mat-card class="kpi">
          <mat-icon>people</mat-icon>
          <div class="kpi-value">{{ dash()!.totalUsers }}</div>
          <div class="kpi-label">Total Users</div>
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>flight</mat-icon>
          <div class="kpi-value">{{ dash()!.totalTravels }}</div>
          <div class="kpi-label">Total Travels</div>
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>flag</mat-icon>
          <div class="kpi-value">{{ dash()!.openReports }}</div>
          <div class="kpi-label">Open Reports</div>
          @if (dash()!.openReports > 0) {
            <a routerLink="/admin/reports" class="kpi-action">Review</a>
          }
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>payments</mat-icon>
          <div class="kpi-value">
            {{ totalIncome() | currency }}
          </div>
          <div class="kpi-label">Total Platform Income</div>
        </mat-card>
      </div>

      <div class="section-grid">
        <mat-card>
          <mat-card-header><mat-card-title>Top Managers</mat-card-title></mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="dash()!.topManagers.slice(0,5)" class="full-width">
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef>Manager</th>
                <td mat-cell *matCellDef="let m; let i = index">
                  <span class="rank">{{ i + 1 }}</span>
                  <a [routerLink]="['/managers', m.userId]">{{ m.firstName }} {{ m.lastName }}</a>
                </td>
              </ng-container>
              <ng-container matColumnDef="rating">
                <th mat-header-cell *matHeaderCellDef>Rating</th>
                <td mat-cell *matCellDef="let m"><app-star-rating [value]="m.averageRating" /></td>
              </ng-container>
              <ng-container matColumnDef="income">
                <th mat-header-cell *matHeaderCellDef>Income</th>
                <td mat-cell *matCellDef="let m">{{ m.totalIncome | currency }}</td>
              </ng-container>
              <ng-container matColumnDef="trips">
                <th mat-header-cell *matHeaderCellDef>Trips</th>
                <td mat-cell *matCellDef="let m">{{ m.totalTrips }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="managerCols"></tr>
              <tr mat-row *matRowDef="let row; columns: managerCols;"></tr>
            </table>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button routerLink="/admin/users">Manage Users</a>
          </mat-card-actions>
        </mat-card>

        <mat-card>
          <mat-card-header><mat-card-title>Top Travels</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="top-travels">
              @for (t of dash()!.topTravels.slice(0,4); track t.id) {
                <div class="top-travel-item">
                  <span class="travel-title"><a [routerLink]="['/travels', t.id]">{{ t.title }}</a></span>
                  <span class="travel-meta">{{ t.currentEnrollment }}/{{ t.capacity }}</span>
                </div>
              }
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <mat-card class="feedback-section">
        <mat-card-header><mat-card-title>Recent Feedback</mat-card-title></mat-card-header>
        <mat-card-content>
          @if (dash()!.recentFeedback.length === 0) {
            <p class="empty">No feedback yet.</p>
          }
          @for (f of dash()!.recentFeedback.slice(0,5); track f.id) {
            <div class="feedback-item">
              <div class="feedback-header">
                <app-star-rating [value]="f.rating" />
                <span class="feedback-meta">
                  {{ f.travelerDisplayName }} on
                  <a [routerLink]="['/travels', f.travelId]">{{ f.travelTitle }}</a>
                </span>
              </div>
              @if (f.comment) {
                <p class="feedback-comment">{{ f.comment }}</p>
              }
            </div>
          }
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 24px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .kpi { padding: 20px; text-align: center; }
    .kpi mat-icon { font-size: 32px; width: 32px; height: 32px; color: #4f46e5; margin-bottom: 8px; }
    .kpi-value { font-size: 24px; font-weight: 700; }
    .kpi-label { font-size: 13px; color: #64748b; }
    .kpi-action { font-size: 12px; color: #ef4444; margin-top: 4px; display: block; }
    .section-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    @media (max-width: 900px) { .section-grid { grid-template-columns: 1fr; } }
    .full-width { width: 100%; }
    .rank { display: inline-block; width: 20px; font-weight: 700; color: #94a3b8; }
    .top-travel-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
    .travel-meta { color: #64748b; font-size: 13px; }
    .feedback-section { margin-top: 24px; }
    .feedback-item { padding: 12px 0; border-bottom: 1px solid #f1f5f9; }
    .feedback-item:last-child { border-bottom: none; }
    .feedback-header { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; }
    .feedback-meta { font-size: 13px; color: #64748b; }
    .feedback-comment { margin: 4px 0 0; color: #334155; font-size: 14px; }
    .empty { color: #94a3b8; font-style: italic; }
  `],
})
export class AdminDashboardComponent implements OnInit {
  private analytics = inject(AnalyticsService);
  dash = signal<AdminDashboardResponse | null>(null);
  managerCols = ['name', 'rating', 'income', 'trips'];

  ngOnInit() { this.analytics.getAdminSummary().subscribe(d => this.dash.set(d)); }

  totalIncome() {
    return this.dash()?.incomeByMonth.reduce((s, m) => s + m.totalAmount, 0) ?? 0;
  }
}

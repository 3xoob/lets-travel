import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';
import { ManagerDashboardResponse } from '../../../core/models';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPipe, DecimalPipe,
    MatCardModule, MatButtonModule, MatIconModule, MatTableModule, StarRatingComponent],
  template: `
    <div class="dashboard-header">
      <h1>Manager Dashboard</h1>
      <a mat-flat-button color="primary" routerLink="/manager/travels/create">
        <mat-icon>add</mat-icon> New Travel
      </a>
    </div>

    @if (dash()) {
      <div class="kpi-grid">
        <mat-card class="kpi">
          <mat-icon>payments</mat-icon>
          <div class="kpi-value">{{ dash()!.currentMonthIncome | currency }}</div>
          <div class="kpi-label">Income this month</div>
          @if (dash()!.prevMonthIncome > 0) {
            <div class="kpi-sub">vs {{ dash()!.prevMonthIncome | currency }} last month</div>
          }
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>flight_takeoff</mat-icon>
          <div class="kpi-value">{{ dash()!.activeTravels }}</div>
          <div class="kpi-label">Active Travels</div>
          <div class="kpi-sub">{{ dash()!.totalTravels }} total</div>
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>group</mat-icon>
          <div class="kpi-value">{{ dash()!.totalSubscribers }}</div>
          <div class="kpi-label">Total Subscribers</div>
        </mat-card>
        <mat-card class="kpi">
          <mat-icon>star</mat-icon>
          <div class="kpi-value">{{ dash()!.averageRating | number:'1.1-1' }}</div>
          <div class="kpi-label">Average Rating</div>
          <app-star-rating [value]="dash()!.averageRating" [showValue]="false" />
        </mat-card>
      </div>

      <mat-card class="table-card">
        <mat-card-header><mat-card-title>My Travels</mat-card-title></mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="dash()!.travels" class="full-width">
            <ng-container matColumnDef="title">
              <th mat-header-cell *matHeaderCellDef>Title</th>
              <td mat-cell *matCellDef="let row">
                <a [routerLink]="['/travels', row.travel.id]">{{ row.travel.title }}</a>
              </td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let row"><span class="badge badge-info">{{ row.travel.status }}</span></td>
            </ng-container>
            <ng-container matColumnDef="subscribers">
              <th mat-header-cell *matHeaderCellDef>Subscribers</th>
              <td mat-cell *matCellDef="let row">{{ row.subscriberCount }}/{{ row.travel.capacity }}</td>
            </ng-container>
            <ng-container matColumnDef="income">
              <th mat-header-cell *matHeaderCellDef>Income</th>
              <td mat-cell *matCellDef="let row">{{ row.income | currency }}</td>
            </ng-container>
            <ng-container matColumnDef="rating">
              <th mat-header-cell *matHeaderCellDef>Rating</th>
              <td mat-cell *matCellDef="let row">
                <app-star-rating [value]="row.averageRating" />
              </td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let row">
                <a mat-icon-button [routerLink]="['/manager/travels', row.travel.id, 'edit']"><mat-icon>edit</mat-icon></a>
                <a mat-icon-button [routerLink]="['/manager/travels', row.travel.id, 'subscribers']"><mat-icon>people</mat-icon></a>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="cols"></tr>
            <tr mat-row *matRowDef="let row; columns: cols;"></tr>
          </table>
        </mat-card-content>
        <mat-card-actions>
          <a mat-button routerLink="/manager/travels">View all travels</a>
        </mat-card-actions>
      </mat-card>
    }
  `,
  styles: [`
    .dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .kpi { padding: 20px; text-align: center; }
    .kpi mat-icon { font-size: 32px; width: 32px; height: 32px; color: #4f46e5; margin-bottom: 8px; }
    .kpi-value { font-size: 24px; font-weight: 700; }
    .kpi-label { font-size: 13px; color: #64748b; }
    .kpi-sub { font-size: 12px; color: #94a3b8; }
    .table-card { margin-top: 16px; }
    .full-width { width: 100%; }
  `],
})
export class ManagerDashboardComponent implements OnInit {
  private analytics = inject(AnalyticsService);
  dash = signal<ManagerDashboardResponse | null>(null);
  cols = ['title', 'status', 'subscribers', 'income', 'rating', 'actions'];

  ngOnInit() { this.analytics.getManagerSummary().subscribe(d => this.dash.set(d)); }
}

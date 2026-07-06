import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ReportService } from '../../../core/services/report.service';
import { ReportResponse } from '../../../core/models';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatTableModule, MatSelectModule, MatFormFieldModule, MatPaginatorModule],
  template: `
    <div class="header">
      <h1>Reports</h1>
      <mat-form-field appearance="outline">
        <mat-label>Filter by status</mat-label>
        <mat-select [(ngModel)]="statusFilter" (ngModelChange)="load()">
          <mat-option value="">All</mat-option>
          <mat-option value="OPEN">Open</mat-option>
          <mat-option value="RESOLVED">Resolved</mat-option>
          <mat-option value="DISMISSED">Dismissed</mat-option>
        </mat-select>
      </mat-form-field>
    </div>

    <mat-card>
      <mat-card-content>
        <table mat-table [dataSource]="reports()" class="full-width">
          <ng-container matColumnDef="target">
            <th mat-header-cell *matHeaderCellDef>Target</th>
            <td mat-cell *matCellDef="let r">{{ r.targetType }}: {{ r.targetId | slice:0:8 }}…</td>
          </ng-container>
          <ng-container matColumnDef="reason">
            <th mat-header-cell *matHeaderCellDef>Reason</th>
            <td mat-cell *matCellDef="let r">{{ r.reason }}</td>
          </ng-container>
          <ng-container matColumnDef="reporter">
            <th mat-header-cell *matHeaderCellDef>Reporter</th>
            <td mat-cell *matCellDef="let r">{{ r.reporterEmail }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let r">
              <span class="badge" [class]="statusBadge(r.status)">{{ r.status }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Date</th>
            <td mat-cell *matCellDef="let r">{{ r.createdAt | date:'mediumDate' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let r">
              @if (r.status === 'OPEN') {
                <button mat-button color="primary" (click)="resolve(r, 'RESOLVED')">Resolve</button>
                <button mat-button (click)="resolve(r, 'DISMISSED')">Dismiss</button>
              }
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols;"></tr>
        </table>
        @if (reports().length === 0) {
          <div class="empty-state">
            <mat-icon>gavel</mat-icon>
            <h3>No reports</h3>
          </div>
        }
      </mat-card-content>
    </mat-card>
    <mat-paginator [length]="total()" [pageSize]="20" (page)="onPage($event)" />
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0; }
    .full-width { width: 100%; }
  `],
})
export class AdminReportsComponent implements OnInit {
  private reportService = inject(ReportService);
  private snack = inject(MatSnackBar);
  reports = signal<ReportResponse[]>([]);
  total = signal(0);
  statusFilter = '';
  page = 0;
  cols = ['target', 'reason', 'reporter', 'status', 'date', 'actions'];

  ngOnInit() { this.load(); }

  load() {
    this.reportService.getAll(this.statusFilter || undefined, this.page).subscribe(r => {
      this.reports.set(r.content);
      this.total.set(r.totalElements);
    });
  }

  onPage(e: PageEvent) { this.page = e.pageIndex; this.load(); }

  resolve(r: ReportResponse, resolution: string) {
    this.reportService.resolve(r.id, resolution).subscribe({
      next: () => { this.snack.open(`Report ${resolution}`, 'OK', { duration: 3000 }); this.load(); },
      error: () => this.snack.open('Failed', 'Close'),
    });
  }

  statusBadge(s: string) {
    const m: Record<string, string> = { OPEN: 'badge-warning', RESOLVED: 'badge-success', DISMISSED: 'badge-default' };
    return `badge ${m[s] ?? 'badge-default'}`;
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TravelService } from '../../../core/services/travel.service';
import { TravelSummary } from '../../../core/models';

@Component({
  selector: 'app-manager-travels',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatPaginatorModule],
  template: `
    <div class="header">
      <h1>My Travels</h1>
      <a mat-flat-button color="primary" routerLink="/manager/travels/create">
        <mat-icon>add</mat-icon> New Travel
      </a>
    </div>

    <mat-card>
      <mat-card-content>
        <table mat-table [dataSource]="travels()" class="full-width">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Title</th>
            <td mat-cell *matCellDef="let t"><a [routerLink]="['/travels', t.id]">{{ t.title }}</a></td>
          </ng-container>
          <ng-container matColumnDef="destination">
            <th mat-header-cell *matHeaderCellDef>Destination</th>
            <td mat-cell *matCellDef="let t">{{ t.destination }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let t"><span class="badge badge-info">{{ t.status }}</span></td>
          </ng-container>
          <ng-container matColumnDef="dates">
            <th mat-header-cell *matHeaderCellDef>Dates</th>
            <td mat-cell *matCellDef="let t">{{ t.startDate | date:'mediumDate' }}</td>
          </ng-container>
          <ng-container matColumnDef="enrollment">
            <th mat-header-cell *matHeaderCellDef>Enrollment</th>
            <td mat-cell *matCellDef="let t">{{ t.currentEnrollment }}/{{ t.capacity }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let t">
              <a mat-icon-button [routerLink]="['/manager/travels', t.id, 'edit']"><mat-icon>edit</mat-icon></a>
              <a mat-icon-button [routerLink]="['/manager/travels', t.id, 'subscribers']"><mat-icon>people</mat-icon></a>
              <button mat-icon-button color="warn" (click)="delete(t)"><mat-icon>delete</mat-icon></button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols;"></tr>
        </table>
        @if (travels().length === 0) {
          <div class="empty-state">
            <mat-icon>flight</mat-icon>
            <h3>No travels yet</h3>
            <p>Create your first travel to get started.</p>
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
export class ManagerTravelsComponent implements OnInit {
  private travelService = inject(TravelService);
  private snack = inject(MatSnackBar);
  travels = signal<TravelSummary[]>([]);
  total = signal(0);
  page = 0;
  cols = ['title', 'destination', 'status', 'dates', 'enrollment', 'actions'];

  ngOnInit() { this.load(); }

  load() {
    this.travelService.getMyTravels(this.page).subscribe(r => {
      this.travels.set(r.content);
      this.total.set(r.totalElements);
    });
  }

  onPage(e: PageEvent) { this.page = e.pageIndex; this.load(); }

  delete(t: TravelSummary) {
    if (!confirm(`Delete "${t.title}"?`)) return;
    this.travelService.delete(t.id).subscribe({
      next: () => { this.snack.open('Deleted', 'OK', { duration: 3000 }); this.load(); },
      error: err => this.snack.open(err.error?.message ?? 'Delete failed', 'Close'),
    });
  }
}

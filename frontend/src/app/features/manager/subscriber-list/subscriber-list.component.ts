import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { SubscriptionResponse } from '../../../core/models';

@Component({
  selector: 'app-subscriber-list',
  standalone: true,
  imports: [CommonModule, DatePipe, CurrencyPipe, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatDialogModule, MatPaginatorModule],
  template: `
    <h1>Subscribers</h1>
    <mat-card>
      <mat-card-content>
        <table mat-table [dataSource]="subscribers()" class="full-width">
          <ng-container matColumnDef="travel">
            <th mat-header-cell *matHeaderCellDef>Travel</th>
            <td mat-cell *matCellDef="let s">{{ s.travelTitle }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let s"><span class="badge badge-success">{{ s.status }}</span></td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Subscribed</th>
            <td mat-cell *matCellDef="let s">{{ s.subscribedAt | date:'mediumDate' }}</td>
          </ng-container>
          <ng-container matColumnDef="payment">
            <th mat-header-cell *matHeaderCellDef>Payment</th>
            <td mat-cell *matCellDef="let s">
              @if (s.payment) {
                {{ s.payment.amount | currency }} · {{ s.payment.method }}
              }
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let s">
              @if (s.status === 'ACTIVE') {
                <button mat-icon-button color="warn" (click)="unsubscribe(s)" title="Unsubscribe">
                  <mat-icon>person_remove</mat-icon>
                </button>
              }
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols;"></tr>
        </table>
        @if (subscribers().length === 0) {
          <div class="empty-state">
            <mat-icon>people_outline</mat-icon>
            <h3>No subscribers</h3>
          </div>
        }
      </mat-card-content>
    </mat-card>
    <mat-paginator [length]="total()" [pageSize]="20" (page)="onPage($event)" />
  `,
  styles: [`.full-width { width: 100%; }`],
})
export class SubscriberListComponent implements OnInit {
  @Input() travelId!: string;
  private subService = inject(SubscriptionService);
  private snack = inject(MatSnackBar);
  subscribers = signal<SubscriptionResponse[]>([]);
  total = signal(0);
  page = 0;
  cols = ['travel', 'status', 'date', 'payment', 'actions'];

  ngOnInit() { this.load(); }

  load() {
    this.subService.getTravelSubscribers(this.travelId, this.page).subscribe(r => {
      this.subscribers.set(r.content);
      this.total.set(r.totalElements);
    });
  }

  onPage(e: PageEvent) { this.page = e.pageIndex; this.load(); }

  unsubscribe(s: SubscriptionResponse) {
    const reason = prompt('Reason for unsubscribing this traveler:');
    if (!reason) return;
    this.subService.managerUnsubscribe(s.id, reason).subscribe({
      next: () => { this.snack.open('Unsubscribed', 'OK', { duration: 3000 }); this.load(); },
      error: err => this.snack.open(err.error?.message ?? 'Failed', 'Close'),
    });
  }
}

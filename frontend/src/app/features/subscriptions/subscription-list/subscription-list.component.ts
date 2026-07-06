import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { SubscriptionResponse } from '../../../core/models';

@Component({
  selector: 'app-subscription-list',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, CurrencyPipe, MatCardModule, MatButtonModule, MatIconModule, MatPaginatorModule],
  template: `
    <h1>My Trips</h1>
    @if (subscriptions().length === 0) {
      <div class="empty-state">
        <mat-icon>luggage</mat-icon>
        <h3>No subscriptions yet</h3>
        <p><a routerLink="/">Browse travels</a> to get started.</p>
      </div>
    } @else {
      <div class="sub-list">
        @for (s of subscriptions(); track s.id) {
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <a [routerLink]="['/travels', s.travelId]">{{ s.travelTitle }}</a>
              </mat-card-title>
              <mat-card-subtitle>
                {{ s.startDate | date:'mediumDate' }} — {{ s.endDate | date:'mediumDate' }}
              </mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="badges">
                <span class="badge" [class]="subBadge(s.status)">{{ s.status }}</span>
                @if (s.payment) {
                  <span class="badge" [class]="payBadge(s.payment.status)">
                    {{ s.payment.method }} · {{ s.payment.amount | currency }}
                  </span>
                }
              </div>
            </mat-card-content>
            <mat-card-actions>
              @if (s.status === 'ACTIVE') {
                <button mat-stroked-button color="warn" (click)="unsubscribe(s)">Cancel</button>
              }
            </mat-card-actions>
          </mat-card>
        }
      </div>
      <mat-paginator [length]="total()" [pageSize]="10" (page)="onPage($event)" />
    }
  `,
  styles: [`
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 24px; }
    .sub-list { display: flex; flex-direction: column; gap: 12px; }
    .badges { display: flex; gap: 8px; flex-wrap: wrap; }
  `],
})
export class SubscriptionListComponent implements OnInit {
  private subs = inject(SubscriptionService);
  private snack = inject(MatSnackBar);
  subscriptions = signal<SubscriptionResponse[]>([]);
  total = signal(0);
  page = 0;

  ngOnInit() { this.load(); }

  load() {
    this.subs.getMySubscriptions(this.page).subscribe(r => {
      this.subscriptions.set(r.content);
      this.total.set(r.totalElements);
    });
  }

  onPage(e: PageEvent) { this.page = e.pageIndex; this.load(); }

  unsubscribe(s: SubscriptionResponse) {
    this.subs.unsubscribe(s.id).subscribe({
      next: () => { this.snack.open('Cancelled', 'OK', { duration: 3000 }); this.load(); },
      error: err => this.snack.open(err.error?.message ?? 'Failed', 'Close'),
    });
  }

  subBadge(s: string) {
    const m: Record<string, string> = { ACTIVE: 'badge-success', PENDING: 'badge-warning', CANCELLED: 'badge-error', EXPIRED: 'badge-default' };
    return `badge ${m[s] ?? 'badge-default'}`;
  }

  payBadge(s: string) {
    const m: Record<string, string> = { COMPLETED: 'badge-success', PENDING: 'badge-warning', FAILED: 'badge-error', REFUNDED: 'badge-info' };
    return `badge ${m[s] ?? 'badge-default'}`;
  }
}

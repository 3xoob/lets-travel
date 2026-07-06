import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { TravelSummary } from '../../../core/models';

@Component({
  selector: 'app-travel-card',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatChipsModule, MatIconModule, CurrencyPipe, DatePipe],
  template: `
    <mat-card class="travel-card">
      <img mat-card-image [src]="travel.thumbnailUrl || '/assets/placeholder-travel.jpg'" [alt]="travel.title">
      <mat-card-header>
        <mat-card-title>{{ travel.title }}</mat-card-title>
        <mat-card-subtitle>
          <mat-icon inline>place</mat-icon> {{ travel.destination }}
        </mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <div class="meta-row">
          <mat-chip highlighted color="primary">{{ travel.category | titlecase }}</mat-chip>
          <span class="price">{{ travel.price | currency }}</span>
        </div>
        <div class="dates">
          <mat-icon inline>calendar_today</mat-icon>
          {{ travel.startDate | date:'mediumDate' }} — {{ travel.endDate | date:'mediumDate' }}
        </div>
        <div class="capacity">
          <mat-icon inline>group</mat-icon>
          {{ travel.currentEnrollment }} / {{ travel.capacity }} enrolled
        </div>
        <div class="manager">by {{ travel.managerName }}</div>
      </mat-card-content>
      <mat-card-actions>
        <a mat-stroked-button color="primary" [routerLink]="['/travels', travel.id]">View Details</a>
        @if (showBook) {
          <button mat-flat-button color="accent" (click)="book.emit(travel)">Book Now</button>
        }
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .travel-card { height: 100%; display: flex; flex-direction: column; }
    img[mat-card-image] { height: 180px; object-fit: cover; }
    mat-card-content { flex: 1; }
    .meta-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .price { font-size: 20px; font-weight: 700; color: #4f46e5; }
    .dates, .capacity, .manager { font-size: 13px; color: #64748b; margin-top: 4px; display: flex; align-items: center; gap: 4px; }
    mat-card-actions { margin-top: auto; }
  `],
})
export class TravelCardComponent {
  @Input({ required: true }) travel!: TravelSummary;
  @Input() showBook = false;
  @Output() book = new EventEmitter<TravelSummary>();
}

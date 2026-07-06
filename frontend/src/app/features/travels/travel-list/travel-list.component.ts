import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { TravelCardComponent } from '../../../shared/components/travel-card/travel-card.component';
import { LoadingSkeletonComponent } from '../../../shared/components/loading-skeleton/loading-skeleton.component';
import { TravelService } from '../../../core/services/travel.service';
import { TravelSummary, TravelStatus } from '../../../core/models';

@Component({
  selector: 'app-travel-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatSelectModule, MatFormFieldModule,
    MatButtonModule, MatPaginatorModule, MatIconModule,
    TravelCardComponent, LoadingSkeletonComponent,
  ],
  template: `
    <div class="list-header">
      <h1>Discover Travels</h1>
      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="selectedStatus" (ngModelChange)="onStatusChange()">
          <mat-option value="">All</mat-option>
          <mat-option value="PUBLISHED">Published</mat-option>
          <mat-option value="COMPLETED">Completed</mat-option>
        </mat-select>
      </mat-form-field>
    </div>

    @if (loading()) {
      <app-loading-skeleton [count]="pageSize" />
    } @else if (travels().length === 0) {
      <div class="empty-state">
        <mat-icon>flight_off</mat-icon>
        <h3>No travels found</h3>
        <p>Try a different filter.</p>
      </div>
    } @else {
      <div class="travel-grid">
        @for (t of travels(); track t.id) {
          <app-travel-card [travel]="t" />
        }
      </div>
      <mat-paginator
        [length]="totalElements()"
        [pageSize]="pageSize"
        [pageIndex]="page()"
        (page)="onPage($event)"
        [pageSizeOptions]="[6,12,24]">
      </mat-paginator>
    }
  `,
  styles: [`
    .list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0; }
    .travel-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; margin-bottom: 24px; }
  `],
})
export class TravelListComponent implements OnInit {
  private travelService = inject(TravelService);

  travels = signal<TravelSummary[]>([]);
  loading = signal(true);
  page = signal(0);
  totalElements = signal(0);
  pageSize = 12;
  selectedStatus = '';

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.travelService.getAll({
      status: this.selectedStatus || undefined,
      page: this.page(),
      size: this.pageSize,
    }).subscribe({
      next: res => {
        this.travels.set(res.content);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onStatusChange() { this.page.set(0); this.load(); }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.pageSize = e.pageSize; this.load(); }
}

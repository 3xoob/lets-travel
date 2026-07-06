import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { RecommendationService } from '../../core/services/recommendation.service';
import { TravelCardComponent } from '../../shared/components/travel-card/travel-card.component';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { TravelSummary } from '../../core/models';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatTabsModule, TravelCardComponent, LoadingSkeletonComponent],
  template: `
    <h1>Recommended for You</h1>
    <mat-tab-group>
      <mat-tab label="Personalized">
        @if (loading()) {
          <app-loading-skeleton [count]="6" />
        } @else if (personal().length === 0) {
          <div class="empty-state">
            <mat-icon>explore</mat-icon>
            <h3>No recommendations yet</h3>
            <p>Subscribe to some travels and come back!</p>
          </div>
        } @else {
          <div class="grid">
            @for (t of personal(); track t.id) {
              <app-travel-card [travel]="t" />
            }
          </div>
        }
      </mat-tab>
      <mat-tab label="Trending">
        @if (trending().length === 0) {
          <app-loading-skeleton [count]="6" />
        } @else {
          <div class="grid">
            @for (t of trending(); track t.id) {
              <app-travel-card [travel]="t" />
            }
          </div>
        }
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [`
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 24px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; padding-top: 16px; }
  `],
})
export class RecommendationsComponent implements OnInit {
  private reco = inject(RecommendationService);
  personal = signal<TravelSummary[]>([]);
  trending = signal<TravelSummary[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.reco.getForMe().subscribe({ next: r => { this.personal.set(r); this.loading.set(false); }, error: () => this.loading.set(false) });
    this.reco.getTrending().subscribe(r => this.trending.set(r));
  }
}

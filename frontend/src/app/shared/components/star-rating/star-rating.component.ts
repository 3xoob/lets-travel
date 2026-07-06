import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <span class="stars">
      @for (s of stars; track s) {
        <mat-icon class="star" [class.full]="s <= full" [class.half]="!isFullStar(s) && hasHalf(s)">
          {{ getIcon(s) }}
        </mat-icon>
      }
      @if (showValue) {
        <span class="value">({{ value | number:'1.1-1' }})</span>
      }
    </span>
  `,
  styles: [`
    .stars { display: inline-flex; align-items: center; gap: 1px; }
    .star { font-size: 18px; width: 18px; height: 18px; color: #d1d5db; }
    .star.full { color: #f59e0b; }
    .star.half { color: #f59e0b; }
    .value { font-size: 13px; color: #64748b; margin-left: 4px; }
  `],
})
export class StarRatingComponent {
  @Input() value = 0;
  @Input() showValue = true;
  stars = [1, 2, 3, 4, 5];
  get full() { return Math.floor(this.value); }
  isFullStar(s: number) { return s <= this.full; }
  hasHalf(s: number) { return s === this.full + 1 && this.value % 1 >= 0.5; }
  getIcon(s: number) {
    if (this.isFullStar(s)) return 'star';
    if (this.hasHalf(s)) return 'star_half';
    return 'star_border';
  }
}

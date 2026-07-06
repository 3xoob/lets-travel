import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { SearchService } from '../../core/services/search.service';
import { TravelCardComponent } from '../../shared/components/travel-card/travel-card.component';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { TravelSummary } from '../../core/models';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatAutocompleteModule, MatPaginatorModule, MatIconModule, MatExpansionModule,
    TravelCardComponent, LoadingSkeletonComponent,
  ],
  template: `
    <div class="search-header">
      <h1>Search Travels</h1>
      <form [formGroup]="form" (ngSubmit)="search()" class="search-form">
        <mat-form-field appearance="outline" class="search-input">
          <mat-label>Search destinations, activities...</mat-label>
          <input matInput formControlName="query" [matAutocomplete]="auto">
          <mat-icon matSuffix>search</mat-icon>
          <mat-autocomplete #auto="matAutocomplete">
            @for (opt of suggestions(); track opt.id) {
              <mat-option [value]="opt.title" (onSelectionChange)="selectSuggestion(opt)">
                {{ opt.title }} — {{ opt.destination }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
        <button mat-flat-button color="primary" type="submit">Search</button>
      </form>

      <mat-expansion-panel class="filters-panel">
        <mat-expansion-panel-header>
          <mat-panel-title>Filters</mat-panel-title>
        </mat-expansion-panel-header>
        <div class="filters-grid">
          <mat-form-field appearance="outline">
            <mat-label>Category</mat-label>
            <mat-select formControlName="category" [formGroup]="form">
              <mat-option value="">Any</mat-option>
              @for (cat of categories; track cat) {
                <mat-option [value]="cat">{{ cat | titlecase }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Min Price</mat-label>
            <input matInput type="number" formControlName="minPrice" [formGroup]="form">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Max Price</mat-label>
            <input matInput type="number" formControlName="maxPrice" [formGroup]="form">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>From Date</mat-label>
            <input matInput type="date" formControlName="startDateFrom" [formGroup]="form">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>To Date</mat-label>
            <input matInput type="date" formControlName="startDateTo" [formGroup]="form">
          </mat-form-field>
        </div>
        <button mat-button (click)="clearFilters()">Clear Filters</button>
      </mat-expansion-panel>
    </div>

    @if (loading()) {
      <app-loading-skeleton [count]="12" />
    } @else if (results().length === 0 && searched()) {
      <div class="empty-state">
        <mat-icon>search_off</mat-icon>
        <h3>No results found</h3>
        <p>Try different keywords or adjust filters.</p>
      </div>
    } @else {
      <p class="result-count">{{ totalElements() }} travel{{ totalElements() !== 1 ? 's' : '' }} found</p>
      <div class="travel-grid">
        @for (t of results(); track t.id) {
          <app-travel-card [travel]="t" />
        }
      </div>
      @if (totalElements() > 12) {
        <mat-paginator
          [length]="totalElements()"
          [pageSize]="12"
          [pageIndex]="page()"
          (page)="onPage($event)">
        </mat-paginator>
      }
    }
  `,
  styles: [`
    .search-header { margin-bottom: 24px; }
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 16px; }
    .search-form { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 12px; }
    .search-input { flex: 1; }
    .filters-panel { margin-bottom: 16px; }
    .filters-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
    .result-count { color: #64748b; font-size: 14px; margin-bottom: 16px; }
    .travel-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; margin-bottom: 24px; }
  `],
})
export class SearchComponent implements OnInit {
  private searchService = inject(SearchService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  form = this.fb.group({
    query: [''],
    category: [''],
    minPrice: [null as number | null],
    maxPrice: [null as number | null],
    startDateFrom: [''],
    startDateTo: [''],
  });

  categories = ['ADVENTURE', 'CULTURAL', 'RELAXATION', 'BUSINESS', 'SPORT', 'NATURE', 'CITY_BREAK', 'CRUISE'];
  results = signal<TravelSummary[]>([]);
  suggestions = signal<TravelSummary[]>([]);
  loading = signal(false);
  searched = signal(false);
  totalElements = signal(0);
  page = signal(0);

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.form.patchValue({ query: params['q'] });
        this.search();
      }
    });

    this.form.get('query')!.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => q && q.length >= 2 ? this.searchService.autocomplete(q) : []),
    ).subscribe(s => this.suggestions.set(s));
  }

  search() {
    const v = this.form.value;
    this.loading.set(true);
    this.searched.set(true);
    this.router.navigate([], {
      queryParams: { q: v.query || null },
      queryParamsHandling: 'merge',
    });
    this.searchService.search({
      query: v.query || undefined,
      category: (v.category || undefined) as any,
      minPrice: v.minPrice ?? undefined,
      maxPrice: v.maxPrice ?? undefined,
      startDateFrom: v.startDateFrom || undefined,
      startDateTo: v.startDateTo || undefined,
      page: this.page(),
      size: 12,
    }).subscribe({
      next: r => {
        this.results.set(r.content);
        this.totalElements.set(r.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  selectSuggestion(t: TravelSummary) {
    this.form.patchValue({ query: t.title });
    this.search();
  }

  clearFilters() {
    this.form.reset();
    this.results.set([]);
    this.searched.set(false);
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.search(); }
}

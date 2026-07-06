import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { TravelSummary, PageResponse, SearchFilters } from '../models';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private http = inject(HttpClient);

  search(filters: SearchFilters): Observable<PageResponse<TravelSummary>> {
    let params = new HttpParams();
    if (filters.query) params = params.set('q', filters.query);
    if (filters.category) params = params.set('category', filters.category);
    if (filters.destination) params = params.set('destination', filters.destination);
    if (filters.minPrice !== undefined) params = params.set('minPrice', filters.minPrice);
    if (filters.maxPrice !== undefined) params = params.set('maxPrice', filters.maxPrice);
    if (filters.startDateFrom) params = params.set('startDateFrom', filters.startDateFrom);
    if (filters.startDateTo) params = params.set('startDateTo', filters.startDateTo);
    if (filters.page !== undefined) params = params.set('page', filters.page);
    if (filters.size !== undefined) params = params.set('size', filters.size);
    return this.http.get<PageResponse<TravelSummary>>(
      `${environment.apiUrl}/search/travels`, { params }
    );
  }

  autocomplete(q: string): Observable<TravelSummary[]> {
    return this.http.get<TravelSummary[]>(
      `${environment.apiUrl}/search/autocomplete`,
      { params: new HttpParams().set('q', q) }
    );
  }
}

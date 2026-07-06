import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { TravelResponse, TravelSummary, PageResponse, SearchFilters } from '../models';

@Injectable({ providedIn: 'root' })
export class TravelService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/travels`;

  getAll(filters?: { status?: string; page?: number; size?: number }): Observable<PageResponse<TravelSummary>> {
    let params = new HttpParams();
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.page !== undefined) params = params.set('page', filters.page);
    if (filters?.size !== undefined) params = params.set('size', filters.size ?? 12);
    return this.http.get<PageResponse<TravelSummary>>(this.base, { params });
  }

  getById(id: string): Observable<TravelResponse> {
    return this.http.get<TravelResponse>(`${this.base}/${id}`);
  }

  create(data: Partial<TravelResponse>): Observable<TravelResponse> {
    return this.http.post<TravelResponse>(this.base, data);
  }

  update(id: string, data: Partial<TravelResponse>): Observable<TravelResponse> {
    return this.http.put<TravelResponse>(`${this.base}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  uploadImages(id: string, files: File[]): Observable<TravelResponse> {
    const form = new FormData();
    files.forEach(f => form.append('images', f));
    return this.http.post<TravelResponse>(`${this.base}/${id}/images`, form);
  }

  getMyTravels(page = 0, size = 20): Observable<PageResponse<TravelSummary>> {
    return this.http.get<PageResponse<TravelSummary>>(
      `${environment.apiUrl}/manager/travels`,
      { params: new HttpParams().set('page', page).set('size', size) }
    );
  }
}

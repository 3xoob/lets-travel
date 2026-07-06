import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ReportResponse, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);

  submit(data: { targetType: string; targetId: string; reason: string; description?: string }): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${environment.apiUrl}/reports`, data);
  }

  getAll(status?: string, page = 0): Observable<PageResponse<ReportResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<ReportResponse>>(
      `${environment.apiUrl}/admin/reports`, { params }
    );
  }

  resolve(id: string, resolution: string): Observable<ReportResponse> {
    return this.http.patch<ReportResponse>(`${environment.apiUrl}/admin/reports/${id}`, { resolution });
  }
}

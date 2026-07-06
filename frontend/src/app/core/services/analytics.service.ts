import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { AdminDashboardResponse, ManagerDashboardResponse, IncomeByMonthDto } from '../models';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);

  getAdminSummary(months = 6): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(
      `${environment.apiUrl}/admin/analytics/summary`,
      { params: new HttpParams().set('months', months) }
    );
  }

  getManagerSummary(): Observable<ManagerDashboardResponse> {
    return this.http.get<ManagerDashboardResponse>(`${environment.apiUrl}/manager/analytics/summary`);
  }

  getManagerIncome(months = 6): Observable<IncomeByMonthDto[]> {
    return this.http.get<IncomeByMonthDto[]>(
      `${environment.apiUrl}/manager/analytics/income`,
      { params: new HttpParams().set('months', months) }
    );
  }
}

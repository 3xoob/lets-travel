import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { UserDto, ManagerProfileResponse, TravelerStatsResponse, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getMe(): Observable<UserDto> {
    return this.http.get<UserDto>(`${environment.apiUrl}/users/me`);
  }

  updateProfile(data: { firstName?: string; lastName?: string; bio?: string }): Observable<UserDto> {
    return this.http.patch<UserDto>(`${environment.apiUrl}/users/me`, data);
  }

  getManagerProfile(id: string): Observable<ManagerProfileResponse> {
    return this.http.get<ManagerProfileResponse>(`${environment.apiUrl}/managers/${id}/profile`);
  }

  getMyStats(): Observable<TravelerStatsResponse> {
    return this.http.get<TravelerStatsResponse>(`${environment.apiUrl}/travelers/me/stats`);
  }

  getAllUsers(page = 0): Observable<PageResponse<UserDto>> {
    return this.http.get<PageResponse<UserDto>>(
      `${environment.apiUrl}/admin/users`,
      { params: new HttpParams().set('page', page) }
    );
  }

  changeRole(userId: string, role: string): Observable<UserDto> {
    return this.http.patch<UserDto>(`${environment.apiUrl}/admin/users/${userId}/role`, { role });
  }
}

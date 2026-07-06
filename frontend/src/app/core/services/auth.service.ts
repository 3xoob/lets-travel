import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { AuthResponse, UserDto } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private currentUserSubject = new BehaviorSubject<UserDto | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();

  private loadUser(): UserDto | null {
    const json = localStorage.getItem('user');
    return json ? JSON.parse(json) : null;
  }

  get currentUser(): UserDto | null {
    return this.currentUserSubject.value;
  }

  get accessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  get isLoggedIn(): boolean {
    return !!this.accessToken;
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  get isManager(): boolean {
    return this.currentUser?.role === 'MANAGER' || this.isAdmin;
  }

  register(data: { email: string; password: string; firstName: string; lastName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, data)
      .pipe(tap(res => this.storeAuth(res)));
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(tap(res => this.storeAuth(res)));
  }

  refreshToken(): Observable<AuthResponse> {
    const email = this.currentUser?.email;
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http.post<AuthResponse>(
      `${environment.apiUrl}/auth/refresh?email=${email}`,
      { refreshToken }
    ).pipe(tap(res => this.storeAuth(res)));
  }

  logout(): void {
    this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe();
    localStorage.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private storeAuth(res: AuthResponse): void {
    localStorage.setItem('accessToken', res.accessToken);
    localStorage.setItem('refreshToken', res.refreshToken);
    localStorage.setItem('user', JSON.stringify(res.user));
    this.currentUserSubject.next(res.user);
  }
}

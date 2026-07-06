import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { PwaService } from './core/services/pwa.service';
import { LanguageService } from './core/services/language.service';
import { LanguageSelectorComponent } from './shared/components/language-selector/language-selector.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatButtonModule, MatMenuModule, MatIconModule, MatDividerModule,
    LanguageSelectorComponent,
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <a routerLink="/" class="brand">
        <mat-icon>flight_takeoff</mat-icon>
        <span>Let's Travel</span>
      </a>
      <span class="spacer"></span>
      <nav class="nav-links">
        <a mat-button routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">Travels</a>
        <a mat-button routerLink="/search">Search</a>
        @if (auth.isLoggedIn) {
          <a mat-button routerLink="/recommendations">Recommendations</a>
        }
        @if (auth.isManager) {
          <a mat-button routerLink="/manager/dashboard">Manager</a>
        }
        @if (auth.isAdmin) {
          <a mat-button routerLink="/admin/dashboard">Admin</a>
        }
      </nav>
      <app-language-selector />
      @if (auth.isLoggedIn) {
        <button mat-icon-button [matMenuTriggerFor]="userMenu">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #userMenu="matMenu">
          <div class="user-menu-header">{{ auth.currentUser?.email }}</div>
          <mat-divider></mat-divider>
          <a mat-menu-item routerLink="/profile"><mat-icon>person</mat-icon>Profile</a>
          <a mat-menu-item routerLink="/subscriptions"><mat-icon>bookmark</mat-icon>My Trips</a>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="auth.logout()"><mat-icon>logout</mat-icon>Logout</button>
        </mat-menu>
      } @else {
        <a mat-button routerLink="/login">Login</a>
        <a mat-raised-button color="accent" routerLink="/register">Sign Up</a>
      }
    </mat-toolbar>
    <main class="container page-content">
      <router-outlet />
    </main>
  `,
  styles: [`
    .app-toolbar { position: sticky; top: 0; z-index: 100; }
    .brand { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: 700; color: white; text-decoration: none; }
    .spacer { flex: 1; }
    .nav-links { display: flex; gap: 4px; margin-right: 16px; }
    .nav-links a.active { background: rgba(255,255,255,0.15); border-radius: 4px; }
    .user-menu-header { padding: 8px 16px; font-size: 12px; color: #64748b; }
  `],
})
export class AppComponent implements OnInit {
  auth = inject(AuthService);
  private pwaService = inject(PwaService);
  private langService = inject(LanguageService);

  ngOnInit(): void {
    this.pwaService.init();
    this.langService.applyDocumentDir();
  }
}

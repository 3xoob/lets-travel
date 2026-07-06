import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { LanguageService, LANGUAGES } from '../../../core/services/language.service';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatMenuModule, MatIconModule],
  template: `
    <button mat-button [matMenuTriggerFor]="langMenu" class="lang-btn">
      <mat-icon>language</mat-icon>
      {{ currentLabel() }}
    </button>
    <mat-menu #langMenu="matMenu">
      @for (lang of languages; track lang.code) {
        <button mat-menu-item (click)="select(lang.code)" [class.active]="lang.code === langService.current()">
          <span class="lang-flag">{{ langFlag(lang.code) }}</span>
          {{ lang.label }}
        </button>
      }
    </mat-menu>
  `,
  styles: [`
    .lang-btn { color: white; }
    .active { font-weight: 600; }
    .lang-flag { margin-right: 8px; }
  `],
})
export class LanguageSelectorComponent {
  langService = inject(LanguageService);
  languages = LANGUAGES;

  currentLabel() {
    return LANGUAGES.find(l => l.code === this.langService.current())?.label ?? 'EN';
  }

  langFlag(code: string): string {
    const flags: Record<string, string> = { en: '🇬🇧', fr: '🇫🇷', ar: '🇸🇦' };
    return flags[code] ?? '🌐';
  }

  select(code: any): void {
    this.langService.setLanguage(code);
  }
}

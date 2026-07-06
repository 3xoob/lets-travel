import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from './language.service';

@Injectable({ providedIn: 'root' })
export class TranslateService {
  private http = inject(HttpClient);
  private langService = inject(LanguageService);
  private translations = signal<Record<string, string>>({});

  load(): Promise<void> {
    const lang = this.langService.current();
    return new Promise((resolve) => {
      this.http.get<{ translations: Record<string, string> }>(`/assets/locale/messages.${lang}.json`)
        .subscribe({
          next: data => { this.translations.set(data.translations); resolve(); },
          error: () => resolve(),
        });
    });
  }

  t(key: string, fallback?: string): string {
    return this.translations()[key] ?? fallback ?? key;
  }
}

import { Injectable, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { inject } from '@angular/core';

export type SupportedLang = 'en' | 'fr' | 'ar';

export const LANGUAGES: { code: SupportedLang; label: string; dir: 'ltr' | 'rtl' }[] = [
  { code: 'en', label: 'English', dir: 'ltr' },
  { code: 'fr', label: 'Français', dir: 'ltr' },
  { code: 'ar', label: 'العربية', dir: 'rtl' },
];

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private document = inject(DOCUMENT);

  private _current = signal<SupportedLang>(this.loadSaved());
  current = this._current.asReadonly();

  private loadSaved(): SupportedLang {
    return (localStorage.getItem('lang') as SupportedLang) ?? 'en';
  }

  setLanguage(lang: SupportedLang): void {
    localStorage.setItem('lang', lang);
    this._current.set(lang);
    const entry = LANGUAGES.find(l => l.code === lang)!;
    this.document.documentElement.lang = lang;
    this.document.documentElement.dir = entry.dir;
    // Full reload to apply locale bundle
    const url = new URL(window.location.href);
    url.pathname = `/${lang}${url.pathname.replace(/^\/(en|fr|ar)/, '')}`;
    window.location.href = url.toString();
  }

  applyDocumentDir(): void {
    const lang = this._current();
    const entry = LANGUAGES.find(l => l.code === lang) ?? LANGUAGES[0];
    this.document.documentElement.lang = lang;
    this.document.documentElement.dir = entry.dir;
  }
}

import { Injectable, signal } from '@angular/core';

/** Keep these three in sync with the inline FOUC-prevention script in index.html. */
export const THEME_STORAGE_KEY = 'ff-theme';
export const THEME_DARK_CLASS = 'app-dark';

export type Theme = 'light' | 'dark';

/**
 * Class-based dark mode (`.app-dark` on <html>), backed by a signal.
 * Defaults to the OS preference until the user picks explicitly, at which
 * point that choice is persisted and no longer overridden by the OS.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.readInitialTheme());

  constructor() {
    this.apply(this.theme());
  }

  toggle(): void {
    this.set(this.theme() === 'dark' ? 'light' : 'dark');
  }

  set(theme: Theme): void {
    this.theme.set(theme);
    localStorage.setItem(THEME_STORAGE_KEY, theme);
    this.apply(theme);
  }

  private apply(theme: Theme): void {
    document.documentElement.classList.toggle(THEME_DARK_CLASS, theme === 'dark');
  }

  private readInitialTheme(): Theme {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}

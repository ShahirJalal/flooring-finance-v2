import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'ff-theme';
/** Must match app.config.ts's providePrimeNG({ theme: { options: { darkModeSelector } } }). */
const DARK_CLASS = 'app-dark';

/**
 * Applies the class synchronously (both on construction and on every
 * change) rather than through an effect(), so there's no scheduling delay
 * that could show the wrong theme for a frame.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly modeSignal = signal<ThemeMode>(this.readInitialMode());
  readonly mode = this.modeSignal.asReadonly();

  constructor() {
    this.applyMode(this.modeSignal());
  }

  toggle(): void {
    this.setMode(this.modeSignal() === 'dark' ? 'light' : 'dark');
  }

  setMode(mode: ThemeMode): void {
    this.modeSignal.set(mode);
    this.applyMode(mode);
  }

  private readInitialMode(): ThemeMode {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private applyMode(mode: ThemeMode): void {
    document.documentElement.classList.toggle(DARK_CLASS, mode === 'dark');
    localStorage.setItem(STORAGE_KEY, mode);
  }
}

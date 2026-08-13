import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  fullName?: string | null;
}

/**
 * The JWT itself lives in an httpOnly cookie set by the backend - this
 * service never sees or stores the token, only the logged-in user's info.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<CurrentUser | null>(null);
  private checkedOnce = false;

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor(private readonly http: HttpClient) {}

  login(username: string, password: string): Observable<CurrentUser> {
    return this.http
      .post<CurrentUser>('/api/auth/login', { username, password }, { withCredentials: true })
      .pipe(tap(user => this.currentUserSignal.set(user)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>('/api/auth/logout', {}, { withCredentials: true })
      .pipe(tap(() => this.currentUserSignal.set(null)));
  }

  /** Called once on app startup to restore session state from the auth cookie, if any. */
  restoreSession(): Observable<CurrentUser> {
    this.checkedOnce = true;
    return this.http
      .get<CurrentUser>('/api/auth/me', { withCredentials: true })
      .pipe(tap(user => this.currentUserSignal.set(user)));
  }

  updateProfile(email: string, fullName: string | null): Observable<CurrentUser> {
    return this.http
      .put<CurrentUser>('/api/auth/profile', { email, fullName }, { withCredentials: true })
      .pipe(tap(user => this.currentUserSignal.set(user)));
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>('/api/auth/password', { currentPassword, newPassword }, { withCredentials: true });
  }

  hasCheckedSession(): boolean {
    return this.checkedOnce;
  }

  clearSession(): void {
    this.currentUserSignal.set(null);
  }
}

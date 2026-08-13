import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Protects every route except /login. On first navigation after a page
 * load there's no in-memory user yet, so it asks the backend (via the
 * auth cookie) whether a session is still valid before deciding.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  if (authService.hasCheckedSession()) {
    return router.createUrlTree(['/login']);
  }

  return authService.restoreSession().pipe(
    map(() => true),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};

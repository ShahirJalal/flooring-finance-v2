import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Central place to surface API failures as toasts, and to bounce back to
 * the login page on an expired/invalid session - see GlobalExceptionHandler
 * on the backend for the ApiError shape this reads.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/login')) {
        authService.clearSession();
        router.navigate(['/login']);
      } else if (error.status !== 401) {
        const message = error.error?.message || 'Something went wrong. Please try again.';
        messageService.add({ severity: 'error', summary: 'Error', detail: message, life: 5000 });
      }
      return throwError(() => error);
    })
  );
};

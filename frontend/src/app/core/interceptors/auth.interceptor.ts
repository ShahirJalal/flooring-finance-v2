import { HttpInterceptorFn } from '@angular/common/http';

/** Ensures the httpOnly auth cookie is sent with every API request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('/api')) {
    return next(req);
  }
  return next(req.clone({ withCredentials: true }));
};

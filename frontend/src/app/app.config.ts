import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { THEME_DARK_CLASS, ThemeService } from './core/theme/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: `.${THEME_DARK_CLASS}`,
        },
      },
    }),
    MessageService,
    ConfirmationService,
    // Instantiate eagerly so the theme signal (and its effect on <html>) is
    // live before the first component renders, in sync with the inline
    // FOUC-prevention script in index.html.
    { provide: APP_INITIALIZER, useFactory: (theme: ThemeService) => () => theme, deps: [ThemeService], multi: true },
  ],
};

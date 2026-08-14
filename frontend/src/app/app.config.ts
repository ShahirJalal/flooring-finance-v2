import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

/**
 * Flat, lined look (studied off Cloudflare's dashboard) instead of Aura's
 * default elevated-card style: hairline borders do the separating, not
 * drop shadows, and corners stay tight rather than heavily rounded.
 */
const FlatPreset = definePreset(Aura, {
  components: {
    card: {
      root: {
        borderRadius: '{border.radius.md}',
        shadow: 'none',
      },
    },
    dialog: {
      root: {
        borderRadius: '{border.radius.md}',
        shadow: '0 1px 2px rgba(0, 0, 0, 0.06), 0 8px 24px rgba(0, 0, 0, 0.08)',
      },
    },
    button: {
      root: {
        borderRadius: '{border.radius.sm}',
      },
    },
  },
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: FlatPreset,
        options: {
          // Must match ThemeService's DARK_CLASS and the inline script in index.html.
          darkModeSelector: '.app-dark',
        },
      },
    }),
    MessageService,
    ConfirmationService,
  ],
};

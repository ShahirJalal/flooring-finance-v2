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
  semantic: {
    // Cloudflare's dashboard uses blue for every interactive/actionable
    // element (links, buttons, active nav, focus rings) - swapped in place
    // of Aura's default emerald. Profit red/green stays untouched below;
    // that's a financial in/out signal, not the app's brand color.
    primary: {
      50: '{blue.50}',
      100: '{blue.100}',
      200: '{blue.200}',
      300: '{blue.300}',
      400: '{blue.400}',
      500: '{blue.500}',
      600: '{blue.600}',
      700: '{blue.700}',
      800: '{blue.800}',
      900: '{blue.900}',
      950: '{blue.950}',
    },
  },
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

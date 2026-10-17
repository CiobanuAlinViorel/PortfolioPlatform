import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withInMemoryScrolling, withViewTransitions } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { slowRequestInterceptor } from '../shared/services/slow-request.interceptor';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { catchError, firstValueFrom, of } from 'rxjs';

import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { AuthService } from '../features/auth/register/services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }),
      withViewTransitions()
    ),
    provideClientHydration(),
    provideHttpClient(withFetch(), withInterceptors([slowRequestInterceptor])),
    provideAnimationsAsync(),
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return firstValueFrom(authService.refresh().pipe(catchError(() => of(null))));
    })
  ]
};

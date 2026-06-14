import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Protege rutas del portal del cliente. Si hay token pero el perfil aún no se
 * ha cargado, lo resuelve antes de decidir. Sin sesión → redirige a login con
 * returnUrl.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const deny = () =>
    router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });

  if (auth.isAuthenticated()) return true;
  if (!auth.hasToken()) return deny();

  return auth.loadCurrentUser().pipe(
    map(() => true),
    catchError(() => of(deny())),
  );
};

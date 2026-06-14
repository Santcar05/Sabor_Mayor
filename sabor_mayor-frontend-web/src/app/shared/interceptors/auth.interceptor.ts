import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { TokenStorageService } from '../services/token-storage.service';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

/** Rutas de auth que NO deben llevar token ni intentar refresh. */
const AUTH_FREE = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh', '/api/auth/oauth2'];

function isApiRequest(url: string): boolean {
  return url.startsWith(environment.apiUrl);
}
function isAuthFree(url: string): boolean {
  return AUTH_FREE.some((p) => url.includes(p));
}

/**
 * Añade el JWT a las peticiones al API y, ante un 401, intenta UNA rotación de
 * refresh token y reintenta la petición original. Si el refresh falla, limpia
 * la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokens = inject(TokenStorageService);
  const auth = inject(AuthService);

  const attachToken = (token: string | null) =>
    token && isApiRequest(req.url) && !isAuthFree(req.url)
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  const authedReq = attachToken(tokens.accessToken);

  return next(authedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const canRetry =
        error.status === 401 &&
        isApiRequest(req.url) &&
        !isAuthFree(req.url) &&
        !!tokens.refreshToken;

      if (!canRetry) return throwError(() => error);

      return auth.refresh().pipe(
        switchMap((res) => next(attachToken(res.accessToken))),
        catchError((refreshErr) => {
          auth.clearSession();
          return throwError(() => refreshErr);
        }),
      );
    }),
  );
};

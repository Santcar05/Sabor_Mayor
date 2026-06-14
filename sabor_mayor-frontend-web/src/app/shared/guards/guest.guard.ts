import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { getRoleHome } from './role.guard';

/** Rutas solo para invitados (login, registro). Si ya hay sesión → al panel apropiado según rol. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) return true;
  return router.createUrlTree([getRoleHome(auth.role() ?? 'CLIENTE')]);
};

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

export function getRoleHome(role: Role): string {
  switch (role) {
    case 'MESERO':
      return '/staff/mesas';
    case 'COCINERO':
      return '/staff/cocina';
    case 'ADMIN':
    case 'SUPER_ADMIN':
      return '/admin';
    default:
      return '/perfil';
  }
}

/**
 * Guard de roles. Llamar como: canActivate: [authGuard, roleGuard('ADMIN', 'SUPER_ADMIN')]
 * Requiere que authGuard corra primero para garantizar que el usuario esté cargado.
 */
export function roleGuard(...allowedRoles: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const role = auth.role();
    if (!role) return router.createUrlTree(['/auth/login']);
    if (allowedRoles.includes(role)) return true;
    return router.createUrlTree([getRoleHome(role)]);
  };
}

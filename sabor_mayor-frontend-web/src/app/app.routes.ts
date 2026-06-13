import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    // CORRECCIÓN: Se cambió 'loadChildren' por 'loadComponent'
    loadComponent: () => import('./features/landing-page/landing-page').then((m) => m.LandingPage),
  },
  {
    path: 'menu',
    // CORRECCIÓN: Se cambió 'loadChildren' por 'loadComponent'
    loadComponent: () =>
      import('./features/menu/menu-container/menu-container').then((m) => m.MenuContainerComponent),
  },
  {
    path: '**',
    redirectTo: '',
  },
];

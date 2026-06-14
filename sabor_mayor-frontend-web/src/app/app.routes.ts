import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';
import { guestGuard } from './shared/guards/guest.guard';
import { roleGuard } from './shared/guards/role.guard';

export const routes: Routes = [
  // Landing autocontenida (trae su propio navbar/footer premium).
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/landing-page/landing-page').then((m) => m.LandingPage),
  },

  // Sitio público (navbar + footer compartidos).
  {
    path: '',
    loadComponent: () =>
      import('./components/layout/public-layout/public-layout').then((m) => m.PublicLayoutComponent),
    children: [
      {
        path: 'carta',
        loadComponent: () => import('./features/carta/carta-page').then((m) => m.CartaPageComponent),
      },
      {
        path: 'carta/:slug',
        loadComponent: () =>
          import('./features/carta/dish-detail/dish-detail-page').then((m) => m.DishDetailPageComponent),
      },
      { path: 'menu', redirectTo: 'carta', pathMatch: 'full' },
      {
        path: 'reservar',
        loadComponent: () =>
          import('./features/reservar/reservar-page').then((m) => m.ReservarPageComponent),
      },
      {
        path: 'reservar/confirmacion',
        loadComponent: () =>
          import('./features/reservar/confirmacion/confirmacion-page').then(
            (m) => m.ConfirmacionPageComponent,
          ),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/pedidos/pedidos-inicio/pedidos-inicio-page').then(
            (m) => m.PedidosInicioPageComponent,
          ),
      },
      {
        path: 'pedidos/carrito',
        loadComponent: () =>
          import('./features/pedidos/carrito/carrito-page').then((m) => m.CarritoPageComponent),
      },
      {
        path: 'pedidos/checkout',
        loadComponent: () =>
          import('./features/pedidos/checkout/checkout-page').then((m) => m.CheckoutPageComponent),
      },
      {
        path: 'pedidos/:id/rastreo',
        loadComponent: () =>
          import('./features/pedidos/rastreo/rastreo-page').then((m) => m.RastreoPageComponent),
      },
      {
        path: 'experiencias',
        loadComponent: () =>
          import('./features/experiencias/experiencias-page').then((m) => m.ExperienciasPageComponent),
      },
      {
        path: 'blog',
        loadComponent: () => import('./features/blog/blog-page').then((m) => m.BlogPageComponent),
      },
      {
        path: 'blog/:slug',
        loadComponent: () =>
          import('./features/blog/blog-detail/blog-detail-page').then((m) => m.BlogDetailPageComponent),
      },
      {
        path: 'galeria',
        loadComponent: () =>
          import('./features/galeria/galeria-page').then((m) => m.GaleriaPageComponent),
      },
      {
        path: 'nosotros',
        loadComponent: () =>
          import('./features/nosotros/nosotros-page').then((m) => m.NosotrosPageComponent),
      },
      {
        path: 'contacto',
        loadComponent: () =>
          import('./features/contacto/contacto-page').then((m) => m.ContactoPageComponent),
      },
    ],
  },

  // Auth (guest-only)
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login-page').then((m) => m.LoginPageComponent),
      },
      {
        path: 'registro',
        loadComponent: () =>
          import('./features/auth/registro/registro-page').then((m) => m.RegistroPageComponent),
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  // Portal del cliente (solo CLIENTE)
  {
    path: 'perfil',
    canActivate: [authGuard, roleGuard('CLIENTE')],
    loadComponent: () =>
      import('./components/layout/portal-layout/portal-layout').then((m) => m.PortalLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/perfil/perfil-page').then((m) => m.PerfilPageComponent),
      },
      {
        path: 'reservas',
        loadComponent: () =>
          import('./features/perfil/reservas/mis-reservas-page').then((m) => m.MisReservasPageComponent),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/perfil/pedidos/mis-pedidos-page').then((m) => m.MisPedidosPageComponent),
      },
      {
        path: 'favoritos',
        loadComponent: () =>
          import('./features/perfil/favoritos/favoritos-page').then((m) => m.FavoritosPageComponent),
      },
      {
        path: 'puntos',
        loadComponent: () =>
          import('./features/perfil/puntos/puntos-page').then((m) => m.PuntosPageComponent),
      },
      {
        path: 'configuracion',
        loadComponent: () =>
          import('./features/perfil/configuracion/configuracion-page').then(
            (m) => m.ConfiguracionPageComponent,
          ),
      },
    ],
  },

  // Panel de staff (MESERO y COCINERO)
  {
    path: 'staff',
    canActivate: [authGuard, roleGuard('MESERO', 'COCINERO')],
    loadComponent: () =>
      import('./components/layout/staff-layout/staff-layout').then((m) => m.StaffLayoutComponent),
    children: [
      {
        path: 'mesas',
        canActivate: [roleGuard('MESERO')],
        loadComponent: () =>
          import('./features/staff/mesas/mesas-page').then((m) => m.MesasPageComponent),
      },
      {
        path: 'cocina',
        canActivate: [roleGuard('COCINERO')],
        loadComponent: () =>
          import('./features/staff/cocina/cocina-page').then((m) => m.CocinaPageComponent),
      },
      { path: '', redirectTo: 'mesas', pathMatch: 'full' },
    ],
  },

  // Panel de administración (ADMIN y SUPER_ADMIN)
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN', 'SUPER_ADMIN')],
    loadComponent: () =>
      import('./components/layout/admin-layout/admin-layout').then((m) => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/admin/dashboard/admin-dashboard-page').then(
            (m) => m.AdminDashboardPageComponent,
          ),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/admin/pedidos/admin-pedidos-page').then((m) => m.AdminPedidosPageComponent),
      },
      {
        path: 'reservas',
        loadComponent: () =>
          import('./features/admin/reservas/admin-reservas-page').then(
            (m) => m.AdminReservasPageComponent,
          ),
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard('SUPER_ADMIN')],
        loadComponent: () =>
          import('./features/admin/usuarios/admin-usuarios-page').then(
            (m) => m.AdminUsuariosPageComponent,
          ),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];

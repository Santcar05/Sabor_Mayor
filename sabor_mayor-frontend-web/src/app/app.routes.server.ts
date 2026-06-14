import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Dynamic routes rendered on-demand (cannot prerender without known params)
  { path: 'carta/:slug', renderMode: RenderMode.Server },
  { path: 'blog/:slug', renderMode: RenderMode.Server },
  { path: 'pedidos/:id/rastreo', renderMode: RenderMode.Server },

  // Auth + role-specific portals — rendered per-request (personalized content)
  { path: 'auth/**', renderMode: RenderMode.Server },
  { path: 'perfil/**', renderMode: RenderMode.Server },
  { path: 'staff/**', renderMode: RenderMode.Server },
  { path: 'admin/**', renderMode: RenderMode.Server },

  // All other routes can be prerendered
  { path: '**', renderMode: RenderMode.Prerender },
];

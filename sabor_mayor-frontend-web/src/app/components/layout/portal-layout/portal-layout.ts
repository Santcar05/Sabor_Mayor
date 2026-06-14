import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar';
import { AuthService } from '../../../shared/services/auth.service';
import { UserAvatarComponent } from '../../ui/user-avatar/user-avatar';

interface PortalLink {
  label: string;
  path: string;
  icon: string;
}

/** Layout del portal del cliente: navbar + sidebar de cuenta + contenido. */
@Component({
  selector: 'app-portal-layout',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NavbarComponent, UserAvatarComponent],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.scss',
})
export class PortalLayoutComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly user = this.auth.currentUser;

  protected readonly links: PortalLink[] = [
    { label: 'Resumen', path: '/perfil', icon: '◆' },
    { label: 'Mis reservas', path: '/perfil/reservas', icon: '📅' },
    { label: 'Mis pedidos', path: '/perfil/pedidos', icon: '🍽' },
    { label: 'Favoritos', path: '/perfil/favoritos', icon: '♥' },
    { label: 'Sabor Points', path: '/perfil/puntos', icon: '★' },
    { label: 'Configuración', path: '/perfil/configuracion', icon: '⚙' },
  ];

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}

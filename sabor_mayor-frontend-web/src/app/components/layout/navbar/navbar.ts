import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { CartService } from '../../../shared/services/cart.service';
import { UserAvatarComponent } from '../../ui/user-avatar/user-avatar';
import { getRoleHome } from '../../../shared/guards/role.guard';
import { Role } from '../../../shared/models/auth.model';

interface NavItem {
  label: string;
  path: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, UserAvatarComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class NavbarComponent {
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  protected readonly user = this.auth.currentUser;
  protected readonly cartCount = this.cart.count;
  protected readonly mobileOpen = signal(false);
  protected readonly userMenuOpen = signal(false);
  protected readonly role = this.auth.role;

  protected readonly links: NavItem[] = [
    { label: 'Carta', path: '/carta' },
    { label: 'Reservar', path: '/reservar' },
    { label: 'Pedir', path: '/pedidos' },
    { label: 'Experiencias', path: '/experiencias' },
    { label: 'Blog', path: '/blog' },
    { label: 'Galería', path: '/galeria' },
    { label: 'Nosotros', path: '/nosotros' },
    { label: 'Contacto', path: '/contacto' },
  ];

  protected readonly isAuth = computed(() => this.auth.isAuthenticated());
  protected readonly isCliente = computed(() => this.role() === 'CLIENTE' || this.role() === null);
  protected readonly isStaff = computed(() => this.role() === 'MESERO' || this.role() === 'COCINERO');
  protected readonly isAdmin = computed(() => this.role() === 'ADMIN' || this.role() === 'SUPER_ADMIN');

  protected readonly panelLink = computed(() => {
    const r = this.role();
    if (!r) return '/perfil';
    return getRoleHome(r);
  });

  protected readonly panelLabel = computed(() => {
    const map: Record<Role, string> = {
      CLIENTE: 'Mi panel',
      MESERO: 'Panel mesero',
      COCINERO: 'Pantalla cocina',
      ADMIN: 'Panel admin',
      SUPER_ADMIN: 'Panel admin',
    };
    return map[this.role() ?? 'CLIENTE'] ?? 'Mi panel';
  });

  protected readonly roleChip = computed(() => {
    const map: Record<Role, string> = {
      CLIENTE: '',
      MESERO: 'Mesero',
      COCINERO: 'Cocinero',
      ADMIN: 'Admin',
      SUPER_ADMIN: 'Super Admin',
    };
    return map[this.role() ?? 'CLIENTE'] ?? '';
  });

  protected toggleMobile(): void { this.mobileOpen.update((v) => !v); }
  protected closeMobile(): void { this.mobileOpen.set(false); }
  protected toggleUserMenu(): void { this.userMenuOpen.update((v) => !v); }
  protected closeUserMenu(): void { this.userMenuOpen.set(false); }

  protected logout(): void {
    this.auth.logout();
    this.userMenuOpen.set(false);
    this.router.navigate(['/']);
  }
}

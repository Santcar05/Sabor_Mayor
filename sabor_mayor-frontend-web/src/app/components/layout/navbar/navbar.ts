import {
  ChangeDetectionStrategy,
  Component,
  computed,
  HostListener,
  inject,
  signal,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
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
  private readonly platformId = inject(PLATFORM_ID);

  protected readonly user = this.auth.currentUser;
  protected readonly cartCount = this.cart.count;
  protected readonly mobileOpen = signal(false);
  protected readonly userMenuOpen = signal(false);
  protected readonly role = this.auth.role;
  protected readonly scrolled = signal(false);

  protected readonly linksLeft: NavItem[] = [
    { label: 'Carta', path: '/carta' },
    { label: 'Experiencias', path: '/experiencias' },
    { label: 'Galería', path: '/galeria' },
  ];

  protected readonly linksRight: NavItem[] = [
    { label: 'Blog', path: '/blog' },
    { label: 'Nosotros', path: '/nosotros' },
    { label: 'Contacto', path: '/contacto' },
  ];

  // Mobile overlay includes Reservar since the CTA button hides when authenticated
  protected readonly allLinks: NavItem[] = [
    ...this.linksLeft,
    { label: 'Reservar', path: '/reservar' },
    ...this.linksRight,
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

  @HostListener('window:scroll')
  onScroll(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.scrolled.set(window.scrollY > 60);
  }

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

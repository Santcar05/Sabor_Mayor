import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { CartService } from '../../../shared/services/cart.service';
import { UserAvatarComponent } from '../../ui/user-avatar/user-avatar';

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

  protected toggleMobile(): void {
    this.mobileOpen.update((v) => !v);
  }
  protected closeMobile(): void {
    this.mobileOpen.set(false);
  }
  protected toggleUserMenu(): void {
    this.userMenuOpen.update((v) => !v);
  }

  protected logout(): void {
    this.auth.logout();
    this.userMenuOpen.set(false);
    this.router.navigate(['/']);
  }
}

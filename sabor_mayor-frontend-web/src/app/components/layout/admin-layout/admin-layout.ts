import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar';
import { AuthService } from '../../../shared/services/auth.service';

interface AdminLink {
  label: string;
  path: string;
  icon: string;
  superAdminOnly?: boolean;
}

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NavbarComponent],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayoutComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly user = this.auth.currentUser;
  protected readonly role = this.auth.role;
  protected readonly isSuperAdmin = computed(() => this.auth.role() === 'SUPER_ADMIN');

  private readonly allLinks: AdminLink[] = [
    { label: 'Dashboard', path: '/admin', icon: '📊' },
    { label: 'Pedidos', path: '/admin/pedidos', icon: '🍽' },
    { label: 'Reservas', path: '/admin/reservas', icon: '📅' },
    { label: 'Usuarios', path: '/admin/usuarios', icon: '👥', superAdminOnly: true },
  ];

  protected readonly links = computed(() =>
    this.allLinks.filter((l) => !l.superAdminOnly || this.isSuperAdmin()),
  );

  protected readonly roleLabel = computed(() =>
    this.role() === 'SUPER_ADMIN' ? 'Super Admin' : 'Administrador',
  );

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}

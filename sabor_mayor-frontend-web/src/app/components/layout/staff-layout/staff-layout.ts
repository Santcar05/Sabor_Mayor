import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar';
import { AuthService } from '../../../shared/services/auth.service';

interface StaffLink {
  label: string;
  path: string;
  icon: string;
  roles: string[];
}

@Component({
  selector: 'app-staff-layout',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NavbarComponent],
  templateUrl: './staff-layout.html',
  styleUrl: './staff-layout.scss',
})
export class StaffLayoutComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly user = this.auth.currentUser;
  protected readonly role = this.auth.role;

  private readonly allLinks: StaffLink[] = [
    { label: 'Mesas', path: '/staff/mesas', icon: '🍽', roles: ['MESERO'] },
    { label: 'Cocina', path: '/staff/cocina', icon: '👨‍🍳', roles: ['COCINERO'] },
  ];

  protected readonly links = computed(() =>
    this.allLinks.filter((l) => l.roles.includes(this.role() ?? '')),
  );

  protected roleLabel = computed(() => {
    const map: Record<string, string> = { MESERO: 'Mesero', COCINERO: 'Cocinero' };
    return map[this.role() ?? ''] ?? 'Staff';
  });

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}

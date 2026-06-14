import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../shared/services/api.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { AuthUser, Role } from '../../../shared/models/auth.model';

@Component({
  selector: 'app-admin-usuarios-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, FormsModule, LoadingSpinnerComponent],
  templateUrl: './admin-usuarios-page.html',
  styleUrl: './admin-usuarios-page.scss',
})
export class AdminUsuariosPageComponent {
  private readonly api = inject(ApiService);

  protected readonly loading = signal(true);
  protected readonly users = signal<AuthUser[]>([]);
  protected readonly filterRole = signal<string>('');

  protected readonly filtered = computed(() => {
    const role = this.filterRole();
    return role ? this.users().filter((u) => u.role === role) : this.users();
  });

  protected readonly roleOptions = [
    { value: '', label: 'Todos los roles' },
    { value: 'CLIENTE', label: 'Clientes' },
    { value: 'MESERO', label: 'Meseros' },
    { value: 'COCINERO', label: 'Cocineros' },
    { value: 'ADMIN', label: 'Admins' },
    { value: 'SUPER_ADMIN', label: 'Super Admins' },
  ];

  constructor() {
    this.api
      .get<AuthUser[]>('/api/auth/users')
      .pipe(catchError(() => of([])), takeUntilDestroyed())
      .subscribe((users) => {
        this.users.set(users);
        this.loading.set(false);
      });
  }

  protected roleLabel(role: Role): string {
    const map: Record<Role, string> = {
      CLIENTE: 'Cliente',
      MESERO: 'Mesero',
      COCINERO: 'Cocinero',
      ADMIN: 'Admin',
      SUPER_ADMIN: 'Super Admin',
    };
    return map[role] ?? role;
  }

  protected roleClass(role: Role): string {
    const map: Record<Role, string> = {
      CLIENTE: 'cliente',
      MESERO: 'mesero',
      COCINERO: 'cocinero',
      ADMIN: 'admin',
      SUPER_ADMIN: 'superadmin',
    };
    return map[role] ?? 'cliente';
  }

  get filterRoleModel(): string { return this.filterRole(); }
  set filterRoleModel(v: string) { this.filterRole.set(v); }
}

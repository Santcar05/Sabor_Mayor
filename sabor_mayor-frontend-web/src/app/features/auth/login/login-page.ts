import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { getRoleHome } from '../../../shared/guards/role.guard';
import { FormInputComponent } from '../../../components/ui/form-input/form-input';
import { ButtonComponent } from '../../../components/ui/button/button';

@Component({
  selector: 'app-login-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, FormInputComponent, ButtonComponent],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notify = inject(NotificationService);

  protected readonly loading = signal(false);
  protected readonly showPass = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected fieldError(field: string): string {
    const ctrl = this.form.get(field);
    if (!ctrl?.touched || !ctrl.invalid) return '';
    if (ctrl.errors?.['required']) return 'Campo requerido';
    if (ctrl.errors?.['email']) return 'Correo inválido';
    if (ctrl.errors?.['minlength']) return 'Mínimo 8 caracteres';
    return 'Campo inválido';
  }

  protected submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const { email, password } = this.form.getRawValue();
    this.auth.login({ email, password }).subscribe({
      next: (user) => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
        } else {
          this.router.navigate([getRoleHome(user.role)]);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.notify.error(err?.error?.detail ?? 'Credenciales incorrectas. Intenta de nuevo.');
      },
    });
  }

  protected oauthGoogle(): void {
    this.notify.info('Próximamente — login con Google estará disponible.');
  }
}

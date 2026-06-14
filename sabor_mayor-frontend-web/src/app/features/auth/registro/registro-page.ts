import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { FormInputComponent } from '../../../components/ui/form-input/form-input';
import { ButtonComponent } from '../../../components/ui/button/button';

function passwordsMatch(ctrl: AbstractControl): Record<string, boolean> | null {
  const pass = ctrl.get('password')?.value;
  const confirm = ctrl.get('confirm')?.value;
  return pass && confirm && pass !== confirm ? { mismatch: true } : null;
}

@Component({
  selector: 'app-registro-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, FormInputComponent, ButtonComponent],
  templateUrl: './registro-page.html',
  styleUrl: './registro-page.scss',
})
export class RegistroPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  protected readonly step = signal(1);
  protected readonly loading = signal(false);

  protected readonly form = this.fb.nonNullable.group(
    {
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirm: ['', Validators.required],
      terms: [false, Validators.requiredTrue],
    },
    { validators: passwordsMatch },
  );

  protected fieldError(field: string): string {
    const ctrl = this.form.get(field);
    if (!ctrl?.touched || !ctrl.invalid) return '';
    if (ctrl.errors?.['required']) return 'Campo requerido';
    if (ctrl.errors?.['email']) return 'Correo inválido';
    if (ctrl.errors?.['minlength']) return `Mínimo ${ctrl.errors['minlength'].requiredLength} caracteres`;
    return 'Campo inválido';
  }

  protected confirmError(): string {
    const ctrl = this.form.get('confirm');
    if (!ctrl?.touched) return '';
    if (this.form.errors?.['mismatch']) return 'Las contraseñas no coinciden';
    return this.fieldError('confirm');
  }

  protected nextStep(): void {
    ['firstName', 'lastName', 'email', 'phone'].forEach((f) => this.form.get(f)?.markAsTouched());
    const step1Valid = !this.form.get('firstName')?.invalid
      && !this.form.get('lastName')?.invalid
      && !this.form.get('email')?.invalid;
    if (step1Valid) this.step.set(2);
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    const { firstName, lastName, email, password } = this.form.getRawValue();
    const fullName = `${firstName.trim()} ${lastName.trim()}`.trim();
    this.auth.register({ fullName, email, password }).subscribe({
      next: () => {
        this.notify.success('¡Cuenta creada! Bienvenido a Sabor Mayor.');
        this.router.navigate(['/perfil']);
      },
      error: (err) => {
        this.loading.set(false);
        this.notify.error(err?.error?.detail ?? 'No pudimos crear tu cuenta. Intenta de nuevo.');
      },
    });
  }
}

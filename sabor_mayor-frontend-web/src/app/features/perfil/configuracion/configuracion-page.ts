import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth.service';
import { UserService } from '../../../shared/services/user.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { FormInputComponent } from '../../../components/ui/form-input/form-input';
import { ButtonComponent } from '../../../components/ui/button/button';

@Component({
  selector: 'app-configuracion-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, FormInputComponent, ButtonComponent],
  templateUrl: './configuracion-page.html',
  styleUrl: './configuracion-page.scss',
})
export class ConfiguracionPageComponent {
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);
  private readonly userSvc = inject(UserService);
  private readonly notify = inject(NotificationService);

  protected readonly savingProfile = signal(false);

  protected readonly profileForm = this.fb.nonNullable.group({
    fullName: [this.auth.currentUser()?.fullName ?? '', [Validators.required, Validators.minLength(2)]],
    phone: [''],
  });

  protected readonly dietPrefs = signal<string[]>([]);
  protected readonly dietOptions = ['Vegetariano', 'Vegano', 'Sin gluten', 'Sin lácteos', 'Sin mariscos', 'Kosher'];

  protected toggleDiet(opt: string): void {
    this.dietPrefs.update((list) =>
      list.includes(opt) ? list.filter((x) => x !== opt) : [...list, opt],
    );
  }

  protected fieldError(field: string): string {
    const ctrl = this.profileForm.get(field);
    if (!ctrl?.touched || !ctrl.invalid) return '';
    if (ctrl.errors?.['required']) return 'Campo requerido';
    if (ctrl.errors?.['minlength']) return `Mínimo ${ctrl.errors['minlength'].requiredLength} caracteres`;
    return 'Campo inválido';
  }

  protected saveProfile(): void {
    if (this.profileForm.invalid || this.savingProfile()) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.savingProfile.set(true);
    const { fullName, phone } = this.profileForm.getRawValue();
    this.userSvc
      .updateProfile({ fullName, phone: phone || undefined, dietaryPreferences: this.dietPrefs(), allergies: [] })
      .subscribe({
      next: () => {
        this.notify.success('Perfil actualizado');
        this.savingProfile.set(false);
      },
      error: () => {
        this.notify.error('No se pudo actualizar el perfil');
        this.savingProfile.set(false);
      },
    });
  }

  protected logout(): void {
    this.auth.logout();
  }
}

import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { NotificationService } from '../../shared/services/notification.service';
import { FormInputComponent } from '../../components/ui/form-input/form-input';
import { ButtonComponent } from '../../components/ui/button/button';

@Component({
  selector: 'app-contacto-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, FormInputComponent, ButtonComponent],
  templateUrl: './contacto-page.html',
  styleUrl: './contacto-page.scss',
})
export class ContactoPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly notify = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);

  private readonly queryParams = toSignal(this.route.queryParams, { initialValue: {} as Params });

  protected readonly submitting = signal(false);
  protected readonly sent = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    subject: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(20)]],
  });

  constructor() {
    const params = this.queryParams();
    if (params['subject']) {
      this.form.patchValue({ subject: params['subject'] });
    }
  }

  protected fieldError(field: string): string {
    const ctrl = this.form.get(field);
    if (!ctrl?.touched || !ctrl.invalid) return '';
    if (ctrl.errors?.['required']) return 'Campo requerido';
    if (ctrl.errors?.['email']) return 'Correo inválido';
    if (ctrl.errors?.['minlength']) return `Mínimo ${ctrl.errors['minlength'].requiredLength} caracteres`;
    return 'Campo inválido';
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    // No contact endpoint in backend — simulate success after 800ms
    setTimeout(() => {
      this.submitting.set(false);
      this.sent.set(true);
      this.notify.success('¡Mensaje enviado! Te responderemos pronto.');
    }, 800);
  }

  protected reset(): void {
    this.sent.set(false);
    this.form.reset();
  }

  readonly hours = [
    { day: 'Martes – Viernes', time: '12:00 – 15:00 / 19:00 – 23:00' },
    { day: 'Sábados', time: '12:00 – 23:00' },
    { day: 'Domingos', time: '12:00 – 17:00' },
    { day: 'Lunes', time: 'Cerrado' },
  ];

  readonly subjects = [
    'Reserva grupal (10+ personas)',
    'Eventos privados',
    'Prensa y medios',
    'Propuesta comercial',
    'Sugerencias y comentarios',
    'Otro',
  ];
}

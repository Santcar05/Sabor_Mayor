import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class FooterComponent {
  private readonly notify = inject(NotificationService);
  protected readonly year = new Date().getFullYear();
  protected readonly email = signal('');

  protected subscribe(event: Event): void {
    event.preventDefault();
    const value = this.email().trim();
    if (!value || !value.includes('@')) {
      this.notify.warning('Ingresa un correo válido', 'Newsletter');
      return;
    }
    this.notify.success('¡Gracias por suscribirte a Sabor Mayor!', 'Newsletter');
    this.email.set('');
  }
}

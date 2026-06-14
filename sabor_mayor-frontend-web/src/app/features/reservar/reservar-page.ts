import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../shared/services/reservation.service';
import { AuthService } from '../../shared/services/auth.service';
import { NotificationService } from '../../shared/services/notification.service';
import { AvailabilitySlot } from '../../shared/models/reservation.model';
import { ButtonComponent } from '../../components/ui/button/button';
import { LoadingSpinnerComponent } from '../../components/ui/loading-spinner/loading-spinner';
import { CopCurrencyPipe } from '../../shared/pipes/cop-currency.pipe';

@Component({
  selector: 'app-reservar-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ButtonComponent, LoadingSpinnerComponent, CopCurrencyPipe],
  templateUrl: './reservar-page.html',
  styleUrl: './reservar-page.scss',
})
export class ReservarPageComponent {
  private readonly reservations = inject(ReservationService);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly today = new Date().toISOString().split('T')[0];

  protected readonly date = signal(this.today);
  protected readonly partySize = signal(2);
  protected readonly selectedTime = signal<string | null>(null);
  protected readonly specialRequests = signal('');
  protected readonly occasion = signal('');

  protected readonly slots = signal<AvailabilitySlot[] | null>(null);
  protected readonly loadingSlots = signal(false);
  protected readonly submitting = signal(false);

  protected readonly depositRequired = computed(() => this.partySize() >= 8);
  protected readonly depositAmount = computed(() => (this.depositRequired() ? this.partySize() * 30000 : 0));

  protected readonly partyOptions = Array.from({ length: 20 }, (_, i) => i + 1);

  protected setDate(value: string): void {
    this.date.set(value);
    this.resetSlots();
  }
  protected setParty(value: number): void {
    this.partySize.set(Number(value));
    this.resetSlots();
  }
  private resetSlots(): void {
    this.slots.set(null);
    this.selectedTime.set(null);
  }

  protected checkAvailability(): void {
    this.loadingSlots.set(true);
    this.selectedTime.set(null);
    this.reservations.getAvailability(this.date(), this.partySize()).subscribe({
      next: (slots) => {
        this.slots.set(slots);
        this.loadingSlots.set(false);
      },
      error: () => {
        this.slots.set([]);
        this.loadingSlots.set(false);
        this.notify.error('No pudimos consultar la disponibilidad.');
      },
    });
  }

  protected pickTime(slot: AvailabilitySlot): void {
    if (!slot.fitsParty) return;
    this.selectedTime.set(slot.time.slice(0, 5));
  }

  protected confirm(): void {
    if (!this.selectedTime()) {
      this.notify.warning('Selecciona un horario disponible');
      return;
    }
    if (!this.auth.isAuthenticated()) {
      this.notify.info('Inicia sesión para confirmar tu reserva');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/reservar' } });
      return;
    }

    this.submitting.set(true);
    this.reservations
      .create({
        date: this.date(),
        time: this.selectedTime()!,
        partySize: this.partySize(),
        specialRequests: [this.occasion(), this.specialRequests()].filter(Boolean).join(' · ') || undefined,
      })
      .subscribe({
        next: (reservation) => {
          this.submitting.set(false);
          this.router.navigate(['/reservar/confirmacion'], { state: { reservation } });
        },
        error: (err) => {
          this.submitting.set(false);
          this.notify.error(err?.error?.detail ?? 'No se pudo crear la reserva.');
        },
      });
  }
}

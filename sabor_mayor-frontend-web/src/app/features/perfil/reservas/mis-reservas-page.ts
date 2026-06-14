import { ChangeDetectionStrategy, Component, inject, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ReservationService } from '../../../shared/services/reservation.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { Reservation } from '../../../shared/models/reservation.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-mis-reservas-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, LoadingSpinnerComponent, ButtonComponent, DatePipe],
  templateUrl: './mis-reservas-page.html',
  styleUrl: './mis-reservas-page.scss',
})
export class MisReservasPageComponent {
  private readonly reservationSvc = inject(ReservationService);
  private readonly notify = inject(NotificationService);

  private readonly _all = toSignal(
    this.reservationSvc.myReservations().pipe(catchError(() => of(null))),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this._all() === undefined);
  protected readonly reservations = computed(() => this._all() ?? []);
  protected readonly error = computed(() => this._all() === null);
  protected readonly cancelling = signal<string | null>(null);

  protected cancel(id: string): void {
    if (this.cancelling()) return;
    this.cancelling.set(id);
    this.reservationSvc.cancel(id).subscribe({
      next: () => {
        this.notify.success('Reserva cancelada');
        this.cancelling.set(null);
      },
      error: () => {
        this.notify.error('No se pudo cancelar la reserva');
        this.cancelling.set(null);
      },
    });
  }

  protected canCancel(r: Reservation): boolean {
    return r.status === 'PENDING' || r.status === 'CONFIRMED';
  }

  protected statusClass(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'confirmed',
      PENDING: 'pending',
      CANCELLED: 'cancelled',
      COMPLETED: 'completed',
      NO_SHOW: 'cancelled',
    };
    return map[status] ?? 'pending';
  }
}

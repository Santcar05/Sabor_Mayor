import { ChangeDetectionStrategy, Component, inject, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ReservationService } from '../../../shared/services/reservation.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { Reservation } from '../../../shared/models/reservation.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';

@Component({
  selector: 'app-mis-reservas-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, LoadingSpinnerComponent, ButtonComponent],
  templateUrl: './mis-reservas-page.html',
  styleUrl: './mis-reservas-page.scss',
})
export class MisReservasPageComponent {
  private readonly reservationSvc = inject(ReservationService);
  private readonly notify = inject(NotificationService);

  private readonly _all = signal<Reservation[] | null | undefined>(undefined);
  protected readonly cancelling = signal<string | null>(null);

  protected readonly loading = computed(() => this._all() === undefined);
  protected readonly reservations = computed(() => this._all() ?? []);
  protected readonly error = computed(() => this._all() === null);

  constructor() {
    this.reservationSvc
      .myReservations()
      .pipe(catchError(() => of(null)), takeUntilDestroyed())
      .subscribe((r) => this._all.set(r));
  }

  protected cancel(id: string): void {
    if (this.cancelling()) return;
    this.cancelling.set(id);
    this.reservationSvc.cancel(id).subscribe({
      next: () => {
        this._all.update(
          (list) => list?.map((r) => (r.id === id ? { ...r, status: 'CANCELLED' as const } : r)) ?? list,
        );
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

  protected statusLabel(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'Confirmada',
      PENDING: 'Pendiente',
      CANCELLED: 'Cancelada',
      COMPLETED: 'Completada',
      NO_SHOW: 'No asistió',
    };
    return map[status] ?? status;
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

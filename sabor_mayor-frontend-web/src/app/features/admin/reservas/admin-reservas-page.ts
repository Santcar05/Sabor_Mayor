import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReservationService } from '../../../shared/services/reservation.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { Reservation } from '../../../shared/models/reservation.model';

@Component({
  selector: 'app-admin-reservas-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, FormsModule, LoadingSpinnerComponent],
  templateUrl: './admin-reservas-page.html',
  styleUrl: './admin-reservas-page.scss',
})
export class AdminReservasPageComponent {
  private readonly reservationSvc = inject(ReservationService);

  protected readonly loading = signal(true);
  protected readonly reservations = signal<Reservation[]>([]);
  protected readonly filterStatus = signal<string>('');

  protected readonly filtered = computed(() => {
    const status = this.filterStatus();
    return status ? this.reservations().filter((r) => r.status === status) : this.reservations();
  });

  constructor() {
    this.reservationSvc
      .allReservations()
      .pipe(catchError(() => of([])), takeUntilDestroyed())
      .subscribe((reservations) => {
        this.reservations.set(reservations);
        this.loading.set(false);
      });
  }

  protected statusLabel(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'Confirmada', PENDING: 'Pendiente',
      CANCELLED: 'Cancelada', COMPLETED: 'Completada', NO_SHOW: 'No asistió',
    };
    return map[status] ?? status;
  }

  protected statusClass(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'confirmed', PENDING: 'pending',
      CANCELLED: 'cancelled', COMPLETED: 'completed', NO_SHOW: 'cancelled',
    };
    return map[status] ?? 'pending';
  }

  protected readonly statusOptions = [
    { value: '', label: 'Todas' },
    { value: 'PENDING', label: 'Pendientes' },
    { value: 'CONFIRMED', label: 'Confirmadas' },
    { value: 'COMPLETED', label: 'Completadas' },
    { value: 'CANCELLED', label: 'Canceladas' },
    { value: 'NO_SHOW', label: 'No asistió' },
  ];

  get filterStatusModel(): string { return this.filterStatus(); }
  set filterStatusModel(v: string) { this.filterStatus.set(v); }
}

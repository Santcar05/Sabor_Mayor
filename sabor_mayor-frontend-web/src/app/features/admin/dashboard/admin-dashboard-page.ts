import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, forkJoin, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';
import { OrderService } from '../../../shared/services/order.service';
import { ReservationService } from '../../../shared/services/reservation.service';
import { AuthService } from '../../../shared/services/auth.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { Order } from '../../../shared/models/order.model';
import { Reservation } from '../../../shared/models/reservation.model';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CopCurrencyPipe, LoadingSpinnerComponent],
  templateUrl: './admin-dashboard-page.html',
  styleUrl: './admin-dashboard-page.scss',
})
export class AdminDashboardPageComponent {
  private readonly orderSvc = inject(OrderService);
  private readonly reservationSvc = inject(ReservationService);
  protected readonly auth = inject(AuthService);

  protected readonly loading = signal(true);
  protected readonly orders = signal<Order[]>([]);
  protected readonly reservations = signal<Reservation[]>([]);

  protected readonly isSuperAdmin = computed(() => this.auth.role() === 'SUPER_ADMIN');

  protected readonly stats = computed(() => {
    const orders = this.orders();
    const today = new Date().toISOString().split('T')[0];
    const todayOrders = orders.filter((o) => o.createdAt.startsWith(today));
    const pending = orders.filter((o) =>
      ['CREATED', 'CONFIRMED', 'IN_KITCHEN', 'READY'].includes(o.status),
    );
    const revenue = todayOrders
      .filter((o) => o.status !== 'CANCELLED')
      .reduce((s, o) => s + o.total, 0);

    const reservations = this.reservations();
    const todayRes = reservations.filter((r) => r.date === today);
    const confirmedRes = reservations.filter((r) => r.status === 'CONFIRMED' || r.status === 'PENDING');

    return { todayOrders: todayOrders.length, pending: pending.length, revenue, todayRes: todayRes.length, confirmedRes: confirmedRes.length };
  });

  protected readonly recentOrders = computed(() =>
    this.orders().slice(0, 8),
  );

  constructor() {
    forkJoin({
      orders: this.orderSvc.allOrders({ size: 50 }).pipe(catchError(() => of([]))),
      reservations: this.reservationSvc.allReservations().pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed())
      .subscribe(({ orders, reservations }) => {
        this.orders.set(orders);
        this.reservations.set(reservations);
        this.loading.set(false);
      });
  }

  protected orderStatusLabel(status: string): string {
    const map: Record<string, string> = {
      CREATED: 'Nuevo',
      CONFIRMED: 'Confirmado',
      IN_KITCHEN: 'En cocina',
      READY: 'Listo',
      SERVED: 'Servido',
      DELIVERED: 'Entregado',
      PAID: 'Pagado',
      CANCELLED: 'Cancelado',
    };
    return map[status] ?? status;
  }

  protected orderStatusClass(status: string): string {
    if (['CREATED', 'CONFIRMED'].includes(status)) return 'pending';
    if (status === 'IN_KITCHEN') return 'cooking';
    if (status === 'READY') return 'ready';
    if (status === 'CANCELLED') return 'cancelled';
    return 'done';
  }
}

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';
import { OrderService } from '../../../shared/services/order.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { Order, OrderStatus } from '../../../shared/models/order.model';

@Component({
  selector: 'app-admin-pedidos-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, FormsModule, CopCurrencyPipe, LoadingSpinnerComponent, ButtonComponent],
  templateUrl: './admin-pedidos-page.html',
  styleUrl: './admin-pedidos-page.scss',
})
export class AdminPedidosPageComponent {
  private readonly orderSvc = inject(OrderService);
  private readonly notify = inject(NotificationService);

  protected readonly loading = signal(true);
  protected readonly orders = signal<Order[]>([]);
  protected readonly filterStatus = signal<string>('');
  protected readonly updating = signal<string | null>(null);

  protected readonly filtered = computed(() => {
    const status = this.filterStatus();
    return status ? this.orders().filter((o) => o.status === status) : this.orders();
  });

  constructor() {
    this.orderSvc
      .allOrders({ size: 100 })
      .pipe(catchError(() => of([])), takeUntilDestroyed())
      .subscribe((orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      });
  }

  protected updateStatus(id: string, status: OrderStatus): void {
    if (this.updating()) return;
    this.updating.set(id);
    this.orderSvc.updateStatus(id, status).subscribe({
      next: (updated) => {
        this.orders.update((list) => list.map((o) => (o.id === id ? updated : o)));
        this.notify.success('Estado actualizado');
        this.updating.set(null);
      },
      error: () => {
        this.notify.error('No se pudo actualizar el estado');
        this.updating.set(null);
      },
    });
  }

  protected statusOptions: { value: string; label: string }[] = [
    { value: '', label: 'Todos' },
    { value: 'CREATED', label: 'Nuevos' },
    { value: 'CONFIRMED', label: 'Confirmados' },
    { value: 'IN_KITCHEN', label: 'En cocina' },
    { value: 'READY', label: 'Listos' },
    { value: 'SERVED', label: 'Servidos' },
    { value: 'DELIVERED', label: 'Entregados' },
    { value: 'PAID', label: 'Pagados' },
    { value: 'CANCELLED', label: 'Cancelados' },
  ];

  protected nextStatuses(status: string): { value: OrderStatus; label: string }[] {
    const flow: Record<string, { value: OrderStatus; label: string }[]> = {
      CREATED: [{ value: 'CONFIRMED', label: 'Confirmar' }, { value: 'CANCELLED', label: 'Cancelar' }],
      CONFIRMED: [{ value: 'IN_KITCHEN', label: 'Enviar a cocina' }, { value: 'CANCELLED', label: 'Cancelar' }],
      IN_KITCHEN: [{ value: 'READY', label: 'Marcar listo' }],
      READY: [{ value: 'SERVED', label: 'Marcar servido' }],
      SERVED: [{ value: 'PAID', label: 'Marcar pagado' }],
    };
    return flow[status] ?? [];
  }

  protected statusLabel(status: string): string {
    const map: Record<string, string> = {
      CREATED: 'Nuevo', CONFIRMED: 'Confirmado', IN_KITCHEN: 'En cocina',
      READY: 'Listo', SERVED: 'Servido', DELIVERED: 'Entregado',
      PAID: 'Pagado', CANCELLED: 'Cancelado',
    };
    return map[status] ?? status;
  }

  protected statusClass(status: string): string {
    if (['CREATED', 'CONFIRMED'].includes(status)) return 'pending';
    if (status === 'IN_KITCHEN') return 'cooking';
    if (status === 'READY') return 'ready';
    if (status === 'CANCELLED') return 'cancelled';
    return 'done';
  }

  get filterStatusModel(): string { return this.filterStatus(); }
  set filterStatusModel(v: string) { this.filterStatus.set(v); }
}

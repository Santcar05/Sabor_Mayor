import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of, timer } from 'rxjs';
import { OrderService } from '../../../shared/services/order.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { Order, OrderStatus } from '../../../shared/models/order.model';

@Component({
  selector: 'app-cocina-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [LoadingSpinnerComponent, ButtonComponent],
  templateUrl: './cocina-page.html',
  styleUrl: './cocina-page.scss',
})
export class CocinaPageComponent {
  private readonly orderSvc = inject(OrderService);
  private readonly notify = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loading = signal(true);
  protected readonly orders = signal<Order[]>([]);
  protected readonly updating = signal<string | null>(null);

  protected readonly newOrders = computed(() => this.orders().filter((o) => o.status === 'CONFIRMED'));
  protected readonly inProgress = computed(() => this.orders().filter((o) => o.status === 'IN_KITCHEN'));
  protected readonly ready = computed(() => this.orders().filter((o) => o.status === 'READY'));

  constructor() {
    this.load();
    timer(20_000, 20_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
  }

  private load(): void {
    this.orderSvc
      .allOrders({ statuses: ['CONFIRMED', 'IN_KITCHEN', 'READY'] })
      .pipe(catchError(() => of([])))
      .subscribe((orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      });
  }

  protected startCooking(id: string): void {
    this.changeStatus(id, 'IN_KITCHEN', 'Preparación iniciada');
  }

  protected markReady(id: string): void {
    this.changeStatus(id, 'READY', 'Pedido marcado como listo');
  }

  private changeStatus(id: string, status: OrderStatus, successMsg: string): void {
    if (this.updating()) return;
    this.updating.set(id);
    this.orderSvc.updateStatus(id, status).subscribe({
      next: (updated) => {
        this.orders.update((list) => list.map((o) => (o.id === id ? updated : o)));
        this.notify.success(successMsg);
        this.updating.set(null);
      },
      error: () => {
        this.notify.error('No se pudo actualizar el pedido');
        this.updating.set(null);
      },
    });
  }

  protected stationLabel(station: string): string {
    const map: Record<string, string> = {
      FRIOS: '❄ Fríos',
      CALIENTES: '🔥 Calientes',
      POSTRES: '🍮 Postres',
      BAR: '🍹 Bar',
    };
    return map[station] ?? station;
  }

  protected elapsed(createdAt: string): string {
    const mins = Math.floor((Date.now() - new Date(createdAt).getTime()) / 60_000);
    if (mins < 1) return '< 1 min';
    if (mins === 1) return '1 min';
    return `${mins} min`;
  }

  protected elapsedClass(createdAt: string): string {
    const mins = Math.floor((Date.now() - new Date(createdAt).getTime()) / 60_000);
    if (mins >= 20) return 'urgent';
    if (mins >= 10) return 'warning';
    return 'ok';
  }
}

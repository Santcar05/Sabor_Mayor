import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, forkJoin, of, timer } from 'rxjs';
import { DatePipe } from '@angular/common';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';
import { OrderService } from '../../../shared/services/order.service';
import { TableService } from '../../../shared/services/table.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { Order, OrderStatus, RestaurantTable } from '../../../shared/models/order.model';

interface TableWithOrders {
  table: RestaurantTable;
  activeOrders: Order[];
}

@Component({
  selector: 'app-mesas-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, CopCurrencyPipe, LoadingSpinnerComponent, ButtonComponent],
  templateUrl: './mesas-page.html',
  styleUrl: './mesas-page.scss',
})
export class MesasPageComponent {
  private readonly orderSvc = inject(OrderService);
  private readonly tableSvc = inject(TableService);
  private readonly notify = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loading = signal(true);
  protected readonly tables = signal<TableWithOrders[]>([]);
  protected readonly updatingOrder = signal<string | null>(null);
  protected readonly selected = signal<TableWithOrders | null>(null);

  constructor() {
    this.load();
    timer(30_000, 30_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
  }

  private load(): void {
    forkJoin({
      tables: this.tableSvc.getAllTables().pipe(catchError(() => of([]))),
      orders: this.orderSvc
        .allOrders({ statuses: ['CREATED', 'CONFIRMED', 'IN_KITCHEN', 'READY'] })
        .pipe(catchError(() => of([]))),
    }).subscribe(({ tables, orders }) => {
      const mapped = tables.map((t) => ({
        table: t,
        activeOrders: orders.filter((o) => o.tableId === t.id),
      }));
      this.tables.set(mapped);
      this.loading.set(false);
      const sel = this.selected();
      if (sel) {
        const updated = mapped.find((tw) => tw.table.id === sel.table.id);
        this.selected.set(updated ?? null);
      }
    });
  }

  protected selectTable(tw: TableWithOrders): void {
    this.selected.set(this.selected()?.table?.id === tw.table.id ? null : tw);
  }

  protected markServed(orderId: string): void {
    if (this.updatingOrder()) return;
    this.updatingOrder.set(orderId);
    this.orderSvc.updateStatus(orderId, 'SERVED' as OrderStatus).subscribe({
      next: (updated) => {
        this.tables.update((list) =>
          list.map((tw) => ({
            ...tw,
            activeOrders: tw.activeOrders.map((o) => (o.id === orderId ? updated : o)),
          })),
        );
        const sel = this.selected();
        if (sel) {
          this.selected.set({
            ...sel,
            activeOrders: sel.activeOrders.map((o) => (o.id === orderId ? updated : o)),
          });
        }
        this.notify.success('Pedido marcado como servido');
        this.updatingOrder.set(null);
      },
      error: () => {
        this.notify.error('No se pudo actualizar el pedido');
        this.updatingOrder.set(null);
      },
    });
  }

  protected tableClass(status: string): string {
    const map: Record<string, string> = {
      LIBRE: 'free', OCUPADA: 'occupied', RESERVADA: 'reserved', MANTENIMIENTO: 'maintenance',
    };
    return map[status] ?? 'free';
  }

  protected tableStatusLabel(status: string): string {
    const map: Record<string, string> = {
      LIBRE: 'Libre', OCUPADA: 'Ocupada', RESERVADA: 'Reservada', MANTENIMIENTO: 'Mant.',
    };
    return map[status] ?? status;
  }

  protected orderStatusLabel(status: string): string {
    const map: Record<string, string> = {
      CREATED: 'Nuevo', CONFIRMED: 'Confirmado', IN_KITCHEN: 'En cocina',
      READY: 'Listo', SERVED: 'Servido',
    };
    return map[status] ?? status;
  }

  protected canServe(status: string): boolean {
    return status === 'READY';
  }

  protected readonly summary = computed(() => {
    const all = this.tables();
    return {
      total: all.length,
      occupied: all.filter((t) => t.table.status === 'OCUPADA').length,
      free: all.filter((t) => t.table.status === 'LIBRE').length,
      withOrders: all.filter((t) => t.activeOrders.length > 0).length,
    };
  });
}

import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { OrderService } from '../../../shared/services/order.service';
import { CartService } from '../../../shared/services/cart.service';
import { MenuService } from '../../../shared/services/menu.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { Order } from '../../../shared/models/order.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

@Component({
  selector: 'app-mis-pedidos-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, LoadingSpinnerComponent, ButtonComponent, CopCurrencyPipe],
  templateUrl: './mis-pedidos-page.html',
  styleUrl: './mis-pedidos-page.scss',
})
export class MisPedidosPageComponent {
  private readonly orderSvc = inject(OrderService);
  private readonly cartSvc = inject(CartService);
  private readonly menuSvc = inject(MenuService);
  private readonly notify = inject(NotificationService);

  private readonly _all = toSignal(
    this.orderSvc.myOrders().pipe(catchError(() => of(null))),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this._all() === undefined);
  protected readonly orders = computed(() => this._all() ?? []);
  protected readonly error = computed(() => this._all() === null);

  protected statusClass(status: string): string {
    const map: Record<string, string> = {
      CREATED: 'pending',
      CONFIRMED: 'confirmed',
      IN_KITCHEN: 'cooking',
      READY: 'ready',
      SERVED: 'delivering',
      DELIVERED: 'delivered',
      PAID: 'paid',
      CANCELLED: 'cancelled',
    };
    return map[status] ?? 'pending';
  }

  protected orderAgain(order: Order): void {
    const ids = order.items.map((i) => i.dishId);
    this.menuSvc.getDishesByIds(ids).subscribe({
      next: (dishes) => {
        order.items.forEach((item) => {
          const dish = dishes.find((d) => d.id === item.dishId);
          if (dish) this.cartSvc.add(dish, item.quantity, item.notes);
        });
        this.notify.success('Platos añadidos al carrito');
      },
      error: () => this.notify.error('No se pudo cargar el menú'),
    });
  }
}

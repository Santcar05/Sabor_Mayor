import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../../shared/services/cart.service';
import { ButtonComponent } from '../../../components/ui/button/button';

type Fulfillment = 'PICKUP' | 'DELIVERY' | 'DINE_IN';

@Component({
  selector: 'app-pedidos-inicio-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ButtonComponent],
  templateUrl: './pedidos-inicio-page.html',
  styleUrl: './pedidos-inicio-page.scss',
})
export class PedidosInicioPageComponent {
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  protected readonly selected = signal<Fulfillment>(this.cart.fulfillmentType());
  protected readonly address = signal(this.cart.snapshot().deliveryAddress ?? '');
  protected readonly tableId = signal(this.cart.snapshot().tableId ?? '');
  protected readonly count = this.cart.count;

  protected readonly options: { key: Fulfillment; title: string; desc: string; icon: string; eta: string }[] = [
    { key: 'PICKUP', title: 'Recoger', desc: 'Pasa por tu pedido al restaurante', icon: '🥡', eta: 'Listo en ~25 min' },
    { key: 'DELIVERY', title: 'Domicilio', desc: 'Te lo llevamos a tu puerta', icon: '🛵', eta: 'Entrega en ~45 min' },
    { key: 'DINE_IN', title: 'En mesa', desc: 'Pide desde tu mesa con el QR', icon: '🍽', eta: 'Servicio inmediato' },
  ];

  protected select(key: Fulfillment): void {
    this.selected.set(key);
  }

  protected continue(): void {
    this.cart.setFulfillment(this.selected(), {
      deliveryAddress: this.selected() === 'DELIVERY' ? this.address().trim() : undefined,
      tableId: this.selected() === 'DINE_IN' ? this.tableId().trim() : undefined,
    });
    this.router.navigate([this.count() > 0 ? '/pedidos/carrito' : '/carta']);
  }
}

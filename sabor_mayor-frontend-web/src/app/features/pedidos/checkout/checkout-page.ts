import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { CartService } from '../../../shared/services/cart.service';
import { AuthService } from '../../../shared/services/auth.service';
import { OrderService } from '../../../shared/services/order.service';
import { PaymentService } from '../../../shared/services/payment.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { CreateOrderRequest } from '../../../shared/models/order.model';
import { PaymentMethod } from '../../../shared/models/payment.model';
import { ButtonComponent } from '../../../components/ui/button/button';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonComponent, CopCurrencyPipe],
  templateUrl: './checkout-page.html',
  styleUrl: './checkout-page.scss',
})
export class CheckoutPageComponent {
  protected readonly cart = inject(CartService);
  private readonly auth = inject(AuthService);
  private readonly orders = inject(OrderService);
  private readonly payments = inject(PaymentService);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly isAuth = computed(() => this.auth.isAuthenticated());
  protected readonly submitting = signal(false);

  protected readonly method = signal<PaymentMethod>('CARD');
  protected readonly methods: { key: PaymentMethod; label: string; icon: string }[] = [
    { key: 'CARD', label: 'Tarjeta', icon: '💳' },
    { key: 'PSE', label: 'PSE', icon: '🏦' },
    { key: 'WALLET', label: 'Nequi / Wallet', icon: '📱' },
    { key: 'CASH', label: 'Efectivo', icon: '💵' },
  ];

  // Datos de tarjeta (mock: nunca se envían crudos, se traducen a un token).
  protected readonly cardName = signal('');
  protected readonly cardNumber = signal('');
  protected readonly cardExp = signal('');
  protected readonly cardCvc = signal('');
  protected readonly simulateDecline = signal(false);

  protected readonly instructions = signal('');
  protected readonly acceptedTerms = signal(false);

  protected readonly tipPercent = signal(10);
  protected readonly tipPresets = [0, 10, 15, 20];
  protected readonly tip = computed(() => Math.round((this.cart.subtotal() * this.tipPercent()) / 100));
  protected readonly total = computed(() => this.cart.subtotal() + this.tip());

  protected setMethod(m: PaymentMethod): void {
    this.method.set(m);
  }
  protected setTip(p: number): void {
    this.tipPercent.set(p);
  }

  protected placeOrder(): void {
    if (this.cart.isEmpty()) {
      this.notify.warning('Tu carrito está vacío');
      return;
    }
    if (!this.isAuth()) {
      this.notify.info('Inicia sesión para completar tu pedido');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/pedidos/checkout' } });
      return;
    }
    if (!this.acceptedTerms()) {
      this.notify.warning('Debes aceptar los términos y condiciones');
      return;
    }

    const snapshot = this.cart.snapshot();
    const orderReq: CreateOrderRequest = {
      type: snapshot.fulfillmentType,
      tableId: snapshot.tableId || undefined,
      deliveryAddress: snapshot.deliveryAddress || undefined,
      notes: this.instructions().trim() || undefined,
      items: this.cart.items().map((i) => ({
        dishId: i.dishId,
        quantity: i.quantity,
        notes: i.notes,
      })),
    };

    this.submitting.set(true);
    this.orders
      .place(orderReq)
      .pipe(
        switchMap((order) =>
          this.payments
            .pay({
              orderId: order.id,
              amount: this.cart.subtotal(),
              tip: this.tip(),
              method: this.method(),
              paymentMethodToken: this.token(),
            })
            .pipe(switchMap(() => [order])),
        ),
      )
      .subscribe({
        next: (order) => {
          this.submitting.set(false);
          this.cart.clear();
          this.notify.success('¡Pedido confirmado! Sigue su preparación en vivo.', 'Gracias');
          this.router.navigate(['/pedidos', order.id, 'rastreo']);
        },
        error: (err) => {
          this.submitting.set(false);
          const detail = err?.error?.detail ?? 'No se pudo procesar el pago. Intenta de nuevo.';
          this.notify.error(detail);
        },
      });
  }

  /** Traduce el método a un token para el gateway mock del backend. */
  private token(): string {
    if (this.simulateDecline()) return 'tok_fail';
    switch (this.method()) {
      case 'CARD':
        return 'tok_visa';
      case 'PSE':
        return 'tok_pse';
      case 'WALLET':
        return 'tok_wallet';
      case 'CASH':
        return 'tok_cash';
    }
  }
}

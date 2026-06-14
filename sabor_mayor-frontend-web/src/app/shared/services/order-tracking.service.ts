import { Injectable, inject } from '@angular/core';
import { Observable, switchMap, takeWhile, timer } from 'rxjs';
import { Order } from '../models/order.model';
import { OrderService } from './order.service';

/**
 * Seguimiento de pedido en "tiempo real".
 *
 * El backend emite por WebSocket STOMP (/ws → /topic/orders/{id}), pero para
 * mantener compatibilidad con SSR y cero dependencias extra, aquí usamos
 * polling sobre GET /api/orders/{id} cada `intervalMs`. La firma es un
 * Observable<Order>, de modo que migrar a STOMP en el futuro no cambia la UI.
 */
@Injectable({ providedIn: 'root' })
export class OrderTrackingService {
  private readonly orders = inject(OrderService);

  /** Emite el pedido periódicamente hasta que llega a un estado terminal. */
  track(orderId: string, intervalMs = 5000): Observable<Order> {
    return timer(0, intervalMs).pipe(
      switchMap(() => this.orders.getById(orderId)),
      takeWhile((order) => !this.isTerminal(order.status), true),
    );
  }

  private isTerminal(status: Order['status']): boolean {
    return status === 'PAID' || status === 'DELIVERED' || status === 'CANCELLED';
  }
}

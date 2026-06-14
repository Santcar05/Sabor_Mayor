import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreateOrderRequest, Order } from '../models/order.model';

/** order-service: carrito→pedido, historial y seguimiento. */
@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly api = inject(ApiService);

  place(body: CreateOrderRequest): Observable<Order> {
    return this.api.post<Order>('/api/orders', body);
  }

  myOrders(): Observable<Order[]> {
    return this.api.get<Order[]>('/api/orders/me');
  }

  getById(id: string): Observable<Order> {
    return this.api.get<Order>(`/api/orders/${id}`);
  }

  cancel(id: string): Observable<Order> {
    return this.api.post<Order>(`/api/orders/${id}/cancel`);
  }
}

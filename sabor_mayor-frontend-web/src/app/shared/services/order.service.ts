import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreateOrderRequest, Order, OrderStatus } from '../models/order.model';

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

  /** Admin/staff: todos los pedidos con filtros opcionales. */
  allOrders(params?: { statuses?: OrderStatus[]; page?: number; size?: number }): Observable<Order[]> {
    if (!params) return this.api.get<Order[]>('/api/orders');
    const { statuses, page, size } = params;
    return this.api.get<Order[]>('/api/orders', {
      ...(statuses?.length ? { statuses: statuses.join(',') } : {}),
      ...(page != null ? { page } : {}),
      ...(size != null ? { size } : {}),
    });
  }

  /** Staff/admin: cambia el estado de un pedido. */
  updateStatus(id: string, status: OrderStatus): Observable<Order> {
    return this.api.patch<Order>(`/api/orders/${id}/status`, { status });
  }
}

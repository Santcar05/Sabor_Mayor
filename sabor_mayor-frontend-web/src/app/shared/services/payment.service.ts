import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreatePaymentRequest, Payment } from '../models/payment.model';

/** payment-service. El gateway de pago es mock en dev: token "tok_fail" es rechazado. */
@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly api = inject(ApiService);

  pay(body: CreatePaymentRequest): Observable<Payment> {
    return this.api.post<Payment>('/api/payments', body);
  }

  myPayments(): Observable<Payment[]> {
    return this.api.get<Payment[]>('/api/payments/me');
  }

  byOrder(orderId: string): Observable<Payment> {
    return this.api.get<Payment>(`/api/payments/by-order/${orderId}`);
  }
}

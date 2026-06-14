import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Address,
  CustomerProfile,
  PaymentMethodRef,
  UpdateProfileRequest,
} from '../models/user.model';

/** user-service: perfil, direcciones y métodos de pago del cliente. */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  getProfile(): Observable<CustomerProfile> {
    return this.api.get<CustomerProfile>('/api/users/me');
  }

  updateProfile(body: UpdateProfileRequest): Observable<CustomerProfile> {
    return this.api.put<CustomerProfile>('/api/users/me', body);
  }

  // Direcciones
  getAddresses(): Observable<Address[]> {
    return this.api.get<Address[]>('/api/users/me/addresses');
  }
  addAddress(body: Partial<Address>): Observable<Address> {
    return this.api.post<Address>('/api/users/me/addresses', body);
  }
  updateAddress(id: string, body: Partial<Address>): Observable<Address> {
    return this.api.put<Address>(`/api/users/me/addresses/${id}`, body);
  }
  deleteAddress(id: string): Observable<void> {
    return this.api.delete<void>(`/api/users/me/addresses/${id}`);
  }

  // Métodos de pago (solo referencias tokenizadas)
  getPaymentMethods(): Observable<PaymentMethodRef[]> {
    return this.api.get<PaymentMethodRef[]>('/api/users/me/payment-methods');
  }
  addPaymentMethod(body: Partial<PaymentMethodRef>): Observable<PaymentMethodRef> {
    return this.api.post<PaymentMethodRef>('/api/users/me/payment-methods', body);
  }
  deletePaymentMethod(id: string): Observable<void> {
    return this.api.delete<void>(`/api/users/me/payment-methods/${id}`);
  }
}

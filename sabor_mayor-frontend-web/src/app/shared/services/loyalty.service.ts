import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LoyaltyAccount, LoyaltyTransaction } from '../models/loyalty.model';

/** loyalty-service: Sabor Points. */
@Injectable({ providedIn: 'root' })
export class LoyaltyService {
  private readonly api = inject(ApiService);

  getAccount(): Observable<LoyaltyAccount> {
    return this.api.get<LoyaltyAccount>('/api/loyalty/me');
  }

  getTransactions(): Observable<LoyaltyTransaction[]> {
    return this.api.get<LoyaltyTransaction[]>('/api/loyalty/me/transactions');
  }

  redeem(points: number, description?: string): Observable<LoyaltyAccount> {
    return this.api.post<LoyaltyAccount>('/api/loyalty/me/redeem', { points, description });
  }
}

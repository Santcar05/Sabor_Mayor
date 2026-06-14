import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { RestaurantTable } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class TableService {
  private readonly api = inject(ApiService);

  getAllTables(): Observable<RestaurantTable[]> {
    return this.api.get<RestaurantTable[]>('/api/tables');
  }
}

import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  AvailabilitySlot,
  CreateReservationRequest,
  Reservation,
} from '../models/reservation.model';

/** reservation-service. */
@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly api = inject(ApiService);

  getAvailability(date: string, partySize: number): Observable<AvailabilitySlot[]> {
    return this.api.get<AvailabilitySlot[]>('/api/reservations/availability', { date, partySize });
  }

  create(body: CreateReservationRequest): Observable<Reservation> {
    return this.api.post<Reservation>('/api/reservations', body);
  }

  myReservations(): Observable<Reservation[]> {
    return this.api.get<Reservation[]>('/api/reservations/me');
  }

  cancel(id: string): Observable<void> {
    return this.api.delete<void>(`/api/reservations/${id}`);
  }

  /** Admin: todas las reservas con filtros opcionales. */
  allReservations(params?: { status?: string; date?: string }): Observable<Reservation[]> {
    return this.api.get<Reservation[]>('/api/reservations/all', params);
  }
}

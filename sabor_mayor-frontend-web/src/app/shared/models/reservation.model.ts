export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

/** Slot devuelto por GET /api/reservations/availability */
export interface AvailabilitySlot {
  time: string; // "20:00:00"
  remainingSeats: number;
  fitsParty: boolean;
}

export interface PreOrderItem {
  dishId: string;
  quantity: number;
}

export interface Reservation {
  id: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm
  partySize: number;
  status: ReservationStatus;
  depositRequired: boolean;
  depositAmount: number;
  occasion?: string;
  specialRequests?: string;
  createdAt: string;
}

/** Cuerpo de POST /api/reservations */
export interface CreateReservationRequest {
  date: string;
  time: string;
  partySize: number;
  occasion?: string;
  specialRequests?: string;
  preOrderItems?: PreOrderItem[];
}

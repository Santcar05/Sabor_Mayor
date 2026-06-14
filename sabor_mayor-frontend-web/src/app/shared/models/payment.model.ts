export type PaymentMethod = 'CARD' | 'CASH' | 'PSE' | 'WALLET';

export type PaymentStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'FAILED'
  | 'PARTIALLY_REFUNDED'
  | 'REFUNDED';

export interface Payment {
  id: string;
  orderId: string;
  customerId: string;
  amount: number;
  tip: number;
  method: PaymentMethod;
  gateway: string;
  gatewayReference: string;
  status: PaymentStatus;
  refundedAmount: number;
}

/** Cuerpo de POST /api/payments */
export interface CreatePaymentRequest {
  orderId: string;
  amount: number;
  tip?: number;
  method: PaymentMethod;
  paymentMethodToken: string;
}

export type OrderType = 'DINE_IN' | 'DELIVERY' | 'PICKUP';

export type OrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'IN_KITCHEN'
  | 'READY'
  | 'SERVED'
  | 'DELIVERED'
  | 'PAID'
  | 'CANCELLED';

export type KitchenStation = 'FRIOS' | 'CALIENTES' | 'POSTRES' | 'BAR';
export type OrderItemStatus = 'PENDING' | 'IN_PROGRESS' | 'READY' | 'SERVED';

export interface OrderItem {
  id: string;
  dishId: string;
  dishName: string;
  unitPrice: number;
  quantity: number;
  notes?: string;
  station: KitchenStation;
  itemStatus: OrderItemStatus;
}

export interface Order {
  id: string;
  customerId: string;
  type: OrderType;
  status: OrderStatus;
  tableId?: string;
  deliveryAddress?: string;
  notes?: string;
  items: OrderItem[];
  subtotal: number;
  tip: number;
  total: number;
  paymentId?: string;
  createdAt: string;
}

export type TableStatus = 'LIBRE' | 'OCUPADA' | 'RESERVADA' | 'MANTENIMIENTO';

export interface RestaurantTable {
  id: string;
  number: number;
  capacity: number;
  status: TableStatus;
  qrToken: string;
}

/** Cuerpo de POST /api/orders */
export interface CreateOrderRequest {
  type: OrderType;
  tableId?: string;
  deliveryAddress?: string;
  notes?: string;
  fromCart?: boolean;
  items?: { dishId: string; quantity: number; notes?: string }[];
}

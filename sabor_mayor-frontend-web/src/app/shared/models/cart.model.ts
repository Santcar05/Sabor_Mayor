/**
 * Carrito del lado cliente (persistido en localStorage y sincronizable con
 * el backend cuando el usuario inicia sesión: POST /api/cart/items).
 */
export interface CartItem {
  dishId: string;
  name: string;
  slug: string;
  unitPrice: number;
  imageUrl: string;
  quantity: number;
  notes?: string;
}

export interface CartState {
  items: CartItem[];
  /** DINE_IN | DELIVERY | PICKUP — define el flujo de checkout. */
  fulfillmentType: 'DINE_IN' | 'DELIVERY' | 'PICKUP';
  deliveryAddress?: string;
  tableId?: string;
}

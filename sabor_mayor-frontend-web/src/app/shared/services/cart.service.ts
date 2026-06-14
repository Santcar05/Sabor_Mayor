import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CartItem, CartState } from '../models/cart.model';
import { MenuDish } from '../models/dish.model';
import { NotificationService } from './notification.service';

const STORAGE_KEY = 'sm_cart';

const EMPTY: CartState = { items: [], fulfillmentType: 'PICKUP' };

/**
 * Carrito del lado cliente con persistencia en localStorage (SSR-safe).
 * Expone signals para que la UI reaccione (contador, totales).
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly notify = inject(NotificationService);

  private readonly state = signal<CartState>(this.load());

  readonly items = computed(() => this.state().items);
  readonly fulfillmentType = computed(() => this.state().fulfillmentType);
  readonly count = computed(() => this.state().items.reduce((n, i) => n + i.quantity, 0));
  readonly subtotal = computed(() =>
    this.state().items.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0),
  );
  readonly isEmpty = computed(() => this.state().items.length === 0);
  readonly snapshot = computed(() => this.state());

  add(dish: MenuDish, quantity = 1, notes?: string): void {
    this.state.update((s) => {
      const items = [...s.items];
      const idx = items.findIndex((i) => i.dishId === dish.id && (i.notes ?? '') === (notes ?? ''));
      if (idx >= 0) {
        items[idx] = { ...items[idx], quantity: items[idx].quantity + quantity };
      } else {
        items.push({
          dishId: dish.id,
          name: dish.name,
          slug: dish.slug,
          unitPrice: dish.price,
          imageUrl: dish.imageUrl,
          quantity,
          notes,
        });
      }
      return { ...s, items };
    });
    this.persist();
    this.notify.success(`${dish.name} añadido al pedido`, 'Carrito');
  }

  setQuantity(dishId: string, quantity: number, notes?: string): void {
    if (quantity <= 0) return this.remove(dishId, notes);
    this.state.update((s) => ({
      ...s,
      items: s.items.map((i) =>
        i.dishId === dishId && (i.notes ?? '') === (notes ?? '') ? { ...i, quantity } : i,
      ),
    }));
    this.persist();
  }

  increment(item: CartItem): void {
    this.setQuantity(item.dishId, item.quantity + 1, item.notes);
  }
  decrement(item: CartItem): void {
    this.setQuantity(item.dishId, item.quantity - 1, item.notes);
  }

  updateNotes(dishId: string, oldNotes: string | undefined, newNotes: string): void {
    this.state.update((s) => ({
      ...s,
      items: s.items.map((i) =>
        i.dishId === dishId && (i.notes ?? '') === (oldNotes ?? '') ? { ...i, notes: newNotes } : i,
      ),
    }));
    this.persist();
  }

  remove(dishId: string, notes?: string): void {
    this.state.update((s) => ({
      ...s,
      items: s.items.filter((i) => !(i.dishId === dishId && (i.notes ?? '') === (notes ?? ''))),
    }));
    this.persist();
  }

  setFulfillment(type: CartState['fulfillmentType'], opts?: { deliveryAddress?: string; tableId?: string }): void {
    this.state.update((s) => ({
      ...s,
      fulfillmentType: type,
      deliveryAddress: opts?.deliveryAddress,
      tableId: opts?.tableId,
    }));
    this.persist();
  }

  clear(): void {
    this.state.set({ ...EMPTY });
    this.persist();
  }

  private load(): CartState {
    if (!this.isBrowser) return { ...EMPTY };
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? { ...EMPTY, ...JSON.parse(raw) } : { ...EMPTY };
    } catch {
      return { ...EMPTY };
    }
  }

  private persist(): void {
    if (!this.isBrowser) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state()));
  }
}

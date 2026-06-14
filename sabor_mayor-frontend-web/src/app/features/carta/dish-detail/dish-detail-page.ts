import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { MenuService } from '../../../shared/services/menu.service';
import { CartService } from '../../../shared/services/cart.service';
import { FavoritesService } from '../../../shared/services/favorites.service';
import { MenuDish } from '../../../shared/models/dish.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { PriceDisplayComponent } from '../../../components/ui/price-display/price-display';
import { DishCardComponent } from '../../../components/dish-card/dish-card';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

interface DetailState {
  dish: MenuDish | null;
  pairings: MenuDish[];
  error: boolean;
}

@Component({
  selector: 'app-dish-detail-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    LoadingSpinnerComponent,
    ButtonComponent,
    PriceDisplayComponent,
    DishCardComponent,
    CopCurrencyPipe,
  ],
  templateUrl: './dish-detail-page.html',
  styleUrl: './dish-detail-page.scss',
})
export class DishDetailPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly menu = inject(MenuService);
  private readonly cart = inject(CartService);
  private readonly favorites = inject(FavoritesService);

  protected readonly quantity = signal(1);
  protected readonly notes = signal('');
  protected readonly nutritionOpen = signal(false);

  protected readonly state = toSignal(
    this.route.paramMap.pipe(
      map((p) => p.get('slug') ?? ''),
      switchMap((slug): Observable<DetailState> =>
        this.menu.getDishBySlug(slug).pipe(
          switchMap((dish): Observable<DetailState> =>
            dish.pairingIds.length
              ? this.menu.getDishesByIds(dish.pairingIds).pipe(
                  map((pairings): DetailState => ({ dish, pairings, error: false })),
                  catchError(() => of<DetailState>({ dish, pairings: [], error: false })),
                )
              : of<DetailState>({ dish, pairings: [], error: false }),
          ),
          catchError(() => of<DetailState>({ dish: null, pairings: [], error: true })),
        ),
      ),
    ),
    { initialValue: { dish: null, pairings: [], error: false } as DetailState },
  );

  protected isFavorite(slug: string): boolean {
    return this.favorites.isFavorite(slug);
  }
  protected toggleFavorite(slug: string): void {
    this.favorites.toggle(slug);
  }

  protected inc(): void {
    this.quantity.update((q) => Math.min(q + 1, 20));
  }
  protected dec(): void {
    this.quantity.update((q) => Math.max(q - 1, 1));
  }

  protected addToCart(dish: MenuDish): void {
    this.cart.add(dish, this.quantity(), this.notes().trim() || undefined);
  }
}

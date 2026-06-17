import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { MenuService } from '../../../shared/services/menu.service';
import { CartService } from '../../../shared/services/cart.service';
import { FavoritesService } from '../../../shared/services/favorites.service';
import { AuthService } from '../../../shared/services/auth.service';
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
export class DishDetailPageComponent implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly menu = inject(MenuService);
  private readonly cart = inject(CartService);
  private readonly favorites = inject(FavoritesService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly quantity = signal(1);
  protected readonly notes = signal('');
  protected readonly nutritionOpen = signal(false);
  protected readonly justLiked = signal(false);
  protected readonly showAuthPrompt = signal(false);
  private dismissTimer: ReturnType<typeof setTimeout> | null = null;

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
    if (!this.auth.isAuthenticated()) {
      this.showAuthPrompt.set(true);
      if (this.dismissTimer !== null) clearTimeout(this.dismissTimer);
      this.dismissTimer = setTimeout(() => this.showAuthPrompt.set(false), 4000);
      return;
    }
    const wasNotFavorite = !this.favorites.isFavorite(slug);
    this.favorites.toggle(slug);
    if (wasNotFavorite) {
      this.justLiked.set(true);
      setTimeout(() => this.justLiked.set(false), 700);
    }
  }

  protected navigateToLogin(): void {
    if (this.dismissTimer !== null) clearTimeout(this.dismissTimer);
    this.showAuthPrompt.set(false);
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
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

  ngOnDestroy(): void {
    if (this.dismissTimer !== null) clearTimeout(this.dismissTimer);
  }
}

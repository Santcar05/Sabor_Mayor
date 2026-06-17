import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  OnDestroy,
  signal,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MenuDish } from '../../shared/models/dish.model';
import { CartService } from '../../shared/services/cart.service';
import { FavoritesService } from '../../shared/services/favorites.service';
import { AuthService } from '../../shared/services/auth.service';
import { PriceDisplayComponent } from '../ui/price-display/price-display';

interface TagBadge {
  label: string;
  kind: 'vegetarian' | 'spicy' | 'signature' | 'neutral';
}

@Component({
  selector: 'app-dish-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PriceDisplayComponent],
  templateUrl: './dish-card.html',
  styleUrl: './dish-card.scss',
})
export class DishCardComponent implements OnDestroy {
  private readonly cart = inject(CartService);
  private readonly favorites = inject(FavoritesService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  dish = input.required<MenuDish>();
  showActions = input(true);

  protected isFavorite = computed(() => this.favorites.isFavorite(this.dish().slug));
  protected readonly justLiked = signal(false);
  protected readonly showAuthPrompt = signal(false);
  private dismissTimer: ReturnType<typeof setTimeout> | null = null;

  protected badges = computed<TagBadge[]>(() =>
    this.dish().tags.map((tag) => {
      const t = tag.toLowerCase();
      if (t.includes('veg')) return { label: tag, kind: 'vegetarian' };
      if (t.includes('pic') || t.includes('spicy') || t.includes('ají') || t.includes('aji'))
        return { label: tag, kind: 'spicy' };
      if (t.includes('signature') || t.includes('autor') || t.includes('exclus') || t.includes('ancestral'))
        return { label: tag, kind: 'signature' };
      return { label: tag, kind: 'neutral' };
    }),
  );

  protected addToCart(): void {
    this.cart.add(this.dish());
  }

  protected toggleFavorite(): void {
    if (!this.auth.isAuthenticated()) {
      this.showAuthPrompt.set(true);
      if (this.dismissTimer !== null) clearTimeout(this.dismissTimer);
      this.dismissTimer = setTimeout(() => this.showAuthPrompt.set(false), 4000);
      return;
    }
    const wasNotFavorite = !this.favorites.isFavorite(this.dish().slug);
    this.favorites.toggle(this.dish().slug);
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

  protected onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = 'assets/images/dish-placeholder.svg';
  }

  ngOnDestroy(): void {
    if (this.dismissTimer !== null) clearTimeout(this.dismissTimer);
  }
}

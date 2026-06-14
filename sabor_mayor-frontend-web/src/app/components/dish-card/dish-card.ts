import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MenuDish } from '../../shared/models/dish.model';
import { CartService } from '../../shared/services/cart.service';
import { FavoritesService } from '../../shared/services/favorites.service';
import { PriceDisplayComponent } from '../ui/price-display/price-display';

interface TagBadge {
  label: string;
  kind: 'vegetarian' | 'spicy' | 'signature' | 'neutral';
}

/** Tarjeta de plato reutilizable (carta, destacados, favoritos, relacionados). */
@Component({
  selector: 'app-dish-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PriceDisplayComponent],
  templateUrl: './dish-card.html',
  styleUrl: './dish-card.scss',
})
export class DishCardComponent {
  private readonly cart = inject(CartService);
  private readonly favorites = inject(FavoritesService);

  dish = input.required<MenuDish>();
  showActions = input(true);

  protected isFavorite = computed(() => this.favorites.isFavorite(this.dish().slug));

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
    this.favorites.toggle(this.dish().slug);
  }

  protected onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = 'assets/images/dish-placeholder.svg';
  }
}

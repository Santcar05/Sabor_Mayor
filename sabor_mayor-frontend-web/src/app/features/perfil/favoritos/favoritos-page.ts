import { ChangeDetectionStrategy, Component, inject, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap, of, catchError } from 'rxjs';
import { FavoritesService } from '../../../shared/services/favorites.service';
import { MenuService } from '../../../shared/services/menu.service';
import { DishCardComponent } from '../../../components/dish-card/dish-card';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';

@Component({
  selector: 'app-favoritos-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DishCardComponent, LoadingSpinnerComponent, ButtonComponent],
  templateUrl: './favoritos-page.html',
  styleUrl: './favoritos-page.scss',
})
export class FavoritosPageComponent {
  private readonly fav = inject(FavoritesService);
  private readonly menu = inject(MenuService);

  protected readonly slugs = computed(() => this.fav.favorites());

  protected readonly dishes = toSignal(
    this.menu.getCategories().pipe(
      switchMap(() => {
        const ids = this.fav.favorites();
        if (ids.length === 0) return of([]);
        return this.menu.getDishes({}).pipe(
          switchMap((dishes) => of(dishes.filter((d: { slug: string }) => ids.includes(d.slug)))),
          catchError(() => of([])),
        );
      }),
    ),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this.dishes() === undefined);
  protected readonly items = computed(() => this.dishes() ?? []);
}

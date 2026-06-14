import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { trigger, transition, style, animate, query, stagger } from '@angular/animations';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { MenuService } from '../../shared/services/menu.service';
import { MenuDish } from '../../shared/models/dish.model';
import { DishCardComponent } from '../../components/dish-card/dish-card';
import { LoadingSpinnerComponent } from '../../components/ui/loading-spinner/loading-spinner';

type DietFilter = 'todos' | 'vegetarian' | 'spicy' | 'signature';

/** Carta completa conectada a menu-service (GET /api/menu/dishes y /categories). */
@Component({
  selector: 'app-carta-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DishCardComponent, LoadingSpinnerComponent],
  animations: [
    trigger('grid', [
      transition('* => *', [
        query(
          ':enter',
          [
            style({ opacity: 0, transform: 'translateY(28px)' }),
            stagger('60ms', [
              animate('480ms cubic-bezier(0.16,1,0.3,1)', style({ opacity: 1, transform: 'none' })),
            ]),
          ],
          { optional: true },
        ),
      ]),
    ]),
  ],
  templateUrl: './carta-page.html',
  styleUrl: './carta-page.scss',
})
export class CartaPageComponent {
  private readonly menu = inject(MenuService);

  protected readonly categories = toSignal(
    this.menu.getCategories().pipe(catchError(() => of([]))),
    { initialValue: [] },
  );

  private readonly allDishes = toSignal(
    this.menu.getDishes({ onlyAvailable: false }).pipe(catchError(() => of(null))),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this.allDishes() === undefined);
  protected readonly loadError = computed(() => this.allDishes() === null);

  protected readonly selectedCategory = signal<string>('todos');
  protected readonly diet = signal<DietFilter>('todos');
  protected readonly search = signal<string>('');

  protected readonly diets: { key: DietFilter; label: string }[] = [
    { key: 'todos', label: 'Todos' },
    { key: 'signature', label: 'Signature' },
    { key: 'vegetarian', label: 'Vegetariano' },
    { key: 'spicy', label: 'Picante' },
  ];

  protected readonly filtered = computed<MenuDish[]>(() => {
    const dishes = this.allDishes();
    if (!dishes) return [];
    const cat = this.selectedCategory();
    const diet = this.diet();
    const q = this.search().toLowerCase().trim();

    return dishes.filter((d) => {
      const matchCat = cat === 'todos' || d.categorySlug === cat;
      const matchDiet =
        diet === 'todos' || d.tags.some((t) => this.matchesDiet(t, diet));
      const matchSearch =
        !q || d.name.toLowerCase().includes(q) || d.description.toLowerCase().includes(q);
      return matchCat && matchDiet && matchSearch;
    });
  });

  protected setCategory(slug: string): void {
    this.selectedCategory.set(slug);
  }
  protected setDiet(diet: DietFilter): void {
    this.diet.set(diet);
  }
  protected onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected trackBySlug(_: number, dish: MenuDish): string {
    return dish.slug;
  }

  private matchesDiet(tag: string, diet: DietFilter): boolean {
    const t = tag.toLowerCase();
    if (diet === 'vegetarian') return t.includes('veg');
    if (diet === 'spicy') return t.includes('pic') || t.includes('ají') || t.includes('aji');
    if (diet === 'signature')
      return t.includes('signature') || t.includes('autor') || t.includes('ancestral') || t.includes('exclus');
    return false;
  }
}

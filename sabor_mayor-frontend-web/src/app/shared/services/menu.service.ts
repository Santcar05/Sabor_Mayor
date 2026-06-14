import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { ApiService } from './api.service';
import { MenuCategory, MenuDish } from '../models/dish.model';

/** menu-service: carta pública (cacheada en Redis del lado backend). */
@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly api = inject(ApiService);
  private categories$?: Observable<MenuCategory[]>;

  getCategories(): Observable<MenuCategory[]> {
    // Cachea en memoria durante la sesión; la carta cambia muy poco.
    this.categories$ ??= this.api
      .get<MenuCategory[]>('/api/menu/categories')
      .pipe(shareReplay(1));
    return this.categories$;
  }

  getDishes(opts?: { category?: string; tag?: string; onlyAvailable?: boolean }): Observable<MenuDish[]> {
    return this.api.get<MenuDish[]>('/api/menu/dishes', {
      category: opts?.category && opts.category !== 'todos' ? opts.category : undefined,
      tag: opts?.tag,
      onlyAvailable: opts?.onlyAvailable,
    });
  }

  getDishBySlug(slug: string): Observable<MenuDish> {
    return this.api.get<MenuDish>(`/api/menu/dishes/${slug}`);
  }

  getDishesByIds(ids: string[]): Observable<MenuDish[]> {
    return this.api.get<MenuDish[]>('/api/menu/dishes/by-ids', { ids });
  }
}

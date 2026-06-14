import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const STORAGE_KEY = 'sm_favorites';

/**
 * Favoritos del lado cliente (el backend no expone endpoint de favoritos).
 * Guarda los slugs de los platos marcados con ❤️.
 */
@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly slugs = signal<string[]>(this.load());

  readonly favorites = computed(() => this.slugs());
  readonly count = computed(() => this.slugs().length);

  isFavorite(slug: string): boolean {
    return this.slugs().includes(slug);
  }

  toggle(slug: string): void {
    this.slugs.update((list) =>
      list.includes(slug) ? list.filter((s) => s !== slug) : [...list, slug],
    );
    this.persist();
  }

  private load(): string[] {
    if (!this.isBrowser) return [];
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  private persist(): void {
    if (this.isBrowser) localStorage.setItem(STORAGE_KEY, JSON.stringify(this.slugs()));
  }
}

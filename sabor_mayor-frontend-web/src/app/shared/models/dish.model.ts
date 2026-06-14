/**
 * Modelos de carta alineados con menu-service.
 * GET /api/menu/dishes y GET /api/menu/dishes/{slug}
 */
export interface MenuDish {
  id: string;
  name: string;
  slug: string;
  description: string;
  price: number;
  available: boolean;
  featured: boolean;
  imageUrl: string;
  prepMinutes: number;
  categorySlug: string;
  categoryName: string;
  tags: string[];
  allergens: string[];
  pairingIds: string[];
}

export interface MenuCategory {
  id: string;
  name: string;
  slug: string;
  description?: string;
  displayOrder: number;
}

/** Slugs de categoría usados como filtro en la UI. */
export type CategoryFilter = 'todos' | string;

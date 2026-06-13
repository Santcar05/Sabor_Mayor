export interface Dish {
  id: string;
  name: string;
  subtitle?: string;
  description: string;
  price: string;
  category: 'entradas' | 'fuertes' | 'postres' | 'cocteles';
  image: string;
  tags?: Array<{ label: string; type: 'vegetarian' | 'spicy' | 'signature' }>;
}

export type MenuCategory = 'todos' | 'entradas' | 'fuertes' | 'postres' | 'cocteles';

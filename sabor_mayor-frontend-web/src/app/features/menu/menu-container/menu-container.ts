import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate, query, stagger } from '@angular/animations';
import { MenuSearch } from '../menu-search/menu-search';
import { MenuFilters } from '../menu-filters/menu-filters';
import { MenuCard } from '../menu-card/menu-card';
import { Dish, MenuCategory } from '../models/dish';
import { PublicHeader } from '../../../components/public-header/public-header';

@Component({
  selector: 'app-menu-container',
  standalone: true,
  imports: [CommonModule, MenuSearch, MenuFilters, MenuCard, PublicHeader],
  animations: [
    // CORRECCIÓN: trigger recibe un arreglo [] en su segundo parámetro
    trigger('listStagger', [
      transition('* <=> *', [
        query(
          ':enter',
          [
            style({ opacity: 0, transform: 'translateY(30px)' }),
            stagger('80ms', [
              animate(
                '500ms cubic-bezier(0.25, 1, 0.5, 1)',
                style({ opacity: 1, transform: 'translateY(0)' }),
              ),
            ]),
          ],
          { optional: true },
        ),
      ]),
    ]),
  ],
  templateUrl: './menu-container.html',
  styleUrls: ['./menu-container.scss'],
})
export class MenuContainerComponent {
  // Estado Reactivo con Signals
  selectedCategory = signal<MenuCategory>('todos');
  searchQuery = signal<string>('');

  // Base de datos de platos con la narrativa gourmet requerida
  private readonly dishesDatabase = signal<Dish[]>([
    {
      id: 'd1',
      name: 'Ceviche de Maracuyá Molecular',
      subtitle: 'Reinterpretación pacífica',
      description:
        'Pesca del día curada en leche de tigre de maracuyá, esferificaciones de cilantro y aire de ají limo.',
      price: '$42.000',
      category: 'entradas',
      image: 'assets/images/menu/ceviche.jpg',
      tags: [
        { label: 'Ancestral', type: 'signature' },
        { label: 'Picante', type: 'spicy' },
      ],
    },
    {
      id: 'd2',
      name: 'Texturas de Maíz Criollo',
      subtitle: 'Homenaje al grano sagrado',
      description:
        'Tamal deconstruido, crocante de maíz morado, emulsión de choclo dulce y cenizas de cebolla.',
      price: '$36.000',
      category: 'entradas',
      image: 'assets/images/menu/texturas-maiz.jpg',
      tags: [{ label: 'Vegetariano', type: 'vegetarian' }],
    },
    {
      id: 'd3',
      name: 'Vacío en Madera de Olivo',
      subtitle: 'Fuego de la Pampa',
      description:
        'Corte madurado ahumado al momento en mesa, milhojas de papa nativa y chimichurri trufado.',
      price: '$89.000',
      category: 'fuertes',
      image: 'assets/images/menu/vacio-olivo.jpg',
      tags: [{ label: 'Fuego lento', type: 'signature' }],
    },
    {
      id: 'd4',
      name: 'Risotto de Quinoa y Setas',
      subtitle: 'Cosecha del altiplano',
      description:
        'Quinoa real cremosa con hongos silvestres del bosque andino y lascas de queso de oveja curado.',
      price: '$64.000',
      category: 'fuertes',
      image: 'assets/images/menu/risotto-quinoa.jpg',
      tags: [{ label: 'Vegetariano', type: 'vegetarian' }],
    },
    {
      id: 'd5',
      name: 'Volcán de Cacao Amazónico',
      subtitle: '85% Pureza Origen',
      description:
        'Bizcocho fluido de chocolate negro, helado artesanal de pimienta rosa y sal de Maras.',
      price: '$28.000',
      category: 'postres',
      image: 'assets/images/menu/volcan-cacao.jpg',
    },
    {
      id: 'd6',
      name: 'Clericó Mayor Élite',
      subtitle: 'Maceración de autor',
      description:
        'Destilado de agave premium infusionado con frutos del bosque nativos y burbujas de sidra artesanal.',
      price: '$34.000',
      category: 'cocteles',
      image: 'assets/images/menu/clerico-elite.jpg',
      tags: [{ label: 'Exclusivo', type: 'signature' }],
    },
  ]);

  // Selector Computado de Alta Eficiencia
  filteredDishes = computed(() => {
    const category = this.selectedCategory();
    const search = this.searchQuery().toLowerCase().trim();

    return this.dishesDatabase().filter((dish) => {
      const matchesCategory = category === 'todos' || dish.category === category;
      const matchesSearch =
        !search ||
        dish.name.toLowerCase().includes(search) ||
        dish.description.toLowerCase().includes(search) ||
        (dish.subtitle && dish.subtitle.toLowerCase().includes(search));

      return matchesCategory && matchesSearch;
    });
  });

  updateCategory(category: MenuCategory): void {
    this.selectedCategory.set(category);
  }

  updateSearch(query: string): void {
    this.searchQuery.set(query);
  }

  trackByDishId(index: number, item: Dish): string {
    return item.id;
  }
}

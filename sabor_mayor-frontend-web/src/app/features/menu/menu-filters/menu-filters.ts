import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuCategory } from '../models/dish';
@Component({
  selector: 'app-menu-filters',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './menu-filters.html',
  styleUrl: './menu-filters.scss',
})
export class MenuFilters {
  currentCategory = input.required<MenuCategory>();
  categoryChanged = output<MenuCategory>();

  readonly categories: Array<{ id: MenuCategory; label: string }> = [
    { id: 'todos', label: 'La Carta Completa' },
    { id: 'entradas', label: 'Entradas de la Tierra' },
    { id: 'fuertes', label: 'Platos Mayores' },
    { id: 'postres', label: 'Dulce Final' },
    { id: 'cocteles', label: 'Coctelería Ancestral' },
  ];

  selectCategory(id: MenuCategory): void {
    this.categoryChanged.emit(id);
  }
}

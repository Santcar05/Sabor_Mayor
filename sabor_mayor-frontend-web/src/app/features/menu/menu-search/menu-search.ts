import { Component, output, signal } from '@angular/core';
// Referenciar HTML y SCSS específicos para este componente
@Component({
  selector: 'app-menu-search',
  standalone: true,
  templateUrl: './menu-search.html',
  styleUrl: './menu-search.scss',
})
export class MenuSearch {
  searchChanged = output<string>();

  onInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchChanged.emit(value);
  }
}

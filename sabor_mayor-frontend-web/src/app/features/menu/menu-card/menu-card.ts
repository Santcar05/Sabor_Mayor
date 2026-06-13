import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dish } from '../models/dish';

@Component({
  selector: 'app-menu-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './menu-card.html',
  styleUrl: './menu-card.scss',
})
export class MenuCard {
  dish = input.required<Dish>();
}

import { Component } from '@angular/core';

interface Dish {
  name: string;
  description: string;
  price: string;
  image: string;
  tag: string;
  tagType: 'default' | 'vegetarian' | 'spicy';
}

@Component({
  selector: 'app-featured-dishes-section',
  standalone: true,
  imports: [],
  templateUrl: './featured-dishes-section.html',
  styleUrl: './featured-dishes-section.scss',
})
export class FeaturedDishesSectionComponent {
  dishes: Dish[] = [
    {
      name: 'Ceviche Ancestral',
      description:
        'Pescado fresco marinado en limón fresco, cilantro silvestre y ají limeño. Una reinterpretación clásica de la joya del Pacífico.',
      price: '$34.00',
      image: 'assets/ceviche.jpg',
      tag: 'Pescado fresco',
      tagType: 'vegetarian',
    },
    {
      name: 'Causa Limeña Premium',
      description:
        'Capas delicadas de papas amarillas espolvoreadas, relleno de pulpo a baja temperatura y aguacate Hass premium.',
      price: '$28.00',
      image: 'assets/Limenia.jpg',
      tag: 'Especialidad',
      tagType: 'default',
    },
    {
      name: 'Ají de Gallina Contemporáneo',
      description:
        'Pechuga de pollo orgánico desmenuzada en una salsa de ají amarillo cremosa con pan negro artesanal.',
      price: '$32.00',
      image: 'assets/Aji_de_Gallina_Contemporaneo.jpg',
      tag: 'Picante',
      tagType: 'spicy',
    },
  ];

  get featuredDish(): Dish {
    return this.dishes[0];
  }

  get sideDishes(): Dish[] {
    return this.dishes.slice(1);
  }
}

import { Component } from '@angular/core';
import { NgForOf } from '@angular/common';

interface Testimonial {
  text: string;
  author: string;
  role: string;
  rating: number;
}

@Component({
  selector: 'app-testimonials-section',
  standalone: true,
  imports: [NgForOf],
  templateUrl: './testimonials-section.html',
  styleUrl: './testimonials-section.scss',
})
export class TestimonialsSectionComponent {
  testimonials: Testimonial[] = [
    {
      text: 'Una experiencia verdaderamente transcendental. Cada plato es una obra maestra. El chef entiende la profundidad de nuestras tradiciones culinarias.',
      author: 'María García',
      role: 'Chef reconocida internacionalmente',
      rating: 5,
    },
    {
      text: 'No es solo comida, es poesía en el plato. La atención al detalle, desde la presentación hasta el sabor, es impecable. Volveré con frecuencia.',
      author: 'Carlos Mendoza',
      role: 'Crítico gastronómico',
      rating: 5,
    },
    {
      text: 'El ambiente es elegante sin ser pretencioso. El servicio impecable. Y la comida... simplemente incomparable. Sabor Mayor es un destino culinario obligatorio.',
      author: 'Julia Rodríguez',
      role: 'Viajera de gastronomía',
      rating: 5,
    },
  ];

  getStars(rating: number): string {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }
}

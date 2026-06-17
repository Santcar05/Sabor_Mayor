import { Component, computed } from '@angular/core';

interface Testimonial {
  text: string;
  author: string;
  role: string;
}

@Component({
  selector: 'app-testimonials-section',
  standalone: true,
  imports: [],
  templateUrl: './testimonials-section.html',
  styleUrl: './testimonials-section.scss',
})
export class TestimonialsSectionComponent {
  testimonials: Testimonial[] = [
    {
      text: 'Una experiencia verdaderamente transcendental. Cada plato es una obra maestra. El chef entiende la profundidad de nuestras tradiciones culinarias.',
      author: 'María García',
      role: 'Chef reconocida internacionalmente',
    },
    {
      text: 'No es solo comida, es poesía en el plato. La atención al detalle, desde la presentación hasta el sabor, es impecable.',
      author: 'Carlos Mendoza',
      role: 'Crítico gastronómico',
    },
    {
      text: 'El ambiente es elegante sin ser pretencioso. El servicio impecable. Y la comida... simplemente incomparable.',
      author: 'Julia Rodríguez',
      role: 'Viajera de gastronomía',
    },
    {
      text: 'Sabor Mayor redefinió lo que una cena puede ser. Volvería todas las noches si pudiera.',
      author: 'Andrés Solano',
      role: 'Director creativo',
    },
    {
      text: 'Los ingredientes hablan solos. Raramente encuentro en un restaurante esta honestidad de sabor combinada con tanta elegancia.',
      author: 'Elena Vargas',
      role: 'Sommelier',
    },
  ];

  // 4x duplication for seamless infinite marquee
  get marqueeRow1(): Testimonial[] {
    return [...this.testimonials, ...this.testimonials, ...this.testimonials, ...this.testimonials];
  }

  get marqueeRow2(): Testimonial[] {
    const reversed = [...this.testimonials].reverse();
    return [...reversed, ...reversed, ...reversed, ...reversed];
  }
}

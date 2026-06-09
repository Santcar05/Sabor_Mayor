import { Component } from '@angular/core';
import { NgForOf } from '@angular/common';

interface Experience {
  title: string;
  duration: string;
  description: string;
  price: string;
  buttonText: string;
}

@Component({
  selector: 'app-experience-section',
  standalone: true,
  imports: [NgForOf],
  templateUrl: './experience-section.html',
  styleUrl: './experience-section.scss',
})
export class ExperienceSectionComponent {
  experiences: Experience[] = [
    {
      title: 'Menú Degustación',
      duration: '4 horas',
      description: 'Un viaje de 7 pasos a través de la herencia culinaria latinoamericana, acompañado de vinos premium seleccionados. Incluye maridaje completo y explicaciones de cada creación.',
      price: '$120.00 por persona',
      buttonText: 'Conocer más',
    },
    {
      title: 'Cena Privada',
      duration: '3-4 horas',
      description: 'Diseña tu propia experiencia culinaria con nuestro chef. Menú personalizado para hasta 20 personas en nuestro salón privado con acceso a la cocina abierta.',
      price: '$150.00 por persona',
      buttonText: 'Solicitar',
    },
    {
      title: 'Taller de Cocina',
      duration: '2.5 horas',
      description: 'Aprende a preparar platos clásicos latinoamericanos bajo la guía del chef. Incluye ingredientes premium, libreta de recetas y degustación de lo preparado.',
      price: '$95.00 por persona',
      buttonText: 'Inscribirse',
    },
  ];
}

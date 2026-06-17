import { Component } from '@angular/core';

interface Experience {
  title: string;
  subtitle: string;
  duration: string;
  description: string;
  price: string;
  image: string;
  buttonText: string;
}

@Component({
  selector: 'app-experience-section',
  standalone: true,
  imports: [],
  templateUrl: './experience-section.html',
  styleUrl: './experience-section.scss',
})
export class ExperienceSectionComponent {
  experiences: Experience[] = [
    {
      title: 'Menú Degustación',
      subtitle: 'Un viaje en siete tiempos',
      duration: '4 horas',
      description:
        'Un recorrido de 7 pasos a través de la herencia culinaria latinoamericana, acompañado de vinos premium seleccionados. Incluye maridaje completo y explicaciones de cada creación.',
      price: '$120.00 por persona',
      image: 'assets/ceviche.jpg',
      buttonText: 'Conocer más',
    },
    {
      title: 'Cena Privada',
      subtitle: 'Solo para ti y los tuyos',
      duration: '3–4 horas',
      description:
        'Diseña tu propia experiencia culinaria con nuestro chef. Menú personalizado para hasta 20 personas en nuestro salón privado con acceso a la cocina abierta.',
      price: '$150.00 por persona',
      image: 'assets/Aji_de_Gallina_Contemporaneo.jpg',
      buttonText: 'Solicitar',
    },
    {
      title: 'Taller de Cocina',
      subtitle: 'Aprende del maestro',
      duration: '2.5 horas',
      description:
        'Aprende a preparar platos clásicos latinoamericanos bajo la guía del chef. Incluye ingredientes premium, libreta de recetas y degustación de lo preparado.',
      price: '$95.00 por persona',
      image: 'assets/Limenia.jpg',
      buttonText: 'Inscribirse',
    },
  ];
}

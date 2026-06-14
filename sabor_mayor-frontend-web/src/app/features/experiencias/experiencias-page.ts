import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ButtonComponent } from '../../components/ui/button/button';
import { CartService } from '../../shared/services/cart.service';
import { NotificationService } from '../../shared/services/notification.service';

interface Experience {
  id: string;
  slug: string;
  title: string;
  subtitle: string;
  description: string;
  price: number;
  duration: string;
  guests: string;
  image: string;
  tags: string[];
  highlight: boolean;
  occasion: string;
}

const EXPERIENCES: Experience[] = [
  {
    id: 'exp-degustacion',
    slug: 'menu-degustacion',
    title: 'Menú Degustación',
    subtitle: 'Un viaje por nuestra cocina',
    description:
      'Ocho tiempos que narran la historia de la cocina latinoamericana de alta montaña. Cada plato cuenta con maridaje opcional de vinos naturales y un recorrido por nuestra bodega.',
    price: 280000,
    duration: '3 h aprox.',
    guests: '2 – 10 personas',
    image: '/assets/images/exp-degustacion.jpg',
    tags: ['8 tiempos', 'Maridaje opcional', 'Menú de temporada'],
    highlight: true,
    occasion: 'Menú Degustación',
  },
  {
    id: 'exp-maridaje',
    slug: 'maridaje-vinos',
    title: 'Maridaje de Vinos',
    subtitle: 'Tierra y cepa en sinfonía',
    description:
      'Cinco platos de autor diseñados en conjunto con nuestro sommelier para armonizar con vinos de pequeños productores colombianos y sudamericanos.',
    price: 195000,
    duration: '2.5 h aprox.',
    guests: '2 – 8 personas',
    image: '/assets/images/exp-maridaje.jpg',
    tags: ['5 tiempos', 'Sommelier incluido', 'Vinos seleccionados'],
    highlight: false,
    occasion: 'Maridaje de Vinos',
  },
  {
    id: 'exp-chef',
    slug: 'chefs-table',
    title: "Chef's Table",
    subtitle: 'La cocina como escenario',
    description:
      'Hasta seis comensales se sientan frente a la cocina abierta y viven el servicio junto al equipo. El chef adapta el menú según los ingredientes del mercado de esa mañana.',
    price: 380000,
    duration: '4 h aprox.',
    guests: '2 – 6 personas',
    image: '/assets/images/exp-chef.jpg',
    tags: ['Menú sorpresa', 'Cocina abierta', 'Interacción con el chef'],
    highlight: true,
    occasion: "Chef's Table",
  },
  {
    id: 'exp-eventos',
    slug: 'eventos-privados',
    title: 'Eventos Privados',
    subtitle: 'El restaurante, solo para ti',
    description:
      'Reserva la totalidad del espacio para celebraciones corporativas, bodas íntimas o cenas de empresa. Menú diseñado a medida, decoración personalizada y servicio exclusivo.',
    price: 0,
    duration: 'Según acuerdo',
    guests: '20 – 60 personas',
    image: '/assets/images/exp-eventos.jpg',
    tags: ['Espacio exclusivo', 'Menú a medida', 'Coordinador incluido'],
    highlight: false,
    occasion: 'Evento Privado',
  },
];

@Component({
  selector: 'app-experiencias-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonComponent],
  templateUrl: './experiencias-page.html',
  styleUrl: './experiencias-page.scss',
})
export class ExperienciasPageComponent {
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  protected readonly experiences = signal(EXPERIENCES);
  protected readonly selected = signal<Experience | null>(null);

  protected select(exp: Experience): void {
    this.selected.set(exp);
  }

  protected dismiss(): void {
    this.selected.set(null);
  }

  protected reserve(exp: Experience): void {
    this.router.navigate(['/reservar'], {
      queryParams: { occasion: exp.occasion, experience: exp.id },
    });
  }

  protected contact(exp: Experience): void {
    this.router.navigate(['/contacto'], {
      queryParams: { subject: `Consulta: ${exp.title}` },
    });
  }

  protected formatPrice(price: number): string {
    if (price === 0) return 'Precio a convenir';
    return `$${price.toLocaleString('es-CO')} / persona`;
  }
}

import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonComponent } from '../../components/ui/button/button';

@Component({
  selector: 'app-nosotros-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonComponent],
  templateUrl: './nosotros-page.html',
  styleUrl: './nosotros-page.scss',
})
export class NosotrosPageComponent {
  readonly team = [
    {
      name: 'Camilo Restrepo',
      role: 'Chef Ejecutivo y Fundador',
      bio: 'Formado en Bogotá y Madrid, Camilo regresó a Colombia con la misión de elevar la cocina andina al lenguaje de la alta gastronomía.',
      image: '/assets/images/team-camilo.jpg',
    },
    {
      name: 'Laura Bermúdez',
      role: 'Chef de Repostería',
      bio: 'Especialista en técnicas de confitería latinoamericana, Laura transforma el chocolate de origen colombiano en experiencias que trascienden el postre.',
      image: '/assets/images/team-laura.jpg',
    },
    {
      name: 'Andrés Ospina',
      role: 'Sommelier y Director de Sala',
      bio: 'Con más de 15 años seleccionando vinos, Andrés ha construido una bodega que celebra los pequeños productores de los Andes.',
      image: '/assets/images/team-andres.jpg',
    },
  ];

  readonly values = [
    {
      icon: '🌿',
      title: 'Ingredientes de origen',
      text: 'Trabajamos directamente con más de 30 productores locales de los Andes colombianos.',
    },
    {
      icon: '🍃',
      title: 'Temporada y biodiversidad',
      text: 'Nuestra carta cambia cuatro veces al año para honrar los ciclos de la tierra.',
    },
    {
      icon: '🔥',
      title: 'Técnica al servicio del sabor',
      text: 'Combinamos siglos de tradición culinaria con técnicas contemporáneas sin perder la identidad.',
    },
    {
      icon: '🤝',
      title: 'Comunidad y territorio',
      text: 'El 5 % de nuestros ingresos va a programas de formación gastronómica en comunidades rurales.',
    },
  ];
}

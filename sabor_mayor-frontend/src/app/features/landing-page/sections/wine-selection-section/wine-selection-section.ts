import { Component } from '@angular/core';
import { NgForOf } from '@angular/common';

interface Wine {
  name: string;
  region: string;
  description: string;
  price: string;
  image: string;
}

@Component({
  selector: 'app-wine-selection-section',
  standalone: true,
  imports: [NgForOf],
  templateUrl: './wine-selection-section.html',
  styleUrl: './wine-selection-section.scss',
})
export class WineSelectionSectionComponent {
  wines: Wine[] = [
    {
      name: 'Malbec Reserva',
      region: 'Mendoza, Argentina',
      description: 'Notas de fruta negra con toques de especias y roble francés.',
      price: '$52.00',
      image: 'assets/Malbec.jpg',
    },
    {
      name: 'Carmenere Gran Reserva',
      region: 'Valle del Maule, Chile',
      description: 'Expresión elegante con acidez equilibrada y taninos sedosos.',
      price: '$48.00',
      image: 'assets/Carmenere.jpg',
    },
    {
      name: 'Sauvignon Blanc',
      region: 'Valle de Ica, Perú',
      description: 'Fresco y aromático con notas cítricas y florales.',
      price: '$38.00',
      image: 'assets/Sauvignon_Blanc.jpg',
    },
  ];
}

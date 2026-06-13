import { Component } from '@angular/core';
import { NgForOf, NgIf } from '@angular/common';

interface FooterLink {
  label: string;
  href: string;
}

interface FooterSection {
  title: string;
  content?: string;
  links?: FooterLink[];
}

@Component({
  selector: 'app-footer-section',
  standalone: true,
  imports: [NgForOf, NgIf],
  templateUrl: './footer-section.html',
  styleUrl: './footer-section.scss',
})
export class FooterSectionComponent {
  footerSections: FooterSection[] = [
    {
      title: 'Sabor Mayor',
      content: 'Celebrando la riqueza culinaria de América Latina con sofisticación contemporánea.',
    },
    {
      title: 'Enlaces Rápidos',
      links: [
        { label: 'Sobre Nosotros', href: '#about' },
        { label: 'Menú', href: '#featured' },
        { label: 'Vinos', href: '#wines' },
        { label: 'Experiencias', href: '#experience' },
      ],
    },
    {
      title: 'Síguenos',
      links: [
        { label: 'Instagram', href: '#' },
        { label: 'Facebook', href: '#' },
        { label: 'Twitter', href: '#' },
      ],
    },
    {
      title: 'Legal',
      links: [
        { label: 'Política de Privacidad', href: '#' },
        { label: 'Términos de Servicio', href: '#' },
        { label: 'Política de Cookies', href: '#' },
      ],
    },
  ];

  currentYear = new Date().getFullYear();
}

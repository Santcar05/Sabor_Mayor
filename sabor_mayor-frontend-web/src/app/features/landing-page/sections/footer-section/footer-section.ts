import { Component } from '@angular/core';

@Component({
  selector: 'app-footer-section',
  standalone: true,
  imports: [],
  templateUrl: './footer-section.html',
  styleUrl: './footer-section.scss',
})
export class FooterSectionComponent {
  currentYear = new Date().getFullYear();
}

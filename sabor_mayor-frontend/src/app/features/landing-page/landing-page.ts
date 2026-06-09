import { Component } from '@angular/core';
import { PublicHeader } from '../../components/public-header/public-header';
import { HeroSectionComponent } from './sections/hero-section/hero-section';
import { AboutSectionComponent } from './sections/about-section/about-section';
import { FeaturedDishesSectionComponent } from './sections/featured-dishes-section/featured-dishes-section';
import { WineSelectionSectionComponent } from './sections/wine-selection-section/wine-selection-section';
import { ExperienceSectionComponent } from './sections/experience-section/experience-section';
import { TestimonialsSectionComponent } from './sections/testimonials-section/testimonials-section';
import { ContactReservationSectionComponent } from './sections/contact-reservation-section/contact-reservation-section';
import { FooterSectionComponent } from './sections/footer-section/footer-section';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [
    PublicHeader,
    HeroSectionComponent,
    AboutSectionComponent,
    FeaturedDishesSectionComponent,
    WineSelectionSectionComponent,
    ExperienceSectionComponent,
    TestimonialsSectionComponent,
    ContactReservationSectionComponent,
    FooterSectionComponent,
  ],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',
})
export class LandingPage {}

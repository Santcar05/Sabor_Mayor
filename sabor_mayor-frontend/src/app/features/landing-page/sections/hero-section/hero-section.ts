import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import {
  fadeInUpTrigger,
  glowEffectTrigger,
  ANIMATION_TIMINGS,
} from '../../animations.config';
import { ParallaxService } from '../../services/parallax.service';
import { ScrollRevealDirective } from '../../directives/scroll-reveal.directive';
import { MagneticHoverDirective } from '../../directives/magnetic-hover.directive';
import { Tilt3dDirective } from '../../directives/tilt-3d.directive';
import { AdvancedScrollDirective } from '../../directives/advanced-scroll.directive';

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [CommonModule, ScrollRevealDirective, MagneticHoverDirective, Tilt3dDirective, AdvancedScrollDirective],
  templateUrl: './hero-section.html',
  styleUrl: './hero-section.scss',
  animations: [fadeInUpTrigger, glowEffectTrigger],
})
export class HeroSectionComponent implements OnInit, OnDestroy {
  @ViewChild('heroImage') heroImageRef: ElementRef | undefined;

  /* Estados para animaciones */
  heroLoaded = false;
  buttonHoverState: 'idle' | 'hover' | 'active' = 'idle';
  glowState = 0;

  /* Timings para template binding */
  animationTimings = ANIMATION_TIMINGS;

  constructor(private parallaxService: ParallaxService) {}

  ngOnInit(): void {
    /* Simula carga de página */
    setTimeout(() => {
      this.heroLoaded = true;
    }, 100);

    /* Registra hero image para parallax effect */
    if (this.heroImageRef) {
      this.parallaxService.registerParallaxElement(
        'hero-image',
        this.heroImageRef.nativeElement,
        0.5 // medium parallax effect
      );
    }
  }

  ngOnDestroy(): void {
    /* Limpia parallax al destruir */
    this.parallaxService.unregisterParallaxElement('hero-image');
  }

  /**
   * MICRO-INTERACTION: Button Magnetic Effect
   * Trigguea scale y glow en hover
   */
  onButtonHover(): void {
    this.buttonHoverState = 'hover';
    this.glowState++;
  }

  onButtonLeave(): void {
    this.buttonHoverState = 'idle';
  }

  onButtonClick(): void {
    this.buttonHoverState = 'active';
    /* Feedback visual - regresa a idle después de animación */
    setTimeout(() => {
      this.buttonHoverState = 'idle';
    }, 300);
  }

  /**
   * SCROLL TRACKING para efectos dinámicos
   */
  trackScrollPosition(): void {
    const scrollY = this.parallaxService.getScrollY();
    /* Puedes usar scrollY para otras animaciones dinámicas */
  }
}

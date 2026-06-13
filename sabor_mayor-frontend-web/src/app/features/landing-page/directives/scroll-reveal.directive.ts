/**
 * SCROLL REVEAL DIRECTIVE
 * Trigguea animaciones fadeInUp cuando elementos entran en viewport
 * Performance: Usa Intersection Observer (lazy detection)
 */

import {
  Directive,
  ElementRef,
  Input,
  OnInit,
  OnDestroy,
  Inject,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({
  selector: '[appScrollReveal]',
  standalone: true,
})
export class ScrollRevealDirective implements OnInit, OnDestroy {
  @Input() revealDelay: string = '0.2s';
  @Input() revealDuration: string = '0.6s';

  private observer: IntersectionObserver | null = null;
  private isBrowser: boolean;

  constructor(
    private el: ElementRef,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;

    /* Respeta prefers-reduced-motion */
    if (this.prefersReducedMotion()) {
      this.el.nativeElement.classList.add('reveal');
      return;
    }

    /* Configuración de Intersection Observer */
    const observerOptions: IntersectionObserverInit = {
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px', /* Trigguea 50px antes de entrar completamente */
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          /* Aplica animación cuando entra en viewport */
          this.el.nativeElement.classList.add('reveal');

          /* Deja de observar después de animation (optimización) */
          if (this.observer) {
            this.observer.unobserve(this.el.nativeElement);
          }
        }
      });
    }, observerOptions);

    this.observer.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.unobserve(this.el.nativeElement);
      this.observer.disconnect();
    }
  }

  /**
   * Detecta preferencia de usuario para movimiento reducido
   */
  private prefersReducedMotion(): boolean {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }
}

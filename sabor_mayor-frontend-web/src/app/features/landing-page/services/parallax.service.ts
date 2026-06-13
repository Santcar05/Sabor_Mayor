/**
 * PARALLAX SERVICE
 * Maneja efectos de parallax en scroll con optimización de performance
 * Usa requestAnimationFrame y debouncing para 60 FPS
 */

import { Injectable, NgZone, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, fromEvent } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

interface ParallaxElement {
  element: HTMLElement;
  offset: number;
  initialY: number;
}

@Injectable({
  providedIn: 'root',
})
export class ParallaxService {
  private parallaxElements: Map<string, ParallaxElement> = new Map();
  private scrollY$ = new BehaviorSubject<number>(0);
  private animationFrameId: number | null = null;
  private isBrowser: boolean;

  constructor(
    private ngZone: NgZone,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
      this.initScrollListener();
    }
  }

  /**
   * Inicializa listener de scroll fuera de Angular zone para mejor performance
   * Solo se ejecuta en navegador (no en SSR)
   */
  private initScrollListener(): void {
    if (!this.isBrowser) return;

    this.ngZone.runOutsideAngular(() => {
      fromEvent(window, 'scroll')
        .pipe(throttleTime(16, undefined, { leading: true, trailing: true })) // ~60fps
        .subscribe(() => {
          const scrollY = window.scrollY || window.pageYOffset;
          this.scrollY$.next(scrollY);
          this.updateParallaxElements(scrollY);
        });
    });
  }

  /**
   * Registra un elemento para efecto parallax
   * @param id Identificador único del elemento
   * @param element Referencia al elemento DOM
   * @param offset Intensidad del parallax (0.3 - subtle, 0.5 - medium, 0.8 - aggressive)
   */
  registerParallaxElement(
    id: string,
    element: HTMLElement,
    offset: number = 0.5
  ): void {
    if (!element || !this.isBrowser) return;

    const parallaxElement: ParallaxElement = {
      element,
      offset,
      initialY: element.offsetTop,
    };

    this.parallaxElements.set(id, parallaxElement);

    /* Aplica initial transform para GPU acceleration */
    element.style.willChange = 'transform';
    element.style.transform = 'translateZ(0)';
  }

  /**
   * Desregistra un elemento parallax
   */
  unregisterParallaxElement(id: string): void {
    const parallaxElement = this.parallaxElements.get(id);
    if (parallaxElement) {
      parallaxElement.element.style.willChange = 'auto';
      this.parallaxElements.delete(id);
    }
  }

  /**
   * Actualiza posición de elementos parallax en base a scroll
   * Performance: Usa transform3d para GPU acceleration
   */
  private updateParallaxElements(scrollY: number): void {
    this.parallaxElements.forEach((parallaxElement) => {
      const { element, offset, initialY } = parallaxElement;

      /* Calcula distancia y aplica parallax */
      const distance = scrollY - initialY;
      const parallaxOffset = distance * offset;

      /* Usa transform3d para máxima performance */
      element.style.transform = `translate3d(0, ${parallaxOffset}px, 0)`;
    });
  }

  /**
   * Obtiene scroll Y actual
   */
  getScrollY(): number {
    return this.scrollY$.getValue();
  }

  /**
   * Observable del scroll Y actual
   */
  getScrollY$() {
    return this.scrollY$.asObservable();
  }

  /**
   * Limpia todos los elementos parallax
   */
  cleanup(): void {
    this.parallaxElements.forEach((parallaxElement) => {
      parallaxElement.element.style.willChange = 'auto';
    });
    this.parallaxElements.clear();
  }
}

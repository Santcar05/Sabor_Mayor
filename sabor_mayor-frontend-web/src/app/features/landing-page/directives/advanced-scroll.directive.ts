/**
 * ADVANCED SCROLL DIRECTIVE
 * Detección de scroll velocity + Skew on scroll + Clip-path reveals
 * Implementa física de movimiento e inercia en animaciones
 */

import {
  Directive,
  ElementRef,
  Input,
  OnInit,
  OnDestroy,
  Renderer2,
  NgZone,
  Inject,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { throttleTime } from 'rxjs/operators';
import { fromEvent } from 'rxjs';

interface ScrollConfig {
  revealType: 'clippath' | 'fade' | 'both';
  maxSkew: number;
  direction: 'down' | 'up' | 'auto';
}

@Directive({
  selector: '[appAdvancedScroll]',
  standalone: true,
})
export class AdvancedScrollDirective implements OnInit, OnDestroy {
  @Input() scrollConfig: ScrollConfig = {
    revealType: 'clippath',
    maxSkew: 10,
    direction: 'down',
  };

  private lastScrollY = 0;
  private scrollVelocity = 0;
  private maxScrollVelocity = 20;
  private currentSkew = 0;
  private targetSkew = 0;
  private animationFrameId: number | null = null;
  private observer: IntersectionObserver | null = null;
  private isBrowser: boolean;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private ngZone: NgZone,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;

    this.setupElement();
    this.initScrollListener();
    this.initIntersectionObserver();
    this.startSkewAnimation();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.observer) {
      this.observer.disconnect();
    }
    /* Resetea estilos para evitar corrupción visual */
    this.renderer.removeStyle(this.el.nativeElement, 'clip-path');
    this.renderer.removeStyle(this.el.nativeElement, 'transform');
    this.renderer.removeStyle(this.el.nativeElement, 'transition');
    this.renderer.removeStyle(this.el.nativeElement, 'will-change');
  }

  /**
   * Setup inicial del elemento
   */
  private setupElement(): void {
    this.renderer.setStyle(this.el.nativeElement, 'transform-origin', 'center');
    this.renderer.setStyle(this.el.nativeElement, 'transition', 'transform 0.1s ease-out');
    this.renderer.setStyle(this.el.nativeElement, 'will-change', 'transform');
  }

  /**
   * Listener de scroll con throttle para performance
   */
  private initScrollListener(): void {
    this.ngZone.runOutsideAngular(() => {
      fromEvent(window, 'scroll')
        .pipe(throttleTime(16, undefined, { leading: true, trailing: true }))
        .subscribe(() => {
          this.updateScrollVelocity();
        });
    });
  }

  /**
   * Calcula velocidad de scroll basada en cambio de posición
   */
  private updateScrollVelocity(): void {
    const currentScrollY = window.scrollY || window.pageYOffset;
    const deltaScroll = currentScrollY - this.lastScrollY;

    /* Interpolación suave de velocidad */
    this.scrollVelocity = deltaScroll * 0.5 + this.scrollVelocity * 0.5;

    /* Clamp a máximo */
    this.scrollVelocity = Math.max(
      -this.maxScrollVelocity,
      Math.min(this.maxScrollVelocity, this.scrollVelocity)
    );

    /* Calcula skew basado en velocidad */
    this.targetSkew = (this.scrollVelocity / this.maxScrollVelocity) * this.scrollConfig.maxSkew;

    this.lastScrollY = currentScrollY;
  }

  /**
   * Intersection Observer para clip-path reveals
   */
  private initIntersectionObserver(): void {
    const observerOptions: IntersectionObserverInit = {
      threshold: [0, 0.25, 0.5, 0.75, 1],
      rootMargin: '50px',
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          /* Aplica clip-path reveal */
          this.applyClipPathReveal(entry);

          /* Agrega clase para fade reveal */
          this.renderer.addClass(this.el.nativeElement, 'scroll-reveal-active');
        }
      });
    }, observerOptions);

    this.observer.observe(this.el.nativeElement);
  }

  /**
   * Aplica clip-path reveal basado en dirección de scroll
   */
  private applyClipPathReveal(entry: IntersectionObserverEntry): void {
    const visibility = entry.intersectionRatio;
    let clipPath = '';

    switch (this.scrollConfig.direction) {
      case 'down':
        clipPath = `polygon(0 ${100 - visibility * 100}%, 100% ${100 - visibility * 100}%, 100% 100%, 0 100%)`;
        break;
      case 'up':
        clipPath = `polygon(0 0, 100% 0, 100% ${visibility * 100}%, 0 ${visibility * 100}%)`;
        break;
      case 'auto':
        /* Determina dirección basado en scroll velocity */
        const direction = this.scrollVelocity > 0 ? 'down' : 'up';
        const percentage = direction === 'down'
          ? 100 - visibility * 100
          : visibility * 100;
        clipPath = direction === 'down'
          ? `polygon(0 ${percentage}%, 100% ${percentage}%, 100% 100%, 0 100%)`
          : `polygon(0 0, 100% 0, 100% ${percentage}%, 0 ${percentage}%)`;
        break;
    }

    this.renderer.setStyle(this.el.nativeElement, 'clip-path', clipPath);
  }

  /**
   * Animación suave de skew usando requestAnimationFrame
   */
  private startSkewAnimation(): void {
    this.ngZone.runOutsideAngular(() => {
      const animate = () => {
        /* Interpolación hacia target skew */
        this.currentSkew += (this.targetSkew - this.currentSkew) * 0.1;

        /* Cuando scroll se detiene, decay a 0 */
        if (Math.abs(this.scrollVelocity) < 0.1) {
          this.currentSkew *= 0.95;
        }

        /* Aplica skew transform */
        this.renderer.setStyle(
          this.el.nativeElement,
          'transform',
          `skewY(${this.currentSkew}deg)`
        );

        this.animationFrameId = requestAnimationFrame(animate);
      };

      this.animationFrameId = requestAnimationFrame(animate);
    });
  }
}

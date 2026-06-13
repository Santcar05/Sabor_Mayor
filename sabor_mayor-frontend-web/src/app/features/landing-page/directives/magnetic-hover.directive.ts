/**
 * MAGNETIC HOVER DIRECTIVE - Extreme Edition
 * El botón es atraído hacia el cursor cuando se acerca
 * Con efecto elástico de "soltarse" cuando el cursor se aleja
 */

import {
  Directive,
  ElementRef,
  HostListener,
  Input,
  OnInit,
  OnDestroy,
  Renderer2,
  NgZone,
  Inject,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

interface MagneticConfig {
  magneticRange: number; // Rango en px donde aplica efecto magnético
  magneticStrength: number; // Fuerza de atracción (0-1)
  elasticity: number; // Elasticidad al soltar (0-1)
}

@Directive({
  selector: '[appMagneticHover]',
  standalone: true,
})
export class MagneticHoverDirective implements OnInit, OnDestroy {
  @Input() magneticConfig: MagneticConfig = {
    magneticRange: 200,
    magneticStrength: 0.8,
    elasticity: 0.12,
  };

  private currentX = 0;
  private currentY = 0;
  private targetX = 0;
  private targetY = 0;
  private animationFrameId: number | null = null;
  private originalRect: DOMRect | null = null;
  private isHovering = false;
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

    this.setupMagneticButton();
    this.startAnimation();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  /**
   * Setup inicial del botón
   */
  private setupMagneticButton(): void {
    this.renderer.setStyle(this.el.nativeElement, 'position', 'relative');
    this.renderer.setStyle(this.el.nativeElement, 'cursor', 'pointer');
    this.renderer.setStyle(this.el.nativeElement, 'transition', 'box-shadow 0.3s ease-out');
  }

  /**
   * Listener de mouse move
   */
  @HostListener('document:mousemove', ['$event'])
  onDocumentMouseMove(event: MouseEvent): void {
    if (!this.isBrowser || !this.isHovering) {
      this.targetX = 0;
      this.targetY = 0;
      return;
    }

    const rect = this.el.nativeElement.getBoundingClientRect();
    const mouseX = event.clientX;
    const mouseY = event.clientY;

    /* Calcula distancia del cursor al botón */
    const buttonCenterX = rect.left + rect.width / 2;
    const buttonCenterY = rect.top + rect.height / 2;

    const distanceX = mouseX - buttonCenterX;
    const distanceY = mouseY - buttonCenterY;
    const distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

    /* Solo aplica si está dentro del rango magnético */
    if (distance < this.magneticConfig.magneticRange) {
      /* Calcula atracción (inversamente proporcional a distancia) */
      const attractionX = (distanceX / distance) * (1 - distance / this.magneticConfig.magneticRange);
      const attractionY = (distanceY / distance) * (1 - distance / this.magneticConfig.magneticRange);

      this.targetX = attractionX * 30 * this.magneticConfig.magneticStrength;
      this.targetY = attractionY * 30 * this.magneticConfig.magneticStrength;
    } else {
      this.targetX = 0;
      this.targetY = 0;
    }
  }

  /**
   * Mouse enter - activa efecto magnético
   */
  @HostListener('mouseenter')
  onMouseEnter(): void {
    if (!this.isBrowser) return;

    this.isHovering = true;
    this.originalRect = this.el.nativeElement.getBoundingClientRect();

    /* Glow magnético en enter */
    this.renderer.setStyle(
      this.el.nativeElement,
      'box-shadow',
      '0 0 40px rgba(243, 181, 74, 0.6)'
    );
  }

  /**
   * Mouse leave - desactiva efecto magnético con elasticidad
   */
  @HostListener('mouseleave')
  onMouseLeave(): void {
    if (!this.isBrowser) return;

    this.isHovering = false;
    this.targetX = 0;
    this.targetY = 0;

    /* Restore box-shadow */
    this.renderer.setStyle(
      this.el.nativeElement,
      'box-shadow',
      '0 4px 15px rgba(243, 181, 74, 0.2)'
    );
  }

  /**
   * Animación suave con elasticidad (spring effect)
   */
  private startAnimation(): void {
    this.ngZone.runOutsideAngular(() => {
      const animate = () => {
        /* Interpolación con elasticidad para efecto spring */
        const springStrength = 0.15; // Control del "bounciness"
        this.currentX += (this.targetX - this.currentX) * springStrength;
        this.currentY += (this.targetY - this.currentY) * springStrength;

        /* Decay cuando no está siendo atraído */
        if (!this.isHovering) {
          this.currentX *= (1 - this.magneticConfig.elasticity);
          this.currentY *= (1 - this.magneticConfig.elasticity);
        }

        /* Aplica transform */
        this.renderer.setStyle(
          this.el.nativeElement,
          'transform',
          `translate(${this.currentX}px, ${this.currentY}px)`
        );

        this.animationFrameId = requestAnimationFrame(animate);
      };

      this.animationFrameId = requestAnimationFrame(animate);
    });
  }
}

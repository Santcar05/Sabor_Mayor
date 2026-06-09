/**
 * TILT 3D DIRECTIVE - Advanced Card Physics
 * Simula un efecto de Tilt 3D suave impulsado por posición del cursor
 * Con glare effect superpuesto para materialidad y profundidad
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

interface TiltConfig {
  maxTilt: number;
  scale: number;
  speed: number;
}

@Directive({
  selector: '[appTilt3d]',
  standalone: true,
})
export class Tilt3dDirective implements OnInit, OnDestroy {
  @Input() tiltConfig: TiltConfig = {
    maxTilt: 15,
    scale: 1.05,
    speed: 0.5,
  };

  private currentX = 0;
  private currentY = 0;
  private targetX = 0;
  private targetY = 0;
  private animationFrameId: number | null = null;
  private glareElement: HTMLElement | null = null;
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

    this.setupTiltCard();
    this.createGlareElement();
    this.startAnimation();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.glareElement) {
      this.renderer.removeChild(this.el.nativeElement, this.glareElement);
    }
  }

  /**
   * Setup inicial de la tarjeta para tilt
   */
  private setupTiltCard(): void {
    this.renderer.setStyle(this.el.nativeElement, 'perspective', '1000px');
    this.renderer.setStyle(this.el.nativeElement, 'transform-style', 'preserve-3d');
    this.renderer.setStyle(this.el.nativeElement, 'transition', 'transform 0.1s ease-out');
  }

  /**
   * Crea el elemento glare superpuesto
   */
  private createGlareElement(): void {
    this.glareElement = this.renderer.createElement('div');
    this.renderer.setStyle(this.glareElement, 'position', 'absolute');
    this.renderer.setStyle(this.glareElement, 'top', '0');
    this.renderer.setStyle(this.glareElement, 'left', '0');
    this.renderer.setStyle(this.glareElement, 'width', '100%');
    this.renderer.setStyle(this.glareElement, 'height', '100%');
    this.renderer.setStyle(this.glareElement, 'background',
      'linear-gradient(135deg, rgba(255,255,255,0.2), transparent)');
    this.renderer.setStyle(this.glareElement, 'opacity', '0');
    this.renderer.setStyle(this.glareElement, 'pointer-events', 'none');
    this.renderer.setStyle(this.glareElement, 'transition', 'opacity 0.1s ease-out');
    this.renderer.setStyle(this.glareElement, 'z-index', '10');
    this.renderer.setStyle(this.glareElement, 'border-radius', 'inherit');
    this.renderer.appendChild(this.el.nativeElement, this.glareElement);
  }

  /**
   * Listener de mouse move para calcular tilt
   */
  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent): void {
    if (!this.isBrowser) return;

    const rect = this.el.nativeElement.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    /* Calcula ángulos basados en posición del cursor */
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    const rotationX = ((y - centerY) / centerY) * this.tiltConfig.maxTilt;
    const rotationY = ((centerX - x) / centerX) * this.tiltConfig.maxTilt;

    this.targetX = rotationX;
    this.targetY = rotationY;

    /* Actualiza glare basado en posición */
    if (this.glareElement) {
      const glareX = (x / rect.width) * 100;
      const glareY = (y / rect.height) * 100;

      this.renderer.setStyle(
        this.glareElement,
        'background',
        `radial-gradient(circle at ${glareX}% ${glareY}%, rgba(255,255,255,0.3), transparent)`
      );
      this.renderer.setStyle(this.glareElement, 'opacity', '0.5');
    }
  }

  /**
   * Listener de mouse leave para reset
   */
  @HostListener('mouseleave')
  onMouseLeave(): void {
    if (!this.isBrowser) return;

    this.targetX = 0;
    this.targetY = 0;

    if (this.glareElement) {
      this.renderer.setStyle(this.glareElement, 'opacity', '0');
    }
  }

  /**
   * Animación suave de transición usando requestAnimationFrame
   */
  private startAnimation(): void {
    this.ngZone.runOutsideAngular(() => {
      const animate = () => {
        /* Interpolación suave hacia el target */
        this.currentX += (this.targetX - this.currentX) * this.tiltConfig.speed;
        this.currentY += (this.targetY - this.currentY) * this.tiltConfig.speed;

        /* Aplica transform 3D */
        this.renderer.setStyle(
          this.el.nativeElement,
          'transform',
          `rotateX(${this.currentX}deg) rotateY(${this.currentY}deg) scale(${this.tiltConfig.scale})`
        );

        this.animationFrameId = requestAnimationFrame(animate);
      };

      this.animationFrameId = requestAnimationFrame(animate);
    });
  }
}

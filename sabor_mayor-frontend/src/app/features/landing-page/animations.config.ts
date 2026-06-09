/**
 * ANIMACIONES PREMIUM - Sabor Mayor Landing Page
 * Top 1% Engineering - Micro-interacciones, scroll reveals, parallax effects
 * Optimizado para 60 FPS con hardware acceleration
 */

import { trigger, transition, style, animate, query, stagger } from '@angular/animations';

/**
 * CURVAS BEZIER PERSONALIZADAS
 * Explicación: Elegidas para naturalidad y profesionalismo
 * - cubic-bezier(0.4, 0, 0.2, 1): Material Design standard - suave, confiable
 * - cubic-bezier(0.25, 0.46, 0.45, 0.94): Spring-like - elegante, dinámico
 * - cubic-bezier(0.34, 1.56, 0.64, 1): Bounce suave - captura atención
 */

export const ANIMATION_TIMINGS = {
  // Micro-interacciones
  BUTTON_HOVER: '0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
  LINK_HOVER: '0.2s cubic-bezier(0.4, 0, 0.2, 1)',
  CARD_HOVER: '0.4s cubic-bezier(0.34, 1.56, 0.64, 1)',

  // Scroll reveals
  SCROLL_FADE_IN: '0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  SCROLL_STAGGER: '100ms',

  // Parallax
  PARALLAX_SMOOTH: '60ms linear',

  // Page transitions
  PAGE_ENTER: '0.5s cubic-bezier(0.4, 0, 0.2, 1)',
};

/**
 * TRIGGER: Fade In & Slide Up (Para Scroll Reveals)
 * Comportamiento: El elemento aparece desde abajo con fade-in suave
 * Curva: Material Design standard para sensación profesional
 */
export const fadeInUpTrigger = trigger('fadeInUp', [
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'translateY(30px)',
    }),
    animate('0.6s 0.2s cubic-bezier(0.4, 0, 0.2, 1)', style({
      opacity: 1,
      transform: 'translateY(0)',
    })),
  ]),
]);

/**
 * TRIGGER: Staggered List Animation
 * Comportamiento: Cada item en una lista aparece secuencialmente
 * Uso: Platos, testimonios, items de menú
 */
export const staggerListTrigger = trigger('staggerList', [
  transition('* => *', [
    query(':enter', [
      style({
        opacity: 0,
        transform: 'translateY(20px)',
      }),
      stagger('100ms', [
        animate('0.5s 0.1s cubic-bezier(0.4, 0, 0.2, 1)', style({
          opacity: 1,
          transform: 'translateY(0)',
        })),
      ]),
    ], { optional: true }),
  ]),
]);

/**
 * TRIGGER: Scale Pulse (Para micro-interacciones)
 * Comportamiento: Botón pulsa sutilmente al hover
 */
export const scalePulseTrigger = trigger('scalePulse', [
  transition('* => *', [
    animate('0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)', style({
      transform: 'scale(1.05)',
    })),
  ]),
]);

/**
 * TRIGGER: Glow Effect (Para CTAs)
 * Comportamiento: Efecto de brillo magnético en CTAs principales
 */
export const glowEffectTrigger = trigger('glowEffect', [
  transition('* <=> *', [
    animate('0.4s cubic-bezier(0.4, 0, 0.2, 1)'),
  ]),
]);

/**
 * TRIGGER: Rotate In (Para iconos/elementos decorativos)
 * Comportamiento: Rotación suave de entrada
 */
export const rotateInTrigger = trigger('rotateIn', [
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'rotate(-10deg) scale(0.9)',
    }),
    animate('0.5s 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)', style({
      opacity: 1,
      transform: 'rotate(0) scale(1)',
    })),
  ]),
]);

/**
 * TRIGGER: Blur Fade In (Para overlays y fondos)
 * Comportamiento: Transición suave desde blur a nítido
 */
export const blurFadeInTrigger = trigger('blurFadeIn', [
  transition(':enter', [
    style({
      opacity: 0,
      backdropFilter: 'blur(10px)',
    }),
    animate('0.8s cubic-bezier(0.4, 0, 0.2, 1)', style({
      opacity: 1,
      backdropFilter: 'blur(0px)',
    })),
  ]),
]);

/**
 * TRIGGER: Clip Path Reveal (Dramático, directional)
 * Comportamiento: Elemento se "desplega" desde una dirección
 * Curva: EXPO IN-OUT cubic-bezier(0.87, 0, 0.13, 1) para impacto
 */
export const clipPathRevealTrigger = trigger('clipPathReveal', [
  transition(':enter', [
    style({
      opacity: 0,
      clipPath: 'polygon(0 100%, 100% 100%, 100% 100%, 0 100%)',
    }),
    animate('0.8s cubic-bezier(0.87, 0, 0.13, 1)', style({
      opacity: 1,
      clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 100%)',
    })),
  ]),
]);

/**
 * TRIGGER: Text Split (Palabras/líneas individuales)
 * Comportamiento: Texto aparece línea por línea con stagger
 */
export const textSplitTrigger = trigger('textSplit', [
  transition('* => *', [
    query('span', [
      style({
        opacity: 0,
        transform: 'translateY(30px)',
        clipPath: 'polygon(0 100%, 100% 100%, 100% 100%, 0 100%)',
      }),
      stagger('80ms', [
        animate('0.7s cubic-bezier(0.87, 0, 0.13, 1)', style({
          opacity: 1,
          transform: 'translateY(0)',
          clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 100%)',
        })),
      ]),
    ], { optional: true }),
  ]),
]);

/**
 * TRIGGER: Tilt 3D Entrance (Para tarjetas con physics)
 * Comportamiento: Entra con rotación 3D y scale
 */
export const tilt3dEntranceTrigger = trigger('tilt3dEntrance', [
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'perspective(1000px) rotateX(20deg) rotateY(-20deg) scale(0.9)',
    }),
    animate('0.8s cubic-bezier(0.34, 1.56, 0.64, 1)', style({
      opacity: 1,
      transform: 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1)',
    })),
  ]),
]);

/**
 * TRIGGER: Scale Pulse Extreme (Para micro-interacciones dramáticas)
 * Comportamiento: Escala y glow explosivo
 */
export const scalePulseExtremeTrigger = trigger('scalePulseExtreme', [
  transition('* => *', [
    animate('0.4s cubic-bezier(0.34, 1.56, 0.64, 1)', style({
      transform: 'scale(1.12)',
    })),
  ]),
]);

/**
 * CONSTANTES DE TRANSICIÓN PARALLAX
 * Para efectos de profundidad en scroll
 */
export const PARALLAX_OFFSETS = {
  SUBTLE: 0.3,      // Movimiento muy sutil
  MEDIUM: 0.5,      // Movimiento moderado
  AGGRESSIVE: 0.8,  // Movimiento notable pero profesional
};

/**
 * DEBOUNCE HELPER para scroll events (performance)
 * Evita ejecutar cálculos de parallax en cada pixel
 */
export function debounce(func: Function, wait: number) {
  let timeout: any;
  return function executedFunction(...args: any[]) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * EXPLICACIÓN DE TIMINGS:
 *
 * BUTTON_HOVER (0.3s spring): Rápida y elástica, captura atención inmediatamente
 * Curva spring (0.25, 0.46, 0.45, 0.94) simula un resorte natural
 *
 * SCROLL_FADE_IN (0.6s): Suficientemente lenta para ser elegante y profesional
 * Material Design standard permite que el eye-tracking sea cómodo
 *
 * SCROLL_STAGGER (100ms): Intervalo perfecto para que la secuencia sea legible
 * pero sin esperas aburridas entre elementos
 *
 * PARALLAX_SMOOTH (60ms linear): Sincronizado con scroll sin suavizado
 * (linear porque el scroll ya tiene su propia aceleración)
 *
 * PAGE_ENTER (0.5s): Balance entre performance perception y elegancia
 */

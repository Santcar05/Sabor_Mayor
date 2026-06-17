import { Component, AfterViewInit, OnDestroy, ElementRef, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

interface Stat {
  end: number;
  suffix: string;
  label: string;
  isDecimal?: boolean;
}

@Component({
  selector: 'app-stats-section',
  standalone: true,
  imports: [],
  templateUrl: './stats-section.html',
  styleUrl: './stats-section.scss',
})
export class StatsSectionComponent implements AfterViewInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly el = inject(ElementRef);

  stats: Stat[] = [
    { end: 25,  suffix: '+', label: 'Años de tradición' },
    { end: 180, suffix: '',  label: 'Cubiertos por noche' },
    { end: 40,  suffix: '+', label: 'Platos de temporada' },
    { end: 4.9, suffix: '★', label: 'Calificación promedio', isDecimal: true },
  ];

  displayValues = signal<string[]>(['0', '0', '0', '0']);

  private observer: IntersectionObserver | null = null;
  private animated = false;

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !this.animated) {
          this.animated = true;
          this.stats.forEach((stat, i) => this.animateValue(stat, i));
          this.observer?.disconnect();
        }
      },
      { threshold: 0.3 }
    );

    const section = this.el.nativeElement.querySelector('.stats-strip');
    if (section) this.observer.observe(section);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private animateValue(stat: Stat, index: number): void {
    const duration = 1800;
    const start = performance.now();

    const tick = (now: number) => {
      const progress = Math.min((now - start) / duration, 1);
      const ease = 1 - Math.pow(1 - progress, 4);
      const current = stat.end * ease;
      const display = stat.isDecimal
        ? current.toFixed(1) + stat.suffix
        : Math.round(current) + stat.suffix;

      const updated = [...this.displayValues()];
      updated[index] = display;
      this.displayValues.set(updated);

      if (progress < 1) requestAnimationFrame(tick);
    };

    requestAnimationFrame(tick);
  }
}

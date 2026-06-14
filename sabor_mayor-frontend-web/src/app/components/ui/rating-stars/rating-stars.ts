import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';

/** Selector/visualizador de calificación 1–5 estrellas. */
@Component({
  selector: 'app-rating-stars',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="stars" [class.readonly]="readonly()" role="radiogroup" aria-label="Calificación">
      @for (star of starsArray; track star) {
        <button
          type="button"
          class="star"
          [class.filled]="star <= display()"
          [attr.aria-checked]="star === value()"
          [disabled]="readonly()"
          (click)="select(star)"
          (mouseenter)="hover.set(star)"
          (mouseleave)="hover.set(0)"
        >
          ★
        </button>
      }
    </div>
  `,
  styles: [
    `
      .stars { display: inline-flex; gap: 0.2rem; }
      .star {
        background: none;
        border: none;
        cursor: pointer;
        font-size: 1.6rem;
        line-height: 1;
        color: rgba(213, 196, 164, 0.3);
        transition: transform var(--dur-fast) var(--ease-fast), color var(--dur-fast);
      }
      .star.filled { color: var(--sabor-gold-main); }
      .star:not(:disabled):hover { transform: scale(1.2); }
      .stars.readonly .star { cursor: default; }
    `,
  ],
})
export class RatingStarsComponent {
  value = input(0);
  readonly = input(false);
  readonly changed = output<number>();

  protected hover = signal(0);
  protected readonly starsArray = [1, 2, 3, 4, 5];
  protected display = computed(() => this.hover() || this.value());

  protected select(star: number): void {
    if (this.readonly()) return;
    this.changed.emit(star);
  }
}

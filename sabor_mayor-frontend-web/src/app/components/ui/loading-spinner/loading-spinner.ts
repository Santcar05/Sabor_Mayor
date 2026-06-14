import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Spinner de carga de marca. */
@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="wrap" [style.--size.px]="size()" role="status" [attr.aria-label]="label()">
      <span class="ring"></span>
      @if (label() && showLabel()) {
        <span class="label">{{ label() }}</span>
      }
    </div>
  `,
  styles: [
    `
      .wrap {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        gap: 0.75rem;
        --size: 40px;
      }
      .ring {
        width: var(--size);
        height: var(--size);
        border: 3px solid rgba(213, 196, 164, 0.2);
        border-top-color: var(--sabor-gold-main);
        border-radius: 50%;
        animation: sm-spin 0.8s linear infinite;
      }
      .label {
        font-family: var(--font-body);
        font-size: var(--fs-small);
        color: var(--sabor-gold-muted);
        letter-spacing: 0.04em;
      }
    `,
  ],
})
export class LoadingSpinnerComponent {
  size = input(40);
  label = input('Cargando…');
  showLabel = input(true);
}

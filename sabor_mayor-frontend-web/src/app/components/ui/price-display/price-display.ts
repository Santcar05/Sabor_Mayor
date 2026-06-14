import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

/** Muestra un precio en COP con la tipografía mono de marca. */
@Component({
  selector: 'app-price-display',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CopCurrencyPipe],
  template: `<span class="price" [class.lg]="large()">{{ amount() | cop }}</span>`,
  styles: [
    `
      .price {
        font-family: var(--font-mono);
        font-weight: 500;
        color: var(--sabor-gold-main);
        letter-spacing: -0.01em;
      }
      .price.lg {
        font-size: var(--fs-h3);
      }
    `,
  ],
})
export class PriceDisplayComponent {
  amount = input.required<number>();
  large = input(false);
}

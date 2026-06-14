import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../shared/services/cart.service';
import { CartItem } from '../../../shared/models/cart.model';
import { ButtonComponent } from '../../../components/ui/button/button';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

@Component({
  selector: 'app-carrito-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonComponent, CopCurrencyPipe],
  templateUrl: './carrito-page.html',
  styleUrl: './carrito-page.scss',
})
export class CarritoPageComponent {
  protected readonly cart = inject(CartService);

  protected trackByItem(_: number, item: CartItem): string {
    return item.dishId + (item.notes ?? '');
  }

  protected onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = 'assets/images/dish-placeholder.svg';
  }
}

import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { forkJoin, catchError, of } from 'rxjs';
import { AuthService } from '../../shared/services/auth.service';
import { ReservationService } from '../../shared/services/reservation.service';
import { OrderService } from '../../shared/services/order.service';
import { LoyaltyService } from '../../shared/services/loyalty.service';
import { UserAvatarComponent } from '../../components/ui/user-avatar/user-avatar';
import { LoadingSpinnerComponent } from '../../components/ui/loading-spinner/loading-spinner';
import { CopCurrencyPipe } from '../../shared/pipes/cop-currency.pipe';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-perfil-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UserAvatarComponent, LoadingSpinnerComponent, CopCurrencyPipe, DatePipe],
  templateUrl: './perfil-page.html',
  styleUrl: './perfil-page.scss',
})
export class PerfilPageComponent {
  protected readonly auth = inject(AuthService);
  private readonly reservations = inject(ReservationService);
  private readonly orders = inject(OrderService);
  private readonly loyalty = inject(LoyaltyService);

  protected readonly data = toSignal(
    forkJoin({
      reservations: this.reservations.myReservations().pipe(catchError(() => of([]))),
      orders: this.orders.myOrders().pipe(catchError(() => of([]))),
      loyalty: this.loyalty.getAccount().pipe(catchError(() => of(null))),
    }),
    { initialValue: null },
  );

  protected readonly loading = computed(() => !this.data());
  protected readonly upcomingReservations = computed(
    () => this.data()?.reservations.filter((r) => r.status === 'CONFIRMED' || r.status === 'PENDING').slice(0, 3) ?? [],
  );
  protected readonly recentOrders = computed(() => this.data()?.orders.slice(0, 3) ?? []);
  protected readonly loyaltyAccount = computed(() => this.data()?.loyalty ?? null);
}

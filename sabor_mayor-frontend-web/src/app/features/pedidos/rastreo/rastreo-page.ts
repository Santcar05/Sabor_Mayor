import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap } from 'rxjs';
import { map } from 'rxjs/operators';
import { OrderTrackingService } from '../../../shared/services/order-tracking.service';
import { Order, OrderStatus } from '../../../shared/models/order.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

interface TrackStep {
  key: string;
  label: string;
  statuses: OrderStatus[];
}

@Component({
  selector: 'app-rastreo-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, LoadingSpinnerComponent, CopCurrencyPipe],
  templateUrl: './rastreo-page.html',
  styleUrl: './rastreo-page.scss',
})
export class RastreoPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly tracking = inject(OrderTrackingService);

  protected readonly steps: TrackStep[] = [
    { key: 'received', label: 'Pedido recibido', statuses: ['CREATED', 'CONFIRMED'] },
    { key: 'kitchen', label: 'En preparación', statuses: ['IN_KITCHEN'] },
    { key: 'ready', label: 'Listo', statuses: ['READY'] },
    { key: 'serving', label: 'En camino / servido', statuses: ['SERVED', 'DELIVERED'] },
    { key: 'done', label: 'Completado', statuses: ['PAID'] },
  ];

  protected readonly order = toSignal<Order | null | undefined>(
    this.route.paramMap.pipe(
      map((p) => p.get('id') ?? ''),
      switchMap((id) => (id ? this.tracking.track(id).pipe(catchError(() => of(null))) : of(null))),
    ),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this.order() === undefined);
  protected readonly error = computed(() => this.order() === null);

  protected readonly currentStep = computed(() => {
    const o = this.order();
    if (!o) return -1;
    if (o.status === 'CANCELLED') return -2;
    const idx = this.steps.findIndex((s) => s.statuses.includes(o.status));
    return idx;
  });

  protected stepState(index: number): 'done' | 'active' | 'pending' {
    const current = this.currentStep();
    if (index < current) return 'done';
    if (index === current) return 'active';
    return 'pending';
  }

  protected eta(): string {
    const c = this.currentStep();
    if (c <= 0) return '~30 min';
    if (c === 1) return '~20 min';
    if (c === 2) return '~10 min';
    return 'Muy pronto';
  }

  protected shareWhatsapp(o: Order): void {
    const text = encodeURIComponent(`¡Mi pedido en Sabor Mayor va en camino! Estado: ${o.status}`);
    window.open(`https://wa.me/?text=${text}`, '_blank');
  }
}

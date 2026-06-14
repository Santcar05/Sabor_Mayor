import { ChangeDetectionStrategy, Component, inject, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { forkJoin, catchError, of } from 'rxjs';
import { LoyaltyService } from '../../../shared/services/loyalty.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { LoyaltyAccount, LoyaltyTransaction, LEVEL_LABELS, LEVEL_THRESHOLDS } from '../../../shared/models/loyalty.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-puntos-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [LoadingSpinnerComponent, ButtonComponent, DatePipe],
  templateUrl: './puntos-page.html',
  styleUrl: './puntos-page.scss',
})
export class PuntosPageComponent {
  private readonly loyaltySvc = inject(LoyaltyService);
  private readonly notify = inject(NotificationService);

  private readonly _data = toSignal(
    forkJoin({
      account: this.loyaltySvc.getAccount().pipe(catchError(() => of(null))),
      transactions: this.loyaltySvc.getTransactions().pipe(catchError(() => of([]))),
    }),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => !this._data());
  protected readonly account = computed((): LoyaltyAccount | null => this._data()?.account ?? null);
  protected readonly transactions = computed((): LoyaltyTransaction[] => this._data()?.transactions ?? []);

  protected readonly redeemAmount = signal(0);
  protected readonly redeeming = signal(false);

  protected readonly levelLabel = computed(() => {
    const a = this.account();
    if (!a) return '';
    return LEVEL_LABELS[a.level] ?? a.level;
  });

  protected readonly nextThreshold = computed(() => {
    const a = this.account();
    if (!a) return 0;
    const thresholds = Object.entries(LEVEL_THRESHOLDS).sort((x, y) => x[1] - y[1]);
    const next = thresholds.find(([, t]) => t > a.lifetimePoints);
    return next ? next[1] : a.lifetimePoints;
  });

  protected readonly progressPct = computed(() => {
    const a = this.account();
    if (!a) return 0;
    return Math.min(100, (a.lifetimePoints / Math.max(this.nextThreshold(), 1)) * 100);
  });

  protected redeem(): void {
    const pts = this.redeemAmount();
    if (pts <= 0 || this.redeeming()) return;
    this.redeeming.set(true);
    this.loyaltySvc.redeem(pts).subscribe({
      next: () => {
        this.notify.success(`Canjeaste ${pts} Sabor Points`);
        this.redeemAmount.set(0);
        this.redeeming.set(false);
      },
      error: () => {
        this.notify.error('No se pudo realizar el canje');
        this.redeeming.set(false);
      },
    });
  }
}

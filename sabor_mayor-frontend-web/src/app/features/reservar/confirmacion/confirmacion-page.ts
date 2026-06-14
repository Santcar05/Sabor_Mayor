import { ChangeDetectionStrategy, Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Reservation } from '../../../shared/models/reservation.model';
import { ButtonComponent } from '../../../components/ui/button/button';
import { CopCurrencyPipe } from '../../../shared/pipes/cop-currency.pipe';

@Component({
  selector: 'app-confirmacion-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonComponent, CopCurrencyPipe],
  templateUrl: './confirmacion-page.html',
  styleUrl: './confirmacion-page.scss',
})
export class ConfirmacionPageComponent {
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  protected readonly reservation = signal<Reservation | null>(this.readReservation());

  private readReservation(): Reservation | null {
    const navState = this.router.getCurrentNavigation()?.extras.state;
    const state = navState ?? (this.isBrowser ? history.state : null);
    return (state?.['reservation'] as Reservation) ?? null;
  }

  protected readonly code = computed(() => this.reservation()?.id.slice(0, 8).toUpperCase() ?? '');

  protected readonly qrUrl = computed(() => {
    const r = this.reservation();
    if (!r) return '';
    const payload = encodeURIComponent(`SABORMAYOR|${r.id}|${r.date}|${r.time}`);
    return `https://api.qrserver.com/v1/create-qr-code/?size=220x220&bgcolor=20-16-12&color=243-181-74&data=${payload}`;
  });

  protected shareWhatsapp(): void {
    const r = this.reservation();
    if (!r) return;
    const text = encodeURIComponent(
      `¡Reservé en Sabor Mayor! ${r.date} a las ${r.time} para ${r.partySize} personas. Confirmación: ${this.code()}`,
    );
    window.open(`https://wa.me/?text=${text}`, '_blank');
  }

  protected print(): void {
    window.print();
  }
}

import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

/**
 * Modal genérico. Controlado por `open`. Proyecta contenido y un slot de
 * acciones opcional (select=[modal-actions]).
 */
@Component({
  selector: 'app-modal',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('backdrop', [
      transition(':enter', [style({ opacity: 0 }), animate('200ms ease-out', style({ opacity: 1 }))]),
      transition(':leave', [animate('160ms ease-in', style({ opacity: 0 }))]),
    ]),
    trigger('panel', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(24px) scale(0.97)' }),
        animate('300ms cubic-bezier(0.16,1,0.3,1)', style({ opacity: 1, transform: 'none' })),
      ]),
      transition(':leave', [
        animate('180ms ease-in', style({ opacity: 0, transform: 'translateY(16px)' })),
      ]),
    ]),
  ],
  template: `
    @if (open()) {
      <div class="backdrop" @backdrop (click)="onBackdrop()">
        <div
          class="panel"
          @panel
          role="dialog"
          aria-modal="true"
          [style.--max-width]="maxWidth()"
          (click)="$event.stopPropagation()"
        >
          <header class="modal-head">
            @if (title()) {
              <h3 class="modal-title">{{ title() }}</h3>
            }
            <button class="close" (click)="closed.emit()" aria-label="Cerrar">×</button>
          </header>
          <div class="modal-body"><ng-content /></div>
          <footer class="modal-foot"><ng-content select="[modal-actions]" /></footer>
        </div>
      </div>
    }
  `,
  styleUrl: './modal.scss',
})
export class ModalComponent {
  open = input(false);
  title = input('');
  maxWidth = input('560px');
  closeOnBackdrop = input(true);
  readonly closed = output<void>();

  protected onBackdrop(): void {
    if (this.closeOnBackdrop()) this.closed.emit();
  }
}

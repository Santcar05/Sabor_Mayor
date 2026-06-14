import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { NotificationService } from '../../../shared/services/notification.service';

/** Pila de toasts; se monta una vez en el AppComponent. */
@Component({
  selector: 'app-toast-container',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('toastAnim', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateX(40px) scale(0.96)' }),
        animate('320ms cubic-bezier(0.16,1,0.3,1)', style({ opacity: 1, transform: 'none' })),
      ]),
      transition(':leave', [
        animate('220ms ease-in', style({ opacity: 0, transform: 'translateX(40px)' })),
      ]),
    ]),
  ],
  template: `
    <div class="toast-stack" aria-live="polite" aria-atomic="true">
      @for (toast of notify.toasts(); track toast.id) {
        <div class="toast toast--{{ toast.type }}" @toastAnim role="status">
          <span class="icon">{{ icon(toast.type) }}</span>
          <div class="body">
            @if (toast.title) {
              <strong class="title">{{ toast.title }}</strong>
            }
            <span class="msg">{{ toast.message }}</span>
          </div>
          <button class="close" (click)="notify.dismiss(toast.id)" aria-label="Cerrar">×</button>
        </div>
      }
    </div>
  `,
  styleUrl: './toast-container.scss',
})
export class ToastContainerComponent {
  protected readonly notify = inject(NotificationService);

  protected icon(type: string): string {
    return { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' }[type] ?? 'ℹ';
  }
}

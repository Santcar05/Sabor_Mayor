import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

/**
 * Botón de marca reutilizable. Soporta variantes, tamaños, estado de carga,
 * icono y modo "block". Usar como <button app-button> o <a app-button>.
 */
@Component({
  selector: 'button[app-button], a[app-button]',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <span class="spinner" aria-hidden="true"></span>
    }
    <span class="content" [class.is-hidden]="loading()">
      <ng-content />
    </span>
  `,
  styleUrl: './button.scss',
  host: {
    '[class]': 'hostClasses()',
    '[attr.disabled]': 'isDisabled() ? "" : null',
    '[attr.aria-busy]': 'loading()',
    '(click)': 'onClick($event)',
  },
})
export class ButtonComponent {
  variant = input<ButtonVariant>('primary');
  size = input<ButtonSize>('md');
  loading = input(false);
  disabled = input(false);
  block = input(false);
  pulse = input(false);

  readonly pressed = output<MouseEvent>();

  protected isDisabled = () => this.disabled() || this.loading();

  protected hostClasses() {
    return [
      'sm-btn',
      `sm-btn--${this.variant()}`,
      `sm-btn--${this.size()}`,
      this.block() ? 'sm-btn--block' : '',
      this.loading() ? 'is-loading' : '',
      this.pulse() ? 'sm-btn--pulse' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  protected onClick(event: Event): void {
    if (this.isDisabled()) {
      event.preventDefault();
      event.stopImmediatePropagation();
      return;
    }
    this.pressed.emit(event as MouseEvent);
  }
}

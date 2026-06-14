import { ChangeDetectionStrategy, Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Input reutilizable con label flotante, estados de error y soporte de
 * ControlValueAccessor (funciona con Reactive y Template forms).
 */
@Component({
  selector: 'app-form-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => FormInputComponent), multi: true },
  ],
  template: `
    <label class="field" [class.has-error]="invalid()" [class.has-value]="!!value()">
      <span class="label">{{ label() }}@if (required()) {<span class="req">*</span>}</span>
      @if (type() === 'textarea') {
        <textarea
          class="control"
          [rows]="rows()"
          [placeholder]="placeholder()"
          [value]="value()"
          [attr.aria-invalid]="invalid()"
          (input)="onInput($event)"
          (blur)="onTouched()"
        ></textarea>
      } @else {
        <input
          class="control"
          [type]="type()"
          [placeholder]="placeholder()"
          [value]="value()"
          [attr.autocomplete]="autocomplete()"
          [attr.aria-invalid]="invalid()"
          (input)="onInput($event)"
          (blur)="onTouched()"
        />
      }
      @if (invalid() && error()) {
        <span class="error-msg">{{ error() }}</span>
      } @else if (hint()) {
        <span class="hint">{{ hint() }}</span>
      }
    </label>
  `,
  styleUrl: './form-input.scss',
})
export class FormInputComponent implements ControlValueAccessor {
  label = input('');
  type = input<'text' | 'email' | 'password' | 'tel' | 'number' | 'date' | 'textarea'>('text');
  placeholder = input('');
  hint = input('');
  error = input('');
  required = input(false);
  invalid = input(false);
  rows = input(3);
  autocomplete = input<string | null>(null);

  protected value = signal('');
  private onChange: (v: string) => void = () => {};
  protected onTouched: () => void = () => {};

  protected onInput(event: Event): void {
    const v = (event.target as HTMLInputElement | HTMLTextAreaElement).value;
    this.value.set(v);
    this.onChange(v);
  }

  writeValue(value: string): void {
    this.value.set(value ?? '');
  }
  registerOnChange(fn: (v: string) => void): void {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}

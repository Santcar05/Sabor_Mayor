import { Pipe, PipeTransform } from '@angular/core';

/** Formatea un número como pesos colombianos: 42000 -> "$42.000". */
@Pipe({ name: 'cop', standalone: true })
export class CopCurrencyPipe implements PipeTransform {
  transform(value: number | null | undefined, withSymbol = true): string {
    if (value === null || value === undefined || isNaN(value)) return withSymbol ? '$0' : '0';
    const formatted = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(value);
    return withSymbol ? `$${formatted}` : formatted;
  }
}

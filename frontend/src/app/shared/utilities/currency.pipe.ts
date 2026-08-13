import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats a number as Malaysian Ringgit, e.g. 12500.5 -> "RM 12,500.50".
 * A small custom pipe rather than Angular's CurrencyPipe so the "RM"
 * prefix and grouping are consistent without depending on registered
 * locale data for ms-MY/en-MY.
 */
@Pipe({ name: 'myr', standalone: true })
export class MyrCurrencyPipe implements PipeTransform {
  private readonly formatter = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  transform(value: number | string | null | undefined): string {
    if (value === null || value === undefined || value === '') {
      return 'RM 0.00';
    }
    const numeric = typeof value === 'string' ? parseFloat(value) : value;
    if (Number.isNaN(numeric)) {
      return 'RM 0.00';
    }
    return `RM ${this.formatter.format(numeric)}`;
  }
}

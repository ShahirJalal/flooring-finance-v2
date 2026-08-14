/** Malaysian date convention: DD/MM/YYYY. */
export const DATE_FORMAT_DISPLAY = 'dd/MM/yyyy';
/** PrimeNG p-datepicker uses its own (lowercase) token format. */
export const DATE_FORMAT_PRIME = 'dd/mm/yy';

/**
 * p-datepicker binds to a JS Date, but the backend's LocalDate fields are
 * plain "yyyy-MM-dd" strings over the wire - these two helpers convert at
 * the form/API boundary so components never juggle both types themselves.
 */
export function toIsoDate(date: Date | null | undefined): string | null {
  if (!date) {
    return null;
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function toDateObject(iso: string | null | undefined): Date | null {
  if (!iso) {
    return null;
  }
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

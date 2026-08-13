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

/**
 * Shared quick-select date ranges for Dashboard and Reports, so both pages
 * offer the same shortcuts instead of forcing two raw date pickers every
 * time. "Custom" just switches the pickers into free-entry mode - it never
 * changes the dates itself.
 */
export type DatePreset = 'thisMonth' | 'last3Months' | 'last12Months' | 'thisYear' | 'custom';

export const DATE_PRESET_OPTIONS: { label: string; value: DatePreset }[] = [
  { label: 'This Month', value: 'thisMonth' },
  { label: 'Last 3 Months', value: 'last3Months' },
  { label: 'Last 12 Months', value: 'last12Months' },
  { label: 'This Year', value: 'thisYear' },
  { label: 'Custom', value: 'custom' },
];

export function computePresetRange(preset: DatePreset): { from: Date; to: Date } | null {
  // p-selectButton (allowEmpty) or any other caller can hand this a value
  // outside the DatePreset union at runtime (TS types don't exist post-compile) -
  // treat anything unrecognized as "custom" rather than silently building a
  // range with an undefined `from`, which p-datepicker just renders blank.
  const now = new Date();
  const to = now;
  switch (preset) {
    case 'thisMonth': return { from: new Date(now.getFullYear(), now.getMonth(), 1), to };
    case 'last3Months': return { from: new Date(now.getFullYear(), now.getMonth() - 2, 1), to };
    case 'last12Months': return { from: new Date(now.getFullYear(), now.getMonth() - 11, 1), to };
    case 'thisYear': return { from: new Date(now.getFullYear(), 0, 1), to };
    default: return null;
  }
}

/** Which preset (if any) the given range matches - keeps the selector's highlighted option in sync after a manual date edit. */
export function matchPreset(from: Date | null, to: Date | null): DatePreset {
  if (!from || !to) {
    return 'custom';
  }
  for (const option of DATE_PRESET_OPTIONS) {
    if (option.value === 'custom') continue;
    const range = computePresetRange(option.value)!;
    if (isSameDay(range.from, from) && isSameDay(range.to, to)) {
      return option.value;
    }
  }
  return 'custom';
}

function isSameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

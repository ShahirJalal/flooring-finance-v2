/** Enums mirrored from the backend (see com.flooring.finance.common.*). */

export const MALAYSIAN_STATES = [
  'JOHOR', 'KEDAH', 'KELANTAN', 'MELAKA', 'NEGERI_SEMBILAN', 'PAHANG',
  'PENANG', 'PERAK', 'PERLIS', 'SABAH', 'SARAWAK', 'SELANGOR', 'TERENGGANU',
  'KUALA_LUMPUR', 'LABUAN', 'PUTRAJAYA'
] as const;
export type MalaysianState = typeof MALAYSIAN_STATES[number];

export const JOB_STATUSES = ['IN_PROGRESS', 'COMPLETED', 'CANCELLED'] as const;
export type JobStatus = typeof JOB_STATUSES[number];

/** Suggested categories for "Other Costs" - a free-text field, these are only starting suggestions. */
export const OTHER_COST_CATEGORIES = ['Petrol', 'Parking', 'Utilities', 'Tools', 'Supplies', 'Other'];

export function toLabel(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map(w => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');
}

export function enumOptions<T extends readonly string[]>(values: T): { label: string; value: T[number] }[] {
  return values.map(v => ({ label: toLabel(v), value: v }));
}

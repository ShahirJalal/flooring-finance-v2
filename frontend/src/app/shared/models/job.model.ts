export type EntryCategory = 'INCOME' | 'MATERIALS' | 'WORKER' | 'DELIVERY' | 'OTHER';

export const ENTRY_CATEGORIES: { label: string; value: EntryCategory }[] = [
  { label: 'Income', value: 'INCOME' },
  { label: 'Materials', value: 'MATERIALS' },
  { label: 'Worker', value: 'WORKER' },
  { label: 'Delivery', value: 'DELIVERY' },
  { label: 'Other', value: 'OTHER' },
];

export function entryCategoryLabel(category: EntryCategory): string {
  return ENTRY_CATEGORIES.find(c => c.value === category)?.label ?? category;
}

/** One line the owner wrote himself - a description, an amount and a category. */
export interface JobEntry {
  id?: number;
  category: EntryCategory;
  description?: string | null;
  amount: number;
}

/** A description + category he's typed before, offered back as a one-tap suggestion. */
export interface EntrySuggestion {
  description: string;
  category: EntryCategory;
}

export interface JobRequest {
  name: string;
  customerName?: string | null;
  location?: string | null;
  jobDate?: string | null;
  notes?: string | null;
  entries: JobEntry[];
}

/** Lightweight row for the Jobs list - name/customer, date and profit only. */
export interface JobSummary {
  id: number;
  name: string;
  customerName?: string | null;
  jobDate?: string | null;
  profit: number;
}

/** Full job detail payload: job info + its entries + computed totals. */
export interface Job {
  id: number;
  name: string;
  customerName?: string | null;
  location?: string | null;
  jobDate?: string | null;
  notes?: string | null;
  entries: JobEntry[];
  totalIncome: number;
  totalCost: number;
  profit: number;
  createdAt: string;
  updatedAt: string;
}

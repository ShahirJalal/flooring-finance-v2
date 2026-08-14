export interface JobRequest {
  name: string;
  customerName?: string | null;
  location?: string | null;
  jobDate?: string | null;
  notes?: string | null;
  collectionAmount: number;
  materialsCost: number;
  workerRatePerDay?: number | null;
  workerDays?: number | null;
  workerCost: number;
  otherCosts: number;
}

/** Lightweight row for the Jobs list - name/customer, date and profit only. */
export interface JobSummary {
  id: number;
  name: string;
  customerName?: string | null;
  jobDate?: string | null;
  collectionAmount: number;
  totalCost: number;
  profit: number;
}

/** Full job detail payload: job info + the three flat cost totals + computed totals. */
export interface Job {
  id: number;
  name: string;
  customerName?: string | null;
  location?: string | null;
  jobDate?: string | null;
  notes?: string | null;
  collectionAmount: number;
  materialsCost: number;
  workerRatePerDay?: number | null;
  workerDays?: number | null;
  workerCost: number;
  otherCosts: number;
  totalCost: number;
  profit: number;
  createdAt: string;
  updatedAt: string;
}

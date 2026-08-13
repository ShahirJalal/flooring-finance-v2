import { JobStatus, MalaysianState } from './enums';

export interface MaterialCost {
  id: number;
  description: string;
  amount: number;
  notes?: string | null;
}

export interface MaterialCostRequest {
  description: string;
  amount: number;
  notes?: string | null;
}

export interface DeliveryCost {
  id: number;
  description: string;
  amount: number;
  date?: string | null;
  notes?: string | null;
}

export interface DeliveryCostRequest {
  description: string;
  amount: number;
  date?: string | null;
  notes?: string | null;
}

export interface OtherCost {
  id: number;
  description: string;
  amount: number;
  date?: string | null;
  category?: string | null;
  notes?: string | null;
}

export interface OtherCostRequest {
  description: string;
  amount: number;
  date?: string | null;
  category?: string | null;
  notes?: string | null;
}

export interface WorkerCost {
  id: number;
  workerName: string;
  amount: number;
  notes?: string | null;
}

export interface WorkerCostRequest {
  workerName: string;
  amount: number;
  notes?: string | null;
}

export interface WorkerFoodCost {
  id: number;
  date?: string | null;
  description?: string | null;
  amount: number;
  notes?: string | null;
}

export interface WorkerFoodCostRequest {
  date?: string | null;
  description?: string | null;
  amount: number;
  notes?: string | null;
}

export interface JobRequest {
  name: string;
  customerName?: string | null;
  location?: string | null;
  state?: MalaysianState | null;
  jobDate?: string | null;
  status?: JobStatus | null;
  notes?: string | null;
  collectionAmount: number;
}

/** Lightweight row for the Jobs list and dashboard's recent-jobs list. */
export interface JobSummary {
  id: number;
  name: string;
  customerName?: string | null;
  location?: string | null;
  state?: MalaysianState | null;
  jobDate?: string | null;
  status: JobStatus;
  collectionAmount: number;
  totalCost: number;
  profit: number;
  profitMarginPercent: number;
}

/** Full job detail payload: job info + every cost line item + computed totals. */
export interface Job {
  id: number;
  name: string;
  customerName?: string | null;
  location?: string | null;
  state?: MalaysianState | null;
  jobDate?: string | null;
  status: JobStatus;
  notes?: string | null;
  collectionAmount: number;
  materialCosts: MaterialCost[];
  deliveryCosts: DeliveryCost[];
  otherCosts: OtherCost[];
  workerCosts: WorkerCost[];
  workerFoodCosts: WorkerFoodCost[];
  materialsTotal: number;
  deliveryTotal: number;
  otherCostsTotal: number;
  workerSalaryTotal: number;
  workerFoodTotal: number;
  totalCost: number;
  profit: number;
  profitMarginPercent: number;
  createdAt: string;
  updatedAt: string;
}

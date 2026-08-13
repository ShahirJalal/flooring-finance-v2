export interface MonthlySummaryRow {
  month: string;
  collection: number;
  cost: number;
  profit: number;
  profitMarginPercent: number;
  jobCount: number;
}

export interface JobProfitabilityRow {
  jobId: number;
  jobName: string;
  collection: number;
  cost: number;
  profit: number;
  profitMarginPercent: number;
}

export interface CostBreakdown {
  materials: number;
  delivery: number;
  otherCosts: number;
  workerSalary: number;
  workerFood: number;
}

import { JobSummary } from './job.model';

export interface DashboardSummary {
  totalCollection: number;
  totalCost: number;
  totalProfit: number;
  jobCount: number;
  recentJobs: JobSummary[];
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CostBreakdown, JobProfitabilityRow, MonthlySummaryRow } from '../../shared/models/report.model';

export interface DateRange {
  from?: string | null;
  to?: string | null;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly baseUrl = '/api/reports';

  constructor(private readonly http: HttpClient) {}

  private range(range?: DateRange): string {
    const params: string[] = [];
    if (range?.from) params.push(`from=${range.from}`);
    if (range?.to) params.push(`to=${range.to}`);
    return params.length ? `?${params.join('&')}` : '';
  }

  monthlySummary(range?: DateRange): Observable<MonthlySummaryRow[]> {
    return this.http.get<MonthlySummaryRow[]>(`${this.baseUrl}/monthly-summary${this.range(range)}`);
  }

  jobProfitability(range?: DateRange): Observable<JobProfitabilityRow[]> {
    return this.http.get<JobProfitabilityRow[]>(`${this.baseUrl}/job-profitability${this.range(range)}`);
  }

  costBreakdown(range?: DateRange): Observable<CostBreakdown> {
    return this.http.get<CostBreakdown>(`${this.baseUrl}/cost-breakdown${this.range(range)}`);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardSummary } from '../../shared/models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly http: HttpClient) {}

  summary(from?: string | null, to?: string | null): Observable<DashboardSummary> {
    const query: string[] = [];
    if (from) query.push(`from=${from}`);
    if (to) query.push(`to=${to}`);
    return this.http.get<DashboardSummary>(`/api/dashboard/summary${query.length ? '?' + query.join('&') : ''}`);
  }
}

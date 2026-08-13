import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { JobStatus } from '../../shared/models/enums';
import {
  DeliveryCostRequest,
  Job,
  JobRequest,
  JobSummary,
  MaterialCostRequest,
  OtherCostRequest,
  WorkerCostRequest,
  WorkerFoodCostRequest,
} from '../../shared/models/job.model';

export interface JobSearchParams {
  search?: string;
  status?: JobStatus;
  from?: string | null;
  to?: string | null;
}

@Injectable({ providedIn: 'root' })
export class JobService {
  private readonly baseUrl = '/api/jobs';

  constructor(private readonly http: HttpClient) {}

  search(params?: JobSearchParams): Observable<JobSummary[]> {
    const query: string[] = [];
    if (params?.search) query.push(`search=${encodeURIComponent(params.search)}`);
    if (params?.status) query.push(`status=${params.status}`);
    if (params?.from) query.push(`from=${params.from}`);
    if (params?.to) query.push(`to=${params.to}`);
    return this.http.get<JobSummary[]>(`${this.baseUrl}${query.length ? '?' + query.join('&') : ''}`);
  }

  findById(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.baseUrl}/${id}`);
  }

  create(request: JobRequest): Observable<Job> {
    return this.http.post<Job>(this.baseUrl, request);
  }

  update(id: number, request: JobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // --- Cost line items: every mutation returns the refreshed Job with updated totals. ---

  addMaterial(jobId: number, request: MaterialCostRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/${jobId}/materials`, request);
  }

  removeMaterial(jobId: number, costId: number): Observable<Job> {
    return this.http.delete<Job>(`${this.baseUrl}/${jobId}/materials/${costId}`);
  }

  addDelivery(jobId: number, request: DeliveryCostRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/${jobId}/delivery`, request);
  }

  removeDelivery(jobId: number, costId: number): Observable<Job> {
    return this.http.delete<Job>(`${this.baseUrl}/${jobId}/delivery/${costId}`);
  }

  addOtherCost(jobId: number, request: OtherCostRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/${jobId}/other-costs`, request);
  }

  removeOtherCost(jobId: number, costId: number): Observable<Job> {
    return this.http.delete<Job>(`${this.baseUrl}/${jobId}/other-costs/${costId}`);
  }

  addWorkerCost(jobId: number, request: WorkerCostRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/${jobId}/worker-costs`, request);
  }

  removeWorkerCost(jobId: number, costId: number): Observable<Job> {
    return this.http.delete<Job>(`${this.baseUrl}/${jobId}/worker-costs/${costId}`);
  }

  addWorkerFood(jobId: number, request: WorkerFoodCostRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/${jobId}/worker-food`, request);
  }

  removeWorkerFood(jobId: number, costId: number): Observable<Job> {
    return this.http.delete<Job>(`${this.baseUrl}/${jobId}/worker-food/${costId}`);
  }
}

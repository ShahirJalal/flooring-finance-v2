import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EntrySuggestion, Job, JobRequest, JobSummary } from '../../shared/models/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  private readonly baseUrl = '/api/jobs';

  constructor(private readonly http: HttpClient) {}

  list(search?: string): Observable<JobSummary[]> {
    const query = search ? `?search=${encodeURIComponent(search)}` : '';
    return this.http.get<JobSummary[]>(`${this.baseUrl}${query}`);
  }

  entrySuggestions(): Observable<EntrySuggestion[]> {
    return this.http.get<EntrySuggestion[]>(`${this.baseUrl}/entry-suggestions`);
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
}

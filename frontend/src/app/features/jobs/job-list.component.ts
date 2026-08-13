import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { enumOptions, JOB_STATUSES, JobStatus, MALAYSIAN_STATES } from '../../shared/models/enums';
import { JobRequest, JobSummary } from '../../shared/models/job.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

interface JobFormModel {
  name: string;
  customerName: string | null;
  location: string | null;
  state: JobRequest['state'];
  jobDate: Date | null;
  status: JobStatus;
  notes: string | null;
  collectionAmount: number | null;
}

const EMPTY_FORM: JobFormModel = {
  name: '', customerName: null, location: null, state: null,
  jobDate: new Date(), status: 'IN_PROGRESS', notes: null, collectionAmount: null,
};

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, TableModule, DialogModule, TagModule,
    InputTextModule, InputNumberModule, TextareaModule, SelectModule, DatePickerModule, TooltipModule, MyrCurrencyPipe,
  ],
  templateUrl: './job-list.component.html',
  styleUrl: './job-list.component.scss',
})
export class JobListComponent implements OnInit {
  readonly jobs = signal<JobSummary[]>([]);
  readonly loading = signal(true);
  readonly dialogVisible = signal(false);
  readonly stateOptions = enumOptions(MALAYSIAN_STATES);
  readonly statusOptions = enumOptions(JOB_STATUSES);
  readonly dateFormat = DATE_FORMAT_PRIME;

  searchText = '';
  statusFilter: JobStatus | null = null;

  form: JobFormModel = { ...EMPTY_FORM };

  constructor(
    private readonly jobService: JobService,
    private readonly confirmationService: ConfirmationService,
    private readonly messageService: MessageService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.jobService.search({ search: this.searchText || undefined, status: this.statusFilter ?? undefined }).subscribe(jobs => {
      this.jobs.set(jobs);
      this.loading.set(false);
    });
  }

  openCreate(): void {
    this.form = { ...EMPTY_FORM };
    this.dialogVisible.set(true);
  }

  save(): void {
    if (!this.form.name?.trim() || this.form.collectionAmount === null) {
      this.messageService.add({ severity: 'warn', summary: 'Job name and collection amount are required' });
      return;
    }
    const request: JobRequest = {
      name: this.form.name,
      customerName: this.form.customerName,
      location: this.form.location,
      state: this.form.state,
      jobDate: toIsoDate(this.form.jobDate),
      status: this.form.status,
      notes: this.form.notes,
      collectionAmount: this.form.collectionAmount,
    };
    this.jobService.create(request).subscribe(job => {
      this.dialogVisible.set(false);
      this.messageService.add({ severity: 'success', summary: 'Job created' });
      this.router.navigate(['/jobs', job.id]);
    });
  }

  remove(job: JobSummary): void {
    this.confirmationService.confirm({
      message: `Delete job "${job.name}"? This cannot be undone.`,
      header: 'Confirm deletion',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.jobService.delete(job.id).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Job removed.' });
          this.load();
        });
      },
    });
  }

  statusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'IN_PROGRESS': return 'info';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  }
}

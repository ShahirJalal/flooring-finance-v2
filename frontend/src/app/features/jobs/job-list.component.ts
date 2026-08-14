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
import { TableModule } from 'primeng/table';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { JobRequest, JobSummary } from '../../shared/models/job.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

interface JobFormModel {
  name: string;
  customerName: string | null;
  location: string | null;
  jobDate: Date | null;
  notes: string | null;
  collectionAmount: number | null;
  materialsCost: number | null;
  workerRatePerDay: number | null;
  workerDays: number | null;
  workerCost: number | null;
  otherCosts: number | null;
}

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, TableModule, DialogModule,
    InputTextModule, InputNumberModule, TextareaModule, DatePickerModule, TooltipModule, MyrCurrencyPipe,
  ],
  templateUrl: './job-list.component.html',
  styleUrl: './job-list.component.scss',
})
export class JobListComponent implements OnInit {
  readonly jobs = signal<JobSummary[]>([]);
  readonly loading = signal(true);
  readonly dialogVisible = signal(false);
  readonly moreDetailsExpanded = signal(false);
  readonly dateFormat = DATE_FORMAT_PRIME;

  form: JobFormModel = this.emptyForm();

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
    this.jobService.list().subscribe(jobs => {
      this.jobs.set(jobs);
      this.loading.set(false);
    });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.moreDetailsExpanded.set(false);
    this.dialogVisible.set(true);
  }

  /** So the job name field is never blank/required-feeling - just a starting point the owner can overwrite or leave as-is. */
  private emptyForm(): JobFormModel {
    return {
      name: `Job ${this.jobs().length + 1}`, customerName: null, location: null,
      jobDate: new Date(), notes: null, collectionAmount: null,
      materialsCost: null, workerRatePerDay: null, workerDays: null, workerCost: null, otherCosts: null,
    };
  }

  /** Rate x days is just a convenience calculator - it only fills workerCost, which stays directly editable. */
  onWorkerInputsChange(): void {
    if (this.form.workerRatePerDay != null && this.form.workerDays != null) {
      this.form.workerCost = Math.round(this.form.workerRatePerDay * this.form.workerDays * 100) / 100;
    }
  }

  get previewTotalCost(): number {
    return (this.form.materialsCost ?? 0) + (this.form.workerCost ?? 0) + (this.form.otherCosts ?? 0);
  }

  get previewProfit(): number {
    return (this.form.collectionAmount ?? 0) - this.previewTotalCost;
  }

  save(): void {
    if (this.form.collectionAmount === null) {
      this.messageService.add({ severity: 'warn', summary: 'Enter the price for this job' });
      return;
    }
    const request: JobRequest = {
      name: this.form.name?.trim() || this.emptyForm().name,
      customerName: this.form.customerName,
      location: this.form.location,
      jobDate: toIsoDate(this.form.jobDate),
      notes: this.form.notes,
      collectionAmount: this.form.collectionAmount,
      materialsCost: this.form.materialsCost ?? 0,
      workerRatePerDay: this.form.workerRatePerDay,
      workerDays: this.form.workerDays,
      workerCost: this.form.workerCost ?? 0,
      otherCosts: this.form.otherCosts ?? 0,
    };
    this.jobService.create(request).subscribe(job => {
      this.dialogVisible.set(false);
      this.messageService.add({ severity: 'success', summary: 'Job saved' });
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
}

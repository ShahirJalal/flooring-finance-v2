import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SkeletonModule } from 'primeng/skeleton';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { Job, JobRequest } from '../../shared/models/job.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toDateObject, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, SkeletonModule,
    DialogModule, InputNumberModule, InputTextModule, TextareaModule, DatePickerModule, TooltipModule, MyrCurrencyPipe,
  ],
  templateUrl: './job-detail.component.html',
})
export class JobDetailComponent implements OnInit {
  readonly job = signal<Job | null>(null);
  readonly loading = signal(true);

  readonly jobDialogVisible = signal(false);
  readonly moreDetailsExpanded = signal(false);

  readonly dateFormat = DATE_FORMAT_PRIME;

  jobId!: number;

  jobForm = {
    name: '', customerName: null as string | null, location: null as string | null,
    jobDate: null as Date | null, notes: null as string | null,
    collectionAmount: null as number | null,
    materialsCost: null as number | null,
    workerRatePerDay: null as number | null,
    workerDays: null as number | null,
    workerCost: null as number | null,
    otherCosts: null as number | null,
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly jobService: JobService,
    private readonly confirmationService: ConfirmationService,
    private readonly messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.jobId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.jobService.findById(this.jobId).subscribe(job => {
      this.job.set(job);
      this.loading.set(false);
    });
  }

  openEditJob(): void {
    const j = this.job();
    if (!j) return;
    this.jobForm = {
      name: j.name,
      customerName: j.customerName ?? null,
      location: j.location ?? null,
      jobDate: toDateObject(j.jobDate),
      notes: j.notes ?? null,
      collectionAmount: j.collectionAmount,
      materialsCost: j.materialsCost,
      workerRatePerDay: j.workerRatePerDay ?? null,
      workerDays: j.workerDays ?? null,
      workerCost: j.workerCost,
      otherCosts: j.otherCosts,
    };
    this.moreDetailsExpanded.set(!!(j.customerName || j.location || j.notes));
    this.jobDialogVisible.set(true);
  }

  /** Rate x days is just a convenience calculator - it only fills workerCost, which stays directly editable. */
  onWorkerInputsChange(): void {
    if (this.jobForm.workerRatePerDay != null && this.jobForm.workerDays != null) {
      this.jobForm.workerCost = Math.round(this.jobForm.workerRatePerDay * this.jobForm.workerDays * 100) / 100;
    }
  }

  get previewTotalCost(): number {
    return (this.jobForm.materialsCost ?? 0) + (this.jobForm.workerCost ?? 0) + (this.jobForm.otherCosts ?? 0);
  }

  get previewProfit(): number {
    return (this.jobForm.collectionAmount ?? 0) - this.previewTotalCost;
  }

  saveJob(): void {
    if (this.jobForm.collectionAmount === null) {
      this.messageService.add({ severity: 'warn', summary: 'Enter the price for this job' });
      return;
    }
    const request: JobRequest = {
      name: this.jobForm.name?.trim() || this.job()!.name,
      customerName: this.jobForm.customerName,
      location: this.jobForm.location,
      jobDate: toIsoDate(this.jobForm.jobDate),
      notes: this.jobForm.notes,
      collectionAmount: this.jobForm.collectionAmount,
      materialsCost: this.jobForm.materialsCost ?? 0,
      workerRatePerDay: this.jobForm.workerRatePerDay,
      workerDays: this.jobForm.workerDays,
      workerCost: this.jobForm.workerCost ?? 0,
      otherCosts: this.jobForm.otherCosts ?? 0,
    };
    this.jobService.update(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.jobDialogVisible.set(false);
      this.messageService.add({ severity: 'success', summary: 'Saved' });
    });
  }

  deleteJob(): void {
    const j = this.job();
    if (!j) return;
    this.confirmationService.confirm({
      message: `Delete job "${j.name}"? This cannot be undone.`,
      header: 'Confirm deletion',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.jobService.delete(this.jobId).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Job deleted' });
          this.router.navigate(['/jobs']);
        });
      },
    });
  }
}

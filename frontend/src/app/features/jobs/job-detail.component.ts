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
import { SelectModule } from 'primeng/select';
import { SkeletonModule } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import {
  DeliveryCost, DeliveryCostRequest, Job, JobRequest,
  MaterialCost, MaterialCostRequest, OtherCost, OtherCostRequest,
  WorkerCost, WorkerCostRequest, WorkerFoodCost, WorkerFoodCostRequest,
} from '../../shared/models/job.model';
import { enumOptions, JOB_STATUSES, JobStatus, MALAYSIAN_STATES, OTHER_COST_CATEGORIES } from '../../shared/models/enums';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toDateObject, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, SkeletonModule, TableModule, TagModule,
    DialogModule, SelectModule, InputNumberModule, InputTextModule, TextareaModule, DatePickerModule, TooltipModule, MyrCurrencyPipe,
  ],
  templateUrl: './job-detail.component.html',
})
export class JobDetailComponent implements OnInit {
  readonly job = signal<Job | null>(null);
  readonly loading = signal(true);

  readonly jobDialogVisible = signal(false);
  readonly materialDialogVisible = signal(false);
  readonly deliveryDialogVisible = signal(false);
  readonly otherCostDialogVisible = signal(false);
  readonly workerDialogVisible = signal(false);
  readonly foodDialogVisible = signal(false);

  readonly stateOptions = enumOptions(MALAYSIAN_STATES);
  readonly statusOptions = enumOptions(JOB_STATUSES);
  readonly otherCostCategories = OTHER_COST_CATEGORIES;
  readonly dateFormat = DATE_FORMAT_PRIME;

  jobId!: number;

  jobForm = {
    name: '', customerName: null as string | null, location: null as string | null,
    state: null as JobRequest['state'], jobDate: null as Date | null, status: 'IN_PROGRESS' as JobStatus,
    notes: null as string | null, collectionAmount: null as number | null,
  };

  materialForm: MaterialCostRequest = { description: '', amount: 0, notes: null };
  deliveryForm: { description: string; amount: number | null; date: Date | null; notes: string | null } =
    { description: '', amount: null, date: new Date(), notes: null };
  otherCostForm: { description: string; amount: number | null; date: Date | null; category: string | null; notes: string | null } =
    { description: '', amount: null, date: new Date(), category: null, notes: null };
  workerForm: WorkerCostRequest = { workerName: '', amount: 0, notes: null };
  foodForm: { date: Date | null; description: string | null; amount: number | null; notes: string | null } =
    { date: new Date(), description: null, amount: null, notes: null };

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

  // --- Job info / collection ---

  openEditJob(): void {
    const j = this.job();
    if (!j) return;
    this.jobForm = {
      name: j.name,
      customerName: j.customerName ?? null,
      location: j.location ?? null,
      state: j.state ?? null,
      jobDate: toDateObject(j.jobDate),
      status: j.status,
      notes: j.notes ?? null,
      collectionAmount: j.collectionAmount,
    };
    this.jobDialogVisible.set(true);
  }

  saveJob(): void {
    if (!this.jobForm.name?.trim() || this.jobForm.collectionAmount === null) {
      this.messageService.add({ severity: 'warn', summary: 'Job name and collection amount are required' });
      return;
    }
    const request: JobRequest = {
      name: this.jobForm.name,
      customerName: this.jobForm.customerName,
      location: this.jobForm.location,
      state: this.jobForm.state,
      jobDate: toIsoDate(this.jobForm.jobDate),
      status: this.jobForm.status,
      notes: this.jobForm.notes,
      collectionAmount: this.jobForm.collectionAmount,
    };
    this.jobService.update(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.jobDialogVisible.set(false);
      this.messageService.add({ severity: 'success', summary: 'Saved' });
    });
  }

  markCompleted(): void {
    const j = this.job();
    if (!j) return;
    const request: JobRequest = {
      name: j.name, customerName: j.customerName, location: j.location, state: j.state,
      jobDate: j.jobDate, status: 'COMPLETED', notes: j.notes, collectionAmount: j.collectionAmount,
    };
    this.jobService.update(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.messageService.add({ severity: 'success', summary: 'Job marked as completed' });
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

  // --- Materials ---

  openAddMaterial(): void {
    this.materialForm = { description: '', amount: 0, notes: null };
    this.materialDialogVisible.set(true);
  }

  saveMaterial(): void {
    if (!this.materialForm.description?.trim() || !this.materialForm.amount) {
      this.messageService.add({ severity: 'warn', summary: 'Description and amount are required' });
      return;
    }
    this.jobService.addMaterial(this.jobId, this.materialForm).subscribe(job => {
      this.job.set(job);
      this.materialDialogVisible.set(false);
    });
  }

  removeMaterial(cost: MaterialCost): void {
    this.confirm(`Remove "${cost.description}"?`, () =>
      this.jobService.removeMaterial(this.jobId, cost.id).subscribe(job => this.job.set(job)));
  }

  // --- Delivery ---

  openAddDelivery(): void {
    this.deliveryForm = { description: '', amount: null, date: new Date(), notes: null };
    this.deliveryDialogVisible.set(true);
  }

  saveDelivery(): void {
    if (!this.deliveryForm.description?.trim() || !this.deliveryForm.amount) {
      this.messageService.add({ severity: 'warn', summary: 'Description and amount are required' });
      return;
    }
    const request: DeliveryCostRequest = {
      description: this.deliveryForm.description,
      amount: this.deliveryForm.amount,
      date: toIsoDate(this.deliveryForm.date),
      notes: this.deliveryForm.notes,
    };
    this.jobService.addDelivery(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.deliveryDialogVisible.set(false);
    });
  }

  removeDelivery(cost: DeliveryCost): void {
    this.confirm(`Remove "${cost.description}"?`, () =>
      this.jobService.removeDelivery(this.jobId, cost.id).subscribe(job => this.job.set(job)));
  }

  // --- Other costs ---

  openAddOtherCost(): void {
    this.otherCostForm = { description: '', amount: null, date: new Date(), category: null, notes: null };
    this.otherCostDialogVisible.set(true);
  }

  saveOtherCost(): void {
    if (!this.otherCostForm.description?.trim() || !this.otherCostForm.amount) {
      this.messageService.add({ severity: 'warn', summary: 'Description and amount are required' });
      return;
    }
    const request: OtherCostRequest = {
      description: this.otherCostForm.description,
      amount: this.otherCostForm.amount,
      date: toIsoDate(this.otherCostForm.date),
      category: this.otherCostForm.category,
      notes: this.otherCostForm.notes,
    };
    this.jobService.addOtherCost(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.otherCostDialogVisible.set(false);
    });
  }

  removeOtherCost(cost: OtherCost): void {
    this.confirm(`Remove "${cost.description}"?`, () =>
      this.jobService.removeOtherCost(this.jobId, cost.id).subscribe(job => this.job.set(job)));
  }

  // --- Worker salary ---

  openAddWorker(): void {
    this.workerForm = { workerName: '', amount: 0, notes: null };
    this.workerDialogVisible.set(true);
  }

  saveWorker(): void {
    if (!this.workerForm.workerName?.trim() || !this.workerForm.amount) {
      this.messageService.add({ severity: 'warn', summary: 'Worker name and amount are required' });
      return;
    }
    this.jobService.addWorkerCost(this.jobId, this.workerForm).subscribe(job => {
      this.job.set(job);
      this.workerDialogVisible.set(false);
    });
  }

  removeWorker(cost: WorkerCost): void {
    this.confirm(`Remove "${cost.workerName}"?`, () =>
      this.jobService.removeWorkerCost(this.jobId, cost.id).subscribe(job => this.job.set(job)));
  }

  // --- Worker food ---

  openAddFood(): void {
    this.foodForm = { date: new Date(), description: null, amount: null, notes: null };
    this.foodDialogVisible.set(true);
  }

  saveFood(): void {
    if (!this.foodForm.amount) {
      this.messageService.add({ severity: 'warn', summary: 'Amount is required' });
      return;
    }
    const request: WorkerFoodCostRequest = {
      date: toIsoDate(this.foodForm.date),
      description: this.foodForm.description,
      amount: this.foodForm.amount,
      notes: this.foodForm.notes,
    };
    this.jobService.addWorkerFood(this.jobId, request).subscribe(job => {
      this.job.set(job);
      this.foodDialogVisible.set(false);
    });
  }

  removeFood(cost: WorkerFoodCost): void {
    this.confirm(`Remove this entry?`, () =>
      this.jobService.removeWorkerFood(this.jobId, cost.id).subscribe(job => this.job.set(job)));
  }

  statusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'IN_PROGRESS': return 'info';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  }

  private confirm(message: string, onAccept: () => void): void {
    this.confirmationService.confirm({
      message,
      header: 'Confirm removal',
      icon: 'pi pi-exclamation-triangle',
      accept: onAccept,
    });
  }
}

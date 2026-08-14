import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AutoCompleteCompleteEvent, AutoCompleteModule, AutoCompleteSelectEvent } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SkeletonModule } from 'primeng/skeleton';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { ENTRY_CATEGORIES, entryCategoryLabel, EntryCategory, EntrySuggestion, Job, JobEntry, JobRequest } from '../../shared/models/job.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toDateObject, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, SkeletonModule,
    DialogModule, SelectModule, SelectButtonModule, InputNumberModule, InputTextModule, TextareaModule, DatePickerModule, TooltipModule,
    AutoCompleteModule, MyrCurrencyPipe,
  ],
  templateUrl: './job-detail.component.html',
})
export class JobDetailComponent implements OnInit {
  readonly job = signal<Job | null>(null);
  readonly loading = signal(true);

  readonly jobDialogVisible = signal(false);
  readonly moreDetailsExpanded = signal(false);

  readonly dateFormat = DATE_FORMAT_PRIME;
  readonly categoryLabel = entryCategoryLabel;
  readonly typeOptions: { label: string; value: 'EXPENSE' | 'INCOME' }[] = [
    { label: 'Expense', value: 'EXPENSE' },
    { label: 'Income', value: 'INCOME' },
  ];
  readonly expenseCategoryOptions = ENTRY_CATEGORIES.filter(c => c.value !== 'INCOME');
  readonly incomeCategoryOptions = ENTRY_CATEGORIES.filter(c => c.value === 'INCOME');

  /** Everything he's typed before, for the entry-description autocomplete. */
  private suggestions: EntrySuggestion[] = [];
  filteredSuggestions: string[] = [];

  jobId!: number;

  jobForm = {
    name: '', customerName: null as string | null, location: null as string | null,
    jobDate: null as Date | null, notes: null as string | null,
    entries: [] as JobEntry[],
  };
  /** The entry currently being composed - only joins jobForm.entries once he taps Add. */
  draft: JobEntry = { category: 'MATERIALS', description: null, amount: null as unknown as number };

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
    this.jobService.entrySuggestions().subscribe(s => (this.suggestions = s));
  }

  /** Filters past descriptions by substring match, deduped, most recent first. */
  searchSuggestions(event: AutoCompleteCompleteEvent): void {
    const query = event.query.trim().toLowerCase();
    const seen = new Set<string>();
    const results: string[] = [];
    for (const s of this.suggestions) {
      const key = s.description.toLowerCase();
      if (seen.has(key) || (query && !key.includes(query))) continue;
      seen.add(key);
      results.push(s.description);
      if (results.length >= 8) break;
    }
    this.filteredSuggestions = results;
  }

  /** Picking a past description also carries over the category it was last used with. */
  onDescriptionSelected(entry: JobEntry, event: AutoCompleteSelectEvent): void {
    const match = this.suggestions.find(s => s.description.toLowerCase() === String(event.value).toLowerCase());
    if (match) {
      entry.category = match.category;
    }
  }

  /** Category is always shown - Expense picks from Materials/Worker/Delivery/Other, Income is just Income. */
  entryType(entry: JobEntry): 'EXPENSE' | 'INCOME' {
    return entry.category === 'INCOME' ? 'INCOME' : 'EXPENSE';
  }

  setEntryType(entry: JobEntry, type: 'EXPENSE' | 'INCOME'): void {
    if (type === 'INCOME') {
      entry.category = 'INCOME';
    } else if (entry.category === 'INCOME') {
      entry.category = 'MATERIALS';
    }
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
      entries: j.entries.map(e => ({ ...e })),
    };
    this.draft = { category: 'MATERIALS', description: null, amount: null as unknown as number };
    this.moreDetailsExpanded.set(!!(j.customerName || j.location || j.notes));
    this.jobDialogVisible.set(true);
  }

  /** Moves the composer's current values into the entries list, then clears description/amount for the next one. */
  addDraftEntry(): void {
    if (this.draft.amount == null || this.draft.amount <= 0) {
      this.messageService.add({ severity: 'warn', summary: 'Enter an amount for this entry' });
      return;
    }
    this.jobForm.entries.push({
      category: this.draft.category,
      description: this.draft.description?.trim() || null,
      amount: this.draft.amount,
    });
    this.draft = { category: this.draft.category, description: null, amount: null as unknown as number };
  }

  removeEntry(index: number): void {
    this.jobForm.entries.splice(index, 1);
  }

  private sum(category: EntryCategory | null, exclude?: EntryCategory): number {
    return this.jobForm.entries
      .filter(e => (category ? e.category === category : e.category !== exclude))
      .reduce((total, e) => total + (e.amount ?? 0), 0);
  }

  get previewTotalIncome(): number {
    return this.sum('INCOME');
  }

  get previewTotalCost(): number {
    return this.sum(null, 'INCOME');
  }

  get previewProfit(): number {
    return this.previewTotalIncome - this.previewTotalCost;
  }

  saveJob(): void {
    if (this.jobForm.entries.length === 0) {
      this.messageService.add({ severity: 'warn', summary: 'Add at least one entry' });
      return;
    }
    const request: JobRequest = {
      name: this.jobForm.name?.trim() || this.job()!.name,
      customerName: this.jobForm.customerName,
      location: this.jobForm.location,
      jobDate: toIsoDate(this.jobForm.jobDate),
      notes: this.jobForm.notes,
      entries: this.jobForm.entries,
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

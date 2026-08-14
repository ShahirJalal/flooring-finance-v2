import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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
import { TableModule } from 'primeng/table';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { ENTRY_CATEGORIES, entryCategoryLabel, EntrySuggestion, EntryCategory, JobEntry, JobRequest, JobSummary } from '../../shared/models/job.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import { DATE_FORMAT_PRIME, toIsoDate } from '../../shared/utilities/date.constants';
import { JobService } from './job.service';

interface JobFormModel {
  name: string;
  customerName: string | null;
  location: string | null;
  jobDate: Date | null;
  notes: string | null;
  entries: JobEntry[];
}

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, TableModule, DialogModule,
    InputTextModule, InputNumberModule, TextareaModule, SelectModule, SelectButtonModule, DatePickerModule, TooltipModule,
    AutoCompleteModule, MyrCurrencyPipe,
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

  form: JobFormModel = this.emptyForm();
  /** The entry currently being composed - only joins form.entries once he taps Add. */
  draft: JobEntry = this.emptyDraft();

  constructor(
    private readonly jobService: JobService,
    private readonly confirmationService: ConfirmationService,
    private readonly messageService: MessageService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
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
    this.jobService.list().subscribe(jobs => {
      this.jobs.set(jobs);
      this.loading.set(false);
    });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.draft = this.emptyDraft();
    this.moreDetailsExpanded.set(false);
    this.dialogVisible.set(true);
  }

  /** So the job name field is never blank/required-feeling - just a starting point the owner can overwrite or leave as-is. */
  private emptyForm(): JobFormModel {
    return {
      name: `Job ${this.jobs().length + 1}`, customerName: null, location: null,
      jobDate: new Date(), notes: null,
      entries: [],
    };
  }

  private emptyDraft(): JobEntry {
    return { category: 'INCOME', description: null, amount: null as unknown as number };
  }

  /** Moves the composer's current values into the entries list, then clears description/amount for the next one. */
  addDraftEntry(): void {
    if (this.draft.amount == null || this.draft.amount <= 0) {
      this.messageService.add({ severity: 'warn', summary: 'Enter an amount for this entry' });
      return;
    }
    this.form.entries.push({
      category: this.draft.category,
      description: this.draft.description?.trim() || null,
      amount: this.draft.amount,
    });
    // Keep the same type/category selected - handy when logging several similar entries in a row.
    this.draft = { category: this.draft.category, description: null, amount: null as unknown as number };
  }

  removeEntry(index: number): void {
    this.form.entries.splice(index, 1);
  }

  private sum(category: EntryCategory | null, exclude?: EntryCategory): number {
    return this.form.entries
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

  save(): void {
    if (this.form.entries.length === 0) {
      this.messageService.add({ severity: 'warn', summary: 'Add at least one entry' });
      return;
    }
    const request: JobRequest = {
      name: this.form.name?.trim() || this.emptyForm().name,
      customerName: this.form.customerName,
      location: this.form.location,
      jobDate: toIsoDate(this.form.jobDate),
      notes: this.form.notes,
      entries: this.form.entries,
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

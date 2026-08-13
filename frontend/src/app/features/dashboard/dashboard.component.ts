import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SkeletonModule } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { DashboardSummary } from '../../shared/models/dashboard.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import {
  computePresetRange, DATE_FORMAT_PRIME, DATE_PRESET_OPTIONS, DatePreset, matchPreset, toIsoDate,
} from '../../shared/utilities/date.constants';
import { DashboardService } from './dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, SkeletonModule,
    TableModule, TagModule, DatePickerModule, SelectButtonModule, MyrCurrencyPipe,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(true);
  readonly dateFormat = DATE_FORMAT_PRIME;
  readonly presetOptions = DATE_PRESET_OPTIONS;

  selectedPreset: DatePreset = 'thisMonth';
  from: Date = computePresetRange('thisMonth')!.from;
  to: Date = computePresetRange('thisMonth')!.to;

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.refresh();
  }

  applyPreset(preset: DatePreset): void {
    this.selectedPreset = preset;
    const range = computePresetRange(preset);
    if (!range) return; // "Custom" - leave the dates as-is, wait for the owner to pick and hit Apply.
    this.from = range.from;
    this.to = range.to;
    this.refresh();
  }

  onDateChange(): void {
    this.selectedPreset = matchPreset(this.from, this.to);
  }

  refresh(): void {
    this.loading.set(true);
    this.dashboardService.summary(toIsoDate(this.from), toIsoDate(this.to)).subscribe(summary => {
      this.summary.set(summary);
      this.loading.set(false);
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

import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { ThemeService } from '../../core/theme/theme.service';
import { CostBreakdown, JobProfitabilityRow, MonthlySummaryRow } from '../../shared/models/report.model';
import { MyrCurrencyPipe } from '../../shared/utilities/currency.pipe';
import {
  computePresetRange, DATE_FORMAT_PRIME, DATE_PRESET_OPTIONS, DatePreset, matchPreset, toIsoDate,
} from '../../shared/utilities/date.constants';
import { ReportService } from './report.service';

/**
 * Chart series colors. Fixed and validated (see the project's dataviz
 * palette) rather than auto-generated - a plain green/red money pair fails
 * CVD separation (deutan ΔE 4.2), so Collection/Cost instead reuse the
 * validated categorical slots 1 (blue) and 8 (red), which pass every check
 * in both themes. The cost-breakdown categories use slots 1-5 in their
 * documented order.
 */
const MONEY_COLORS = {
  light: { collection: '#2a78d6', cost: '#e34948' },
  dark: { collection: '#3987e5', cost: '#e66767' },
};

const BREAKDOWN_COLORS = {
  light: ['#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4'],
  dark: ['#3987e5', '#d95926', '#199e70', '#c98500', '#d55181'],
};

/**
 * Chart chrome (axis ticks, legend text, gridlines). Chart.js/canvas needs a
 * literal color, and PrimeNG's dark-mode tokens are defined with the CSS
 * `light-dark()` function - reading them via getComputedStyle().getPropertyValue()
 * returns that raw, unevaluated function text (custom properties are never
 * resolved by that API), not an actual color. Handing that string to Chart.js
 * silently fails and canvas falls back to its default black. Hardcoded
 * light/dark pairs, matching the same slate ramp the rest of the UI already
 * renders with, sidestep the problem entirely.
 */
const CHART_CHROME = {
  light: { text: '#334155', muted: '#64748b', grid: '#e2e8f0' },
  dark: { text: '#ffffff', muted: '#94a3b8', grid: '#334155' },
};

function formatMonth(yyyyMM: string): string {
  const [year, month] = yyyyMM.split('-').map(Number);
  if (!year || !month) return yyyyMM;
  return new Intl.DateTimeFormat('en-US', { month: 'short', year: 'numeric' }).format(new Date(year, month - 1, 1));
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, CardModule, ChartModule, TableModule,
    DatePickerModule, SelectButtonModule, MyrCurrencyPipe,
  ],
  templateUrl: './reports.component.html',
})
export class ReportsComponent implements OnInit {
  readonly dateFormat = DATE_FORMAT_PRIME;
  readonly presetOptions = DATE_PRESET_OPTIONS;

  selectedPreset: DatePreset = 'last12Months';
  from: Date = computePresetRange('last12Months')!.from;
  to: Date = computePresetRange('last12Months')!.to;

  readonly monthlySummary = signal<MonthlySummaryRow[]>([]);
  readonly jobProfitability = signal<JobProfitabilityRow[]>([]);
  readonly costBreakdown = signal<CostBreakdown | null>(null);

  readonly monthlyChartData = computed(() => {
    const rows = this.monthlySummary();
    const colors = MONEY_COLORS[this.themeService.theme()];
    return {
      labels: rows.map(r => formatMonth(r.month)),
      datasets: [
        { label: 'Collection', data: rows.map(r => r.collection), backgroundColor: colors.collection, borderRadius: 4, maxBarThickness: 28 },
        { label: 'Total Cost', data: rows.map(r => r.cost), backgroundColor: colors.cost, borderRadius: 4, maxBarThickness: 28 },
      ],
    };
  });

  readonly costBreakdownChartData = computed(() => {
    const cb = this.costBreakdown();
    const colors = BREAKDOWN_COLORS[this.themeService.theme()];
    const buckets: [string, number][] = cb
      ? [['Materials', cb.materials], ['Delivery', cb.delivery], ['Other Costs', cb.otherCosts], ['Worker Salary', cb.workerSalary], ['Worker Food', cb.workerFood]]
      : [];
    return {
      labels: [''],
      datasets: buckets.map(([label, value], i) => ({
        label,
        data: [value],
        backgroundColor: colors[i],
        borderRadius: 4,
      })),
    };
  });

  readonly chartOptions = computed(() => {
    const chrome = CHART_CHROME[this.themeService.theme()];
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom', labels: { color: chrome.text, usePointStyle: true, boxHeight: 8 } },
      },
      scales: {
        x: { ticks: { color: chrome.muted }, grid: { display: false } },
        y: { ticks: { color: chrome.muted, callback: (v: number) => `RM ${v}` }, grid: { color: chrome.grid }, beginAtZero: true },
      },
    };
  });

  readonly breakdownChartOptions = computed(() => {
    const chrome = CHART_CHROME[this.themeService.theme()];
    return {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom', labels: { color: chrome.text, usePointStyle: true, boxHeight: 8 } },
      },
      scales: {
        x: { stacked: true, beginAtZero: true, ticks: { color: chrome.muted, callback: (v: number) => `RM ${v}` }, grid: { color: chrome.grid } },
        y: { stacked: true, display: false, grid: { display: false } },
      },
    };
  });

  constructor(private readonly reportService: ReportService, private readonly themeService: ThemeService) {}

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
    const range = { from: toIsoDate(this.from), to: toIsoDate(this.to) };
    this.reportService.monthlySummary(range).subscribe(r => this.monthlySummary.set(r));
    this.reportService.jobProfitability(range).subscribe(r => this.jobProfitability.set(r));
    this.reportService.costBreakdown(range).subscribe(r => this.costBreakdown.set(r));
  }
}

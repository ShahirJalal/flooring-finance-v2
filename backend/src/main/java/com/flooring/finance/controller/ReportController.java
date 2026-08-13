package com.flooring.finance.controller;

import com.flooring.finance.dto.report.ReportDtos.CostBreakdownResponse;
import com.flooring.finance.dto.report.ReportDtos.JobProfitabilityRow;
import com.flooring.finance.dto.report.ReportDtos.MonthlySummaryRow;
import com.flooring.finance.service.ReportService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly-summary")
    public List<MonthlySummaryRow> monthlySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.monthlySummary(defaultFrom(from), defaultTo(to));
    }

    @GetMapping("/job-profitability")
    public List<JobProfitabilityRow> jobProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.jobProfitability(defaultFrom(from), defaultTo(to));
    }

    @GetMapping("/cost-breakdown")
    public CostBreakdownResponse costBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.costBreakdown(defaultFrom(from), defaultTo(to));
    }

    private LocalDate defaultFrom(LocalDate from) {
        return from != null ? from : LocalDate.now().minusMonths(12).withDayOfMonth(1);
    }

    private LocalDate defaultTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }
}

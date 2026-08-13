package com.flooring.finance.service;

import com.flooring.finance.dto.report.ReportDtos.CostBreakdownResponse;
import com.flooring.finance.dto.report.ReportDtos.JobProfitabilityRow;
import com.flooring.finance.dto.report.ReportDtos.MonthlySummaryRow;
import com.flooring.finance.entity.Job;
import com.flooring.finance.repository.JobRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple reports built directly on {@link JobCalculationService} - no
 * separate accounting/aggregation rules, just sums of the same per-job
 * numbers shown on the job detail page.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JobRepository jobRepository;
    private final JobCalculationService calculationService;

    public ReportService(JobRepository jobRepository, JobCalculationService calculationService) {
        this.jobRepository = jobRepository;
        this.calculationService = calculationService;
    }

    public List<MonthlySummaryRow> monthlySummary(LocalDate from, LocalDate to) {
        Map<String, BigDecimal[]> byMonth = new TreeMap<>(); // [0]=collection [1]=cost, plus a count map
        Map<String, Long> countByMonth = new TreeMap<>();

        for (Job job : jobRepository.findByJobDateBetween(from, to)) {
            String month = job.getJobDate() != null ? job.getJobDate().format(MONTH_FORMAT) : "Unscheduled";
            BigDecimal[] slot = byMonth.computeIfAbsent(month, m -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal collection = job.getCollectionAmount() != null ? job.getCollectionAmount() : BigDecimal.ZERO;
            slot[0] = slot[0].add(collection);
            slot[1] = slot[1].add(calculationService.calculateTotalCost(job));
            countByMonth.merge(month, 1L, Long::sum);
        }

        return byMonth.entrySet().stream()
                .map(e -> {
                    BigDecimal collection = e.getValue()[0];
                    BigDecimal cost = e.getValue()[1];
                    BigDecimal profit = collection.subtract(cost);
                    BigDecimal margin = collection.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : profit.divide(collection, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                    return new MonthlySummaryRow(e.getKey(), collection, cost, profit, margin, countByMonth.get(e.getKey()));
                })
                .toList();
    }

    public List<JobProfitabilityRow> jobProfitability(LocalDate from, LocalDate to) {
        return jobRepository.findByJobDateBetween(from, to).stream()
                .map(job -> {
                    var t = calculationService.calculateTotals(job);
                    return new JobProfitabilityRow(job.getId(), job.getName(), job.getCollectionAmount(), t.totalCost(), t.profit(), t.profitMarginPercent());
                })
                .toList();
    }

    public CostBreakdownResponse costBreakdown(LocalDate from, LocalDate to) {
        BigDecimal materials = BigDecimal.ZERO, delivery = BigDecimal.ZERO, other = BigDecimal.ZERO,
                workerSalary = BigDecimal.ZERO, workerFood = BigDecimal.ZERO;

        for (Job job : jobRepository.findByJobDateBetween(from, to)) {
            materials = materials.add(calculationService.calculateMaterialTotal(job));
            delivery = delivery.add(calculationService.calculateDeliveryTotal(job));
            other = other.add(calculationService.calculateOtherCostTotal(job));
            workerSalary = workerSalary.add(calculationService.calculateWorkerCostTotal(job));
            workerFood = workerFood.add(calculationService.calculateWorkerFoodTotal(job));
        }
        return new CostBreakdownResponse(materials, delivery, other, workerSalary, workerFood);
    }
}

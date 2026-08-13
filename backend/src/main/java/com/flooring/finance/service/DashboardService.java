package com.flooring.finance.service;

import com.flooring.finance.dto.DashboardSummaryResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.mapper.JobMapper;
import com.flooring.finance.repository.JobRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple dashboard aggregation: sums {@link JobCalculationService}'s
 * per-job numbers across whatever date range is requested. No separate
 * formulas live here.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final JobRepository jobRepository;
    private final JobCalculationService calculationService;

    public DashboardService(JobRepository jobRepository, JobCalculationService calculationService) {
        this.jobRepository = jobRepository;
        this.calculationService = calculationService;
    }

    public DashboardSummaryResponse summary(LocalDate from, LocalDate to) {
        var jobsInRange = jobRepository.findByJobDateBetween(from, to);

        BigDecimal totalCollection = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Job job : jobsInRange) {
            totalCollection = totalCollection.add(job.getCollectionAmount() != null ? job.getCollectionAmount() : BigDecimal.ZERO);
            totalCost = totalCost.add(calculationService.calculateTotalCost(job));
        }
        BigDecimal totalProfit = totalCollection.subtract(totalCost);

        var recentJobs = jobRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(job -> JobMapper.toSummaryResponse(job, calculationService.calculateTotals(job)))
                .toList();

        return new DashboardSummaryResponse(totalCollection, totalCost, totalProfit, jobsInRange.size(), recentJobs);
    }
}

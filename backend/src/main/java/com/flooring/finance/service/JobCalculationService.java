package com.flooring.finance.service;

import com.flooring.finance.entity.Job;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * The one place the job's financial numbers are calculated. This is
 * intentionally centralized (per the brief) since the owner may want to
 * change how profit is calculated later - only this class would need to
 * change.
 *
 * <pre>
 *   Total Cost = Materials + Worker + Other Costs
 *   Profit     = Collection - Total Cost
 * </pre>
 */
@Service
public class JobCalculationService {

    public BigDecimal calculateTotalCost(Job job) {
        return nz(job.getMaterialsCost()).add(nz(job.getWorkerCost())).add(nz(job.getOtherCosts()));
    }

    public BigDecimal calculateProfit(Job job) {
        return nz(job.getCollectionAmount()).subtract(calculateTotalCost(job));
    }

    /** Both numbers a job response needs, computed together in one pass. */
    public Totals calculateTotals(Job job) {
        BigDecimal totalCost = calculateTotalCost(job);
        BigDecimal profit = calculateProfit(job);
        return new Totals(totalCost, profit);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public record Totals(
            BigDecimal totalCost,
            BigDecimal profit
    ) {
    }
}

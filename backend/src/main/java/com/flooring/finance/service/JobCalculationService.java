package com.flooring.finance.service;

import com.flooring.finance.common.EntryCategory;
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
 *   Total Income = sum of entries tagged Income
 *   Total Cost   = sum of every other entry
 *   Profit       = Total Income - Total Cost
 * </pre>
 */
@Service
public class JobCalculationService {

    public Totals calculateTotals(Job job) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        for (var entry : job.getEntries()) {
            if (entry.getCategory() == EntryCategory.INCOME) {
                income = income.add(entry.getAmount());
            } else {
                cost = cost.add(entry.getAmount());
            }
        }
        return new Totals(income, cost, income.subtract(cost));
    }

    /** Both numbers a job response needs, computed together in one pass. */
    public record Totals(
            BigDecimal totalIncome,
            BigDecimal totalCost,
            BigDecimal profit
    ) {
    }
}

package com.flooring.finance.dto.report;

import java.math.BigDecimal;

/** Response shapes for the /api/reports/* endpoints. */
public final class ReportDtos {

    private ReportDtos() {
    }

    public record MonthlySummaryRow(
            String month,
            BigDecimal collection,
            BigDecimal cost,
            BigDecimal profit,
            BigDecimal profitMarginPercent,
            long jobCount
    ) {
    }

    public record JobProfitabilityRow(
            Long jobId,
            String jobName,
            BigDecimal collection,
            BigDecimal cost,
            BigDecimal profit,
            BigDecimal profitMarginPercent
    ) {
    }

    public record CostBreakdownResponse(
            BigDecimal materials,
            BigDecimal delivery,
            BigDecimal otherCosts,
            BigDecimal workerSalary,
            BigDecimal workerFood
    ) {
    }
}

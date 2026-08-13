package com.flooring.finance.dto;

import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal totalCollection,
        BigDecimal totalCost,
        BigDecimal totalProfit,
        long jobCount,
        List<JobSummaryResponse> recentJobs
) {
}

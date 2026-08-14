package com.flooring.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class JobDtos {

    private JobDtos() {
    }

    public record JobRequest(
            @NotBlank String name,
            String customerName,
            String location,
            LocalDate jobDate,
            String notes,
            @NotNull @PositiveOrZero BigDecimal collectionAmount,
            @NotNull @PositiveOrZero BigDecimal materialsCost,
            BigDecimal workerRatePerDay,
            Integer workerDays,
            @NotNull @PositiveOrZero BigDecimal workerCost,
            @NotNull @PositiveOrZero BigDecimal otherCosts
    ) {
    }

    /** Lightweight row for the Jobs list. */
    public record JobSummaryResponse(
            Long id,
            String name,
            String customerName,
            LocalDate jobDate,
            BigDecimal collectionAmount,
            BigDecimal totalCost,
            BigDecimal profit
    ) {
    }

    /**
     * The full job detail payload: job info and the computed financial
     * summary. All totals come from JobCalculationService, never computed
     * on the frontend.
     */
    public record JobResponse(
            Long id,
            String name,
            String customerName,
            String location,
            LocalDate jobDate,
            String notes,
            BigDecimal collectionAmount,
            BigDecimal materialsCost,
            BigDecimal workerRatePerDay,
            Integer workerDays,
            BigDecimal workerCost,
            BigDecimal otherCosts,
            BigDecimal totalCost,
            BigDecimal profit,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

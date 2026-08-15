package com.flooring.finance.dto;

import com.flooring.finance.common.EntryCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class JobDtos {

    private JobDtos() {
    }

    public record JobEntryRequest(
            @NotNull EntryCategory category,
            @Size(max = 255) String description,
            @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount
    ) {
    }

    public record JobEntryResponse(
            Long id,
            EntryCategory category,
            String description,
            BigDecimal amount
    ) {
    }

    public record JobRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 150) String customerName,
            @Size(max = 255) String location,
            LocalDate jobDate,
            String notes,
            @NotNull List<@Valid JobEntryRequest> entries
    ) {
    }

    /** Lightweight row for the Jobs list. */
    public record JobSummaryResponse(
            Long id,
            String name,
            String customerName,
            LocalDate jobDate,
            BigDecimal profit
    ) {
    }

    /**
     * The full job detail payload: job info, its entries, and the computed
     * financial summary. Totals always come from JobCalculationService,
     * never computed on the frontend.
     */
    public record JobResponse(
            Long id,
            String name,
            String customerName,
            String location,
            LocalDate jobDate,
            String notes,
            List<JobEntryResponse> entries,
            BigDecimal totalIncome,
            BigDecimal totalCost,
            BigDecimal profit,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

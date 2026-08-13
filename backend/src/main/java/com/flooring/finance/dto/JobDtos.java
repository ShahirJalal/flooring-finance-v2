package com.flooring.finance.dto;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.common.MalaysianState;
import com.flooring.finance.dto.DeliveryCostDtos.DeliveryCostResponse;
import com.flooring.finance.dto.MaterialCostDtos.MaterialCostResponse;
import com.flooring.finance.dto.OtherCostDtos.OtherCostResponse;
import com.flooring.finance.dto.WorkerCostDtos.WorkerCostResponse;
import com.flooring.finance.dto.WorkerFoodCostDtos.WorkerFoodCostResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class JobDtos {

    private JobDtos() {
    }

    public record JobRequest(
            @NotBlank String name,
            String customerName,
            String location,
            MalaysianState state,
            LocalDate jobDate,
            JobStatus status,
            String notes,
            @NotNull @PositiveOrZero BigDecimal collectionAmount
    ) {
    }

    /** Lightweight row for the Jobs list and dashboard's recent-jobs list. */
    public record JobSummaryResponse(
            Long id,
            String name,
            String customerName,
            String location,
            MalaysianState state,
            LocalDate jobDate,
            JobStatus status,
            BigDecimal collectionAmount,
            BigDecimal totalCost,
            BigDecimal profit,
            BigDecimal profitMarginPercent
    ) {
    }

    /**
     * The full job detail payload: job info, every cost line item, and the
     * computed financial summary - everything the job detail page needs in
     * one call. All totals come from JobCalculationService, never computed
     * on the frontend.
     */
    public record JobResponse(
            Long id,
            String name,
            String customerName,
            String location,
            MalaysianState state,
            LocalDate jobDate,
            JobStatus status,
            String notes,
            BigDecimal collectionAmount,
            List<MaterialCostResponse> materialCosts,
            List<DeliveryCostResponse> deliveryCosts,
            List<OtherCostResponse> otherCosts,
            List<WorkerCostResponse> workerCosts,
            List<WorkerFoodCostResponse> workerFoodCosts,
            BigDecimal materialsTotal,
            BigDecimal deliveryTotal,
            BigDecimal otherCostsTotal,
            BigDecimal workerSalaryTotal,
            BigDecimal workerFoodTotal,
            BigDecimal totalCost,
            BigDecimal profit,
            BigDecimal profitMarginPercent,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

package com.flooring.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public final class WorkerCostDtos {

    private WorkerCostDtos() {
    }

    public record WorkerCostRequest(
            @NotBlank String workerName,
            @NotNull @Positive BigDecimal amount,
            String notes
    ) {
    }

    public record WorkerCostResponse(Long id, String workerName, BigDecimal amount, String notes) {
    }
}

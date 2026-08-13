package com.flooring.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class WorkerFoodCostDtos {

    private WorkerFoodCostDtos() {
    }

    public record WorkerFoodCostRequest(
            LocalDate date,
            String description,
            @NotNull @Positive BigDecimal amount,
            String notes
    ) {
    }

    public record WorkerFoodCostResponse(Long id, LocalDate date, String description, BigDecimal amount, String notes) {
    }
}

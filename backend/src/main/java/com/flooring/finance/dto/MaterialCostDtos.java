package com.flooring.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public final class MaterialCostDtos {

    private MaterialCostDtos() {
    }

    public record MaterialCostRequest(
            @NotBlank String description,
            @NotNull @Positive BigDecimal amount,
            String notes
    ) {
    }

    public record MaterialCostResponse(Long id, String description, BigDecimal amount, String notes) {
    }
}

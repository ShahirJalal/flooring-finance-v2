package com.flooring.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class OtherCostDtos {

    private OtherCostDtos() {
    }

    public record OtherCostRequest(
            @NotBlank String description,
            @NotNull @Positive BigDecimal amount,
            LocalDate date,
            String category,
            String notes
    ) {
    }

    public record OtherCostResponse(Long id, String description, BigDecimal amount, LocalDate date, String category, String notes) {
    }
}

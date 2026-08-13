package com.flooring.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DeliveryCostDtos {

    private DeliveryCostDtos() {
    }

    public record DeliveryCostRequest(
            @NotBlank String description,
            @NotNull @Positive BigDecimal amount,
            LocalDate date,
            String notes
    ) {
    }

    public record DeliveryCostResponse(Long id, String description, BigDecimal amount, LocalDate date, String notes) {
    }
}

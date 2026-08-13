package com.flooring.finance.mapper;

import com.flooring.finance.dto.MaterialCostDtos.MaterialCostRequest;
import com.flooring.finance.dto.MaterialCostDtos.MaterialCostResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.MaterialCost;

public final class MaterialCostMapper {

    private MaterialCostMapper() {
    }

    public static MaterialCost toEntity(MaterialCostRequest r, Job job) {
        return MaterialCost.builder().job(job).description(r.description()).amount(r.amount()).notes(r.notes()).build();
    }

    public static MaterialCostResponse toResponse(MaterialCost c) {
        return new MaterialCostResponse(c.getId(), c.getDescription(), c.getAmount(), c.getNotes());
    }
}

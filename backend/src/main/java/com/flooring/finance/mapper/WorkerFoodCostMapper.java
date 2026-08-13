package com.flooring.finance.mapper;

import com.flooring.finance.dto.WorkerFoodCostDtos.WorkerFoodCostRequest;
import com.flooring.finance.dto.WorkerFoodCostDtos.WorkerFoodCostResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.WorkerFoodCost;

public final class WorkerFoodCostMapper {

    private WorkerFoodCostMapper() {
    }

    public static WorkerFoodCost toEntity(WorkerFoodCostRequest r, Job job) {
        return WorkerFoodCost.builder().job(job).date(r.date()).description(r.description()).amount(r.amount()).notes(r.notes()).build();
    }

    public static WorkerFoodCostResponse toResponse(WorkerFoodCost c) {
        return new WorkerFoodCostResponse(c.getId(), c.getDate(), c.getDescription(), c.getAmount(), c.getNotes());
    }
}

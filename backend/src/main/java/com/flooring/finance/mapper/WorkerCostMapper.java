package com.flooring.finance.mapper;

import com.flooring.finance.dto.WorkerCostDtos.WorkerCostRequest;
import com.flooring.finance.dto.WorkerCostDtos.WorkerCostResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.WorkerCost;

public final class WorkerCostMapper {

    private WorkerCostMapper() {
    }

    public static WorkerCost toEntity(WorkerCostRequest r, Job job) {
        return WorkerCost.builder().job(job).workerName(r.workerName()).amount(r.amount()).notes(r.notes()).build();
    }

    public static WorkerCostResponse toResponse(WorkerCost c) {
        return new WorkerCostResponse(c.getId(), c.getWorkerName(), c.getAmount(), c.getNotes());
    }
}

package com.flooring.finance.mapper;

import com.flooring.finance.dto.DeliveryCostDtos.DeliveryCostRequest;
import com.flooring.finance.dto.DeliveryCostDtos.DeliveryCostResponse;
import com.flooring.finance.entity.DeliveryCost;
import com.flooring.finance.entity.Job;

public final class DeliveryCostMapper {

    private DeliveryCostMapper() {
    }

    public static DeliveryCost toEntity(DeliveryCostRequest r, Job job) {
        return DeliveryCost.builder().job(job).description(r.description()).amount(r.amount()).date(r.date()).notes(r.notes()).build();
    }

    public static DeliveryCostResponse toResponse(DeliveryCost c) {
        return new DeliveryCostResponse(c.getId(), c.getDescription(), c.getAmount(), c.getDate(), c.getNotes());
    }
}

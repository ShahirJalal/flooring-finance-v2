package com.flooring.finance.mapper;

import com.flooring.finance.dto.OtherCostDtos.OtherCostRequest;
import com.flooring.finance.dto.OtherCostDtos.OtherCostResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.OtherCost;

public final class OtherCostMapper {

    private OtherCostMapper() {
    }

    public static OtherCost toEntity(OtherCostRequest r, Job job) {
        return OtherCost.builder()
                .job(job).description(r.description()).amount(r.amount())
                .date(r.date()).category(r.category()).notes(r.notes())
                .build();
    }

    public static OtherCostResponse toResponse(OtherCost c) {
        return new OtherCostResponse(c.getId(), c.getDescription(), c.getAmount(), c.getDate(), c.getCategory(), c.getNotes());
    }
}

package com.flooring.finance.mapper;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.service.JobCalculationService.Totals;

public final class JobMapper {

    private JobMapper() {
    }

    public static Job toEntity(JobRequest r) {
        return Job.builder()
                .name(r.name())
                .customerName(r.customerName())
                .location(r.location())
                .state(r.state())
                .jobDate(r.jobDate())
                .status(r.status() != null ? r.status() : JobStatus.IN_PROGRESS)
                .notes(r.notes())
                .collectionAmount(r.collectionAmount())
                .build();
    }

    public static void updateEntity(Job job, JobRequest r) {
        job.setName(r.name());
        job.setCustomerName(r.customerName());
        job.setLocation(r.location());
        job.setState(r.state());
        job.setJobDate(r.jobDate());
        if (r.status() != null) {
            job.setStatus(r.status());
        }
        job.setNotes(r.notes());
        job.setCollectionAmount(r.collectionAmount());
    }

    public static JobResponse toResponse(Job job, Totals t) {
        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getCustomerName(),
                job.getLocation(),
                job.getState(),
                job.getJobDate(),
                job.getStatus(),
                job.getNotes(),
                job.getCollectionAmount(),
                job.getMaterialCosts().stream().map(MaterialCostMapper::toResponse).toList(),
                job.getDeliveryCosts().stream().map(DeliveryCostMapper::toResponse).toList(),
                job.getOtherCosts().stream().map(OtherCostMapper::toResponse).toList(),
                job.getWorkerCosts().stream().map(WorkerCostMapper::toResponse).toList(),
                job.getWorkerFoodCosts().stream().map(WorkerFoodCostMapper::toResponse).toList(),
                t.materialsTotal(),
                t.deliveryTotal(),
                t.otherCostsTotal(),
                t.workerSalaryTotal(),
                t.workerFoodTotal(),
                t.totalCost(),
                t.profit(),
                t.profitMarginPercent(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    public static JobSummaryResponse toSummaryResponse(Job job, Totals t) {
        return new JobSummaryResponse(
                job.getId(),
                job.getName(),
                job.getCustomerName(),
                job.getLocation(),
                job.getState(),
                job.getJobDate(),
                job.getStatus(),
                job.getCollectionAmount(),
                t.totalCost(),
                t.profit(),
                t.profitMarginPercent()
        );
    }
}

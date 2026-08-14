package com.flooring.finance.mapper;

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
                .jobDate(r.jobDate())
                .notes(r.notes())
                .collectionAmount(r.collectionAmount())
                .materialsCost(r.materialsCost())
                .workerRatePerDay(r.workerRatePerDay())
                .workerDays(r.workerDays())
                .workerCost(r.workerCost())
                .otherCosts(r.otherCosts())
                .build();
    }

    public static void updateEntity(Job job, JobRequest r) {
        job.setName(r.name());
        job.setCustomerName(r.customerName());
        job.setLocation(r.location());
        job.setJobDate(r.jobDate());
        job.setNotes(r.notes());
        job.setCollectionAmount(r.collectionAmount());
        job.setMaterialsCost(r.materialsCost());
        job.setWorkerRatePerDay(r.workerRatePerDay());
        job.setWorkerDays(r.workerDays());
        job.setWorkerCost(r.workerCost());
        job.setOtherCosts(r.otherCosts());
    }

    public static JobResponse toResponse(Job job, Totals t) {
        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getCustomerName(),
                job.getLocation(),
                job.getJobDate(),
                job.getNotes(),
                job.getCollectionAmount(),
                job.getMaterialsCost(),
                job.getWorkerRatePerDay(),
                job.getWorkerDays(),
                job.getWorkerCost(),
                job.getOtherCosts(),
                t.totalCost(),
                t.profit(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    public static JobSummaryResponse toSummaryResponse(Job job, Totals t) {
        return new JobSummaryResponse(
                job.getId(),
                job.getName(),
                job.getCustomerName(),
                job.getJobDate(),
                job.getCollectionAmount(),
                t.totalCost(),
                t.profit()
        );
    }
}

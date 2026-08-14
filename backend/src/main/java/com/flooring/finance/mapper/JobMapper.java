package com.flooring.finance.mapper;

import com.flooring.finance.dto.JobDtos.JobEntryRequest;
import com.flooring.finance.dto.JobDtos.JobEntryResponse;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.JobEntry;
import com.flooring.finance.service.JobCalculationService.Totals;
import java.util.List;

public final class JobMapper {

    private JobMapper() {
    }

    public static Job toEntity(JobRequest r) {
        Job job = Job.builder()
                .name(r.name())
                .customerName(r.customerName())
                .location(r.location())
                .jobDate(r.jobDate())
                .notes(r.notes())
                .build();
        r.entries().forEach(er -> job.getEntries().add(toEntryEntity(job, er)));
        return job;
    }

    public static void updateEntity(Job job, JobRequest r) {
        job.setName(r.name());
        job.setCustomerName(r.customerName());
        job.setLocation(r.location());
        job.setJobDate(r.jobDate());
        job.setNotes(r.notes());

        // Replace-all: simplest correct way to sync a freeform, reorderable
        // list with orphanRemoval - clear then rebuild rather than diffing.
        job.getEntries().clear();
        r.entries().forEach(er -> job.getEntries().add(toEntryEntity(job, er)));
    }

    private static JobEntry toEntryEntity(Job job, JobEntryRequest r) {
        return JobEntry.builder()
                .job(job)
                .category(r.category())
                .description(blankToNull(r.description()))
                .amount(r.amount())
                .build();
    }

    public static JobResponse toResponse(Job job, Totals t) {
        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getCustomerName(),
                job.getLocation(),
                job.getJobDate(),
                job.getNotes(),
                toEntryResponses(job.getEntries()),
                t.totalIncome(),
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
                t.profit()
        );
    }

    private static List<JobEntryResponse> toEntryResponses(List<JobEntry> entries) {
        return entries.stream()
                .map(e -> new JobEntryResponse(e.getId(), e.getCategory(), e.getDescription(), e.getAmount()))
                .toList();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

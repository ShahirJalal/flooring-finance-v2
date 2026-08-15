package com.flooring.finance.service;

import com.flooring.finance.dto.EntrySuggestion;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.entity.Job;
import com.flooring.finance.exception.ResourceNotFoundException;
import com.flooring.finance.mapper.JobMapper;
import com.flooring.finance.repository.JobEntryRepository;
import com.flooring.finance.repository.JobRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the Job aggregate. Every mutation re-saves the job and every read
 * re-derives totals via {@link JobCalculationService} - nothing financial is
 * ever stored redundantly.
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final JobEntryRepository jobEntryRepository;
    private final JobCalculationService calculationService;

    public JobService(JobRepository jobRepository, JobEntryRepository jobEntryRepository, JobCalculationService calculationService) {
        this.jobRepository = jobRepository;
        this.jobEntryRepository = jobEntryRepository;
        this.calculationService = calculationService;
    }

    /** Backs the Jobs list - the app's home screen. Newest first. */
    @Transactional(readOnly = true)
    public List<JobSummaryResponse> list(String search) {
        String needle = blankToNull(search);
        return jobRepository.findAllWithEntries().stream()
                .filter(job -> needle == null || matches(job, needle))
                .sorted(Comparator.comparing(Job::getJobDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Job::getId, Comparator.reverseOrder()))
                .map(job -> JobMapper.toSummaryResponse(job, calculationService.calculateTotals(job)))
                .toList();
    }

    private boolean matches(Job job, String needle) {
        String lower = needle.toLowerCase();
        return containsIgnoreCase(job.getName(), lower) || containsIgnoreCase(job.getCustomerName(), lower);
    }

    private boolean containsIgnoreCase(String value, String lowerNeedle) {
        return value != null && value.toLowerCase().contains(lowerNeedle);
    }

    /** Backs the entry-description autocomplete - what has he typed before, and under which category. */
    @Transactional(readOnly = true)
    public List<EntrySuggestion> entrySuggestions() {
        return jobEntryRepository.findSuggestions();
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        Job job = getOrThrow(id);
        return JobMapper.toResponse(job, calculationService.calculateTotals(job));
    }

    public JobResponse create(JobRequest request) {
        // saveAndFlush (not save) so createdAt/updatedAt - populated by
        // Hibernate's @CreationTimestamp/@UpdateTimestamp only at flush time -
        // are already set on the entity before toResponse() reads them.
        // Otherwise the response can carry a stale value that only becomes
        // correct on a later GET, once the surrounding transaction commits.
        Job saved = jobRepository.saveAndFlush(JobMapper.toEntity(request));
        return JobMapper.toResponse(saved, calculationService.calculateTotals(saved));
    }

    public JobResponse update(Long id, JobRequest request) {
        Job job = getOrThrow(id);
        JobMapper.updateEntity(job, request);
        Job saved = jobRepository.saveAndFlush(job);
        return JobMapper.toResponse(saved, calculationService.calculateTotals(saved));
    }

    public void delete(Long id) {
        jobRepository.delete(getOrThrow(id));
    }

    Job getOrThrow(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

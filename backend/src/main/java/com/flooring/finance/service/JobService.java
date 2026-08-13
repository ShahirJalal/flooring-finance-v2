package com.flooring.finance.service;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.dto.DeliveryCostDtos.DeliveryCostRequest;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.dto.MaterialCostDtos.MaterialCostRequest;
import com.flooring.finance.dto.OtherCostDtos.OtherCostRequest;
import com.flooring.finance.dto.WorkerCostDtos.WorkerCostRequest;
import com.flooring.finance.dto.WorkerFoodCostDtos.WorkerFoodCostRequest;
import com.flooring.finance.entity.Job;
import com.flooring.finance.exception.ResourceNotFoundException;
import com.flooring.finance.mapper.DeliveryCostMapper;
import com.flooring.finance.mapper.JobMapper;
import com.flooring.finance.mapper.MaterialCostMapper;
import com.flooring.finance.mapper.OtherCostMapper;
import com.flooring.finance.mapper.WorkerCostMapper;
import com.flooring.finance.mapper.WorkerFoodCostMapper;
import com.flooring.finance.repository.JobRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the Job aggregate: the job itself plus its five cost line-item
 * collections. Every mutation re-saves the job (cascades to its costs) and
 * every read re-derives totals via {@link JobCalculationService} - nothing
 * financial is ever stored redundantly.
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final JobCalculationService calculationService;

    public JobService(JobRepository jobRepository, JobCalculationService calculationService) {
        this.jobRepository = jobRepository;
        this.calculationService = calculationService;
    }

    /**
     * Backs the Jobs list page. Filtering happens in Java rather than a
     * dynamic JPQL query - this business has a handful of jobs at a time,
     * so a simple in-memory filter is both correct and easy to follow.
     */
    @Transactional(readOnly = true)
    public List<JobSummaryResponse> search(String search, JobStatus status, LocalDate from, LocalDate to) {
        String needle = blankToNull(search);
        List<Job> jobs = (from != null && to != null) ? jobRepository.findByJobDateBetween(from, to) : jobRepository.findAll();

        return jobs.stream()
                .filter(job -> status == null || job.getStatus() == status)
                .filter(job -> needle == null || matches(job, needle))
                .sorted(Comparator.comparing(Job::getJobDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Job::getId, Comparator.reverseOrder()))
                .map(job -> JobMapper.toSummaryResponse(job, calculationService.calculateTotals(job)))
                .toList();
    }

    private boolean matches(Job job, String needle) {
        String lower = needle.toLowerCase();
        return containsIgnoreCase(job.getName(), lower)
                || containsIgnoreCase(job.getCustomerName(), lower)
                || containsIgnoreCase(job.getLocation(), lower);
    }

    private boolean containsIgnoreCase(String value, String lowerNeedle) {
        return value != null && value.toLowerCase().contains(lowerNeedle);
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        Job job = getOrThrow(id);
        return JobMapper.toResponse(job, calculationService.calculateTotals(job));
    }

    public JobResponse create(JobRequest request) {
        Job saved = jobRepository.save(JobMapper.toEntity(request));
        return JobMapper.toResponse(saved, calculationService.calculateTotals(saved));
    }

    public JobResponse update(Long id, JobRequest request) {
        Job job = getOrThrow(id);
        JobMapper.updateEntity(job, request);
        Job saved = jobRepository.save(job);
        return JobMapper.toResponse(saved, calculationService.calculateTotals(saved));
    }

    public void delete(Long id) {
        jobRepository.delete(getOrThrow(id));
    }

    // --- Cost line items: add appends to the job's collection, remove filters it out. ---
    // Cascade + orphanRemoval on Job's collections do the persistence work.

    public JobResponse addMaterialCost(Long jobId, MaterialCostRequest request) {
        Job job = getOrThrow(jobId);
        job.getMaterialCosts().add(MaterialCostMapper.toEntity(request, job));
        return saveAndRespond(job);
    }

    public JobResponse removeMaterialCost(Long jobId, Long costId) {
        Job job = getOrThrow(jobId);
        job.getMaterialCosts().removeIf(c -> c.getId().equals(costId));
        return saveAndRespond(job);
    }

    public JobResponse addDeliveryCost(Long jobId, DeliveryCostRequest request) {
        Job job = getOrThrow(jobId);
        job.getDeliveryCosts().add(DeliveryCostMapper.toEntity(request, job));
        return saveAndRespond(job);
    }

    public JobResponse removeDeliveryCost(Long jobId, Long costId) {
        Job job = getOrThrow(jobId);
        job.getDeliveryCosts().removeIf(c -> c.getId().equals(costId));
        return saveAndRespond(job);
    }

    public JobResponse addOtherCost(Long jobId, OtherCostRequest request) {
        Job job = getOrThrow(jobId);
        job.getOtherCosts().add(OtherCostMapper.toEntity(request, job));
        return saveAndRespond(job);
    }

    public JobResponse removeOtherCost(Long jobId, Long costId) {
        Job job = getOrThrow(jobId);
        job.getOtherCosts().removeIf(c -> c.getId().equals(costId));
        return saveAndRespond(job);
    }

    public JobResponse addWorkerCost(Long jobId, WorkerCostRequest request) {
        Job job = getOrThrow(jobId);
        job.getWorkerCosts().add(WorkerCostMapper.toEntity(request, job));
        return saveAndRespond(job);
    }

    public JobResponse removeWorkerCost(Long jobId, Long costId) {
        Job job = getOrThrow(jobId);
        job.getWorkerCosts().removeIf(c -> c.getId().equals(costId));
        return saveAndRespond(job);
    }

    public JobResponse addWorkerFoodCost(Long jobId, WorkerFoodCostRequest request) {
        Job job = getOrThrow(jobId);
        job.getWorkerFoodCosts().add(WorkerFoodCostMapper.toEntity(request, job));
        return saveAndRespond(job);
    }

    public JobResponse removeWorkerFoodCost(Long jobId, Long costId) {
        Job job = getOrThrow(jobId);
        job.getWorkerFoodCosts().removeIf(c -> c.getId().equals(costId));
        return saveAndRespond(job);
    }

    private JobResponse saveAndRespond(Job job) {
        Job saved = jobRepository.save(job);
        return JobMapper.toResponse(saved, calculationService.calculateTotals(saved));
    }

    Job getOrThrow(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

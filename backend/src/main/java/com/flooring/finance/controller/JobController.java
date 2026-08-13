package com.flooring.finance.controller;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.dto.DeliveryCostDtos.DeliveryCostRequest;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.dto.MaterialCostDtos.MaterialCostRequest;
import com.flooring.finance.dto.OtherCostDtos.OtherCostRequest;
import com.flooring.finance.dto.WorkerCostDtos.WorkerCostRequest;
import com.flooring.finance.dto.WorkerFoodCostDtos.WorkerFoodCostRequest;
import com.flooring.finance.service.JobService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Jobs and every one of their cost line items. Deliberately one controller
 * for the whole aggregate, matching JobService - there's only one real
 * entity of consequence here.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobSummaryResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return jobService.search(search, status, from, to);
    }

    @GetMapping("/{id}")
    public JobResponse findById(@PathVariable Long id) {
        return jobService.findById(id);
    }

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
    }

    @PutMapping("/{id}")
    public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Materials ---

    @PostMapping("/{id}/materials")
    public JobResponse addMaterial(@PathVariable Long id, @Valid @RequestBody MaterialCostRequest request) {
        return jobService.addMaterialCost(id, request);
    }

    @DeleteMapping("/{id}/materials/{costId}")
    public JobResponse removeMaterial(@PathVariable Long id, @PathVariable Long costId) {
        return jobService.removeMaterialCost(id, costId);
    }

    // --- Delivery ---

    @PostMapping("/{id}/delivery")
    public JobResponse addDelivery(@PathVariable Long id, @Valid @RequestBody DeliveryCostRequest request) {
        return jobService.addDeliveryCost(id, request);
    }

    @DeleteMapping("/{id}/delivery/{costId}")
    public JobResponse removeDelivery(@PathVariable Long id, @PathVariable Long costId) {
        return jobService.removeDeliveryCost(id, costId);
    }

    // --- Other costs ---

    @PostMapping("/{id}/other-costs")
    public JobResponse addOtherCost(@PathVariable Long id, @Valid @RequestBody OtherCostRequest request) {
        return jobService.addOtherCost(id, request);
    }

    @DeleteMapping("/{id}/other-costs/{costId}")
    public JobResponse removeOtherCost(@PathVariable Long id, @PathVariable Long costId) {
        return jobService.removeOtherCost(id, costId);
    }

    // --- Worker salary ---

    @PostMapping("/{id}/worker-costs")
    public JobResponse addWorkerCost(@PathVariable Long id, @Valid @RequestBody WorkerCostRequest request) {
        return jobService.addWorkerCost(id, request);
    }

    @DeleteMapping("/{id}/worker-costs/{costId}")
    public JobResponse removeWorkerCost(@PathVariable Long id, @PathVariable Long costId) {
        return jobService.removeWorkerCost(id, costId);
    }

    // --- Worker food ---

    @PostMapping("/{id}/worker-food")
    public JobResponse addWorkerFood(@PathVariable Long id, @Valid @RequestBody WorkerFoodCostRequest request) {
        return jobService.addWorkerFoodCost(id, request);
    }

    @DeleteMapping("/{id}/worker-food/{costId}")
    public JobResponse removeWorkerFood(@PathVariable Long id, @PathVariable Long costId) {
        return jobService.removeWorkerFoodCost(id, costId);
    }
}

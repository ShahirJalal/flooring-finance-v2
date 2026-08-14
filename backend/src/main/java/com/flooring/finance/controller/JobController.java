package com.flooring.finance.controller;

import com.flooring.finance.dto.EntrySuggestion;
import com.flooring.finance.dto.JobDtos.JobRequest;
import com.flooring.finance.dto.JobDtos.JobResponse;
import com.flooring.finance.dto.JobDtos.JobSummaryResponse;
import com.flooring.finance.service.JobService;
import jakarta.validation.Valid;
import java.util.List;
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

/** Jobs - the one entity of consequence in this system. */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobSummaryResponse> list(@RequestParam(required = false) String search) {
        return jobService.list(search);
    }

    @GetMapping("/entry-suggestions")
    public List<EntrySuggestion> entrySuggestions() {
        return jobService.entrySuggestions();
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
}

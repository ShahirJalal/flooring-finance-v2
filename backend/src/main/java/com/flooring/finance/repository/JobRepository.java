package com.flooring.finance.repository;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.entity.Job;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByJobDateBetween(LocalDate from, LocalDate to);

    List<Job> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(JobStatus status);
}

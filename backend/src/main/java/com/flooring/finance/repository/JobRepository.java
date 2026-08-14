package com.flooring.finance.repository;

import com.flooring.finance.entity.Job;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {

    /** Fetch-joins entries so the Jobs list doesn't run one extra query per job just to total it up. */
    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN FETCH j.entries")
    List<Job> findAllWithEntries();
}

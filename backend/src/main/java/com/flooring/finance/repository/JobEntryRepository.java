package com.flooring.finance.repository;

import com.flooring.finance.dto.EntrySuggestion;
import com.flooring.finance.entity.JobEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobEntryRepository extends JpaRepository<JobEntry, Long> {

    /**
     * Every distinct description+category the owner has typed before, most
     * recently used first - backs the autocomplete in the entry form so he
     * can tap a past entry instead of retyping it.
     */
    @Query("SELECT new com.flooring.finance.dto.EntrySuggestion(e.description, e.category) "
            + "FROM JobEntry e WHERE e.description IS NOT NULL "
            + "GROUP BY e.description, e.category "
            + "ORDER BY MAX(e.createdAt) DESC")
    List<EntrySuggestion> findSuggestions();
}

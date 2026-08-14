package com.flooring.finance.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The central (and only) entity in this system. A job is just a job that
 * happened - a name, when, and a list of entries the owner wrote himself
 * (income and costs alike, each tagged with a category) rather than a fixed
 * set of number fields. No status, no state - neither affects the profit
 * math, and both turned out to be decisions the owner never asked to make.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "job")
public class Job extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(length = 255)
    private String location;

    @Column(name = "job_date")
    private LocalDate jobDate;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Default
    private List<JobEntry> entries = new ArrayList<>();
}

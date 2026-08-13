package com.flooring.finance.entity;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.common.MalaysianState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
 * The central (and only major) entity in this system. Everything financial
 * hangs off a job: a single "Collection" amount plus five cost buckets.
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

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MalaysianState state;

    @Column(name = "job_date")
    private LocalDate jobDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Default
    private JobStatus status = JobStatus.IN_PROGRESS;

    @Column(columnDefinition = "text")
    private String notes;

    /**
     * How much was collected/charged for this job. A single figure for now -
     * kept as its own column (rather than a computed sum) so a future
     * payment/deposit list can be added without changing this contract.
     */
    @Column(name = "collection_amount", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal collectionAmount = BigDecimal.ZERO;

    @Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialCost> materialCosts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryCost> deliveryCosts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OtherCost> otherCosts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkerCost> workerCosts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkerFoodCost> workerFoodCosts = new ArrayList<>();
}

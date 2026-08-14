package com.flooring.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The central (and only) entity in this system. A job is just a job that
 * happened - a name, when, a price, and three cost totals (Materials,
 * Worker, Other) the owner fills in himself, rather than itemized
 * purchase-by-purchase records. No status, no state - neither affects the
 * profit math, and both turned out to be decisions the owner never asked to
 * make. Worker cost carries its rate/day and day-count alongside the total
 * purely so the form can redisplay and re-edit that calculation later - the
 * total (workerCost) is always what actually feeds the profit math.
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

    /**
     * How much was collected/charged for this job. A single figure for now -
     * kept as its own column (rather than a computed sum) so a future
     * payment/deposit list can be added without changing this contract.
     */
    @Column(name = "collection_amount", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal collectionAmount = BigDecimal.ZERO;

    @Column(name = "materials_cost", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal materialsCost = BigDecimal.ZERO;

    /** Optional calculator inputs behind workerCost - purely for redisplay/editing, never read for the profit math. */
    @Column(name = "worker_rate_per_day", precision = 12, scale = 2)
    private BigDecimal workerRatePerDay;

    @Column(name = "worker_days")
    private Integer workerDays;

    @Column(name = "worker_cost", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal workerCost = BigDecimal.ZERO;

    @Column(name = "other_costs", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal otherCosts = BigDecimal.ZERO;
}

package com.flooring.finance.service;

import com.flooring.finance.entity.DeliveryCost;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.MaterialCost;
import com.flooring.finance.entity.OtherCost;
import com.flooring.finance.entity.WorkerCost;
import com.flooring.finance.entity.WorkerFoodCost;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * The one place the job's financial numbers are calculated. This is
 * intentionally centralized (per the brief) since the owner may want to
 * change how profit is calculated later - only this class would need to
 * change.
 *
 * <pre>
 *   Total Cost = Materials + Delivery + Other Costs + Worker Salary + Worker Food
 *   Profit     = Collection - Total Cost
 *   Margin     = Profit / Collection * 100   (0 when Collection is 0, to avoid divide-by-zero)
 * </pre>
 */
@Service
public class JobCalculationService {

    public BigDecimal calculateMaterialTotal(Job job) {
        return job.getMaterialCosts().stream().map(MaterialCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateDeliveryTotal(Job job) {
        return job.getDeliveryCosts().stream().map(DeliveryCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateOtherCostTotal(Job job) {
        return job.getOtherCosts().stream().map(OtherCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateWorkerCostTotal(Job job) {
        return job.getWorkerCosts().stream().map(WorkerCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateWorkerFoodTotal(Job job) {
        return job.getWorkerFoodCosts().stream().map(WorkerFoodCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalCost(Job job) {
        return calculateMaterialTotal(job)
                .add(calculateDeliveryTotal(job))
                .add(calculateOtherCostTotal(job))
                .add(calculateWorkerCostTotal(job))
                .add(calculateWorkerFoodTotal(job));
    }

    public BigDecimal calculateProfit(Job job) {
        BigDecimal collection = job.getCollectionAmount() != null ? job.getCollectionAmount() : BigDecimal.ZERO;
        return collection.subtract(calculateTotalCost(job));
    }

    public BigDecimal calculateProfitMargin(Job job) {
        BigDecimal collection = job.getCollectionAmount() != null ? job.getCollectionAmount() : BigDecimal.ZERO;
        if (collection.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return calculateProfit(job)
                .divide(collection, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** All the numbers a job response needs, computed together in one pass. */
    public Totals calculateTotals(Job job) {
        BigDecimal materials = calculateMaterialTotal(job);
        BigDecimal delivery = calculateDeliveryTotal(job);
        BigDecimal other = calculateOtherCostTotal(job);
        BigDecimal workerSalary = calculateWorkerCostTotal(job);
        BigDecimal workerFood = calculateWorkerFoodTotal(job);
        BigDecimal totalCost = materials.add(delivery).add(other).add(workerSalary).add(workerFood);
        BigDecimal collection = job.getCollectionAmount() != null ? job.getCollectionAmount() : BigDecimal.ZERO;
        BigDecimal profit = collection.subtract(totalCost);
        BigDecimal margin = collection.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : profit.divide(collection, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        return new Totals(materials, delivery, other, workerSalary, workerFood, totalCost, profit, margin);
    }

    public record Totals(
            BigDecimal materialsTotal,
            BigDecimal deliveryTotal,
            BigDecimal otherCostsTotal,
            BigDecimal workerSalaryTotal,
            BigDecimal workerFoodTotal,
            BigDecimal totalCost,
            BigDecimal profit,
            BigDecimal profitMarginPercent
    ) {
    }
}

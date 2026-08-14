-- Simplify the job cost model: the owner fills in one total per category
-- instead of itemizing individual purchases. Replaces the five line-item
-- tables with four flat columns directly on job.

ALTER TABLE job
    ADD COLUMN materials_cost      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN worker_rate_per_day NUMERIC(12, 2),
    ADD COLUMN worker_days         INTEGER,
    ADD COLUMN worker_cost         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN other_costs         NUMERIC(12, 2) NOT NULL DEFAULT 0;

DROP TABLE worker_food_cost;
DROP TABLE worker_cost;
DROP TABLE other_cost;
DROP TABLE delivery_cost;
DROP TABLE material_cost;

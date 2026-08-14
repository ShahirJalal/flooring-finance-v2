-- Drop job state and status. Neither affects the profit calculation, and
-- both turned out to be decisions the owner never asked to make - a job is
-- just a job that happened, with a price and some costs.
ALTER TABLE job
    DROP COLUMN state,
    DROP COLUMN status;

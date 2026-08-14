-- Replace the four fixed cost fields with a free-form entry list: the owner
-- can now log whatever income or expense line he wants, in his own words,
-- and just tags each one with a category - rather than being boxed into
-- fixed Materials/Worker/Other/Price fields.

CREATE TABLE job_entry (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    category        VARCHAR(20) NOT NULL,
    description     VARCHAR(255),
    amount          NUMERIC(12, 2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_job_entry_job_id ON job_entry (job_id);

ALTER TABLE job
    DROP COLUMN collection_amount,
    DROP COLUMN materials_cost,
    DROP COLUMN worker_rate_per_day,
    DROP COLUMN worker_days,
    DROP COLUMN worker_cost,
    DROP COLUMN other_costs;

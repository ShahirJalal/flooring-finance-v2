-- Simple Flooring Job Profit Calculator - initial schema
-- Naming: snake_case tables/columns, BaseEntity fields (id/created_at/updated_at) on every table.

CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE job (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    customer_name       VARCHAR(150),
    location            VARCHAR(255),
    state               VARCHAR(30),
    job_date            DATE,
    status              VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    notes               TEXT,
    collection_amount   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_job_status ON job (status);
CREATE INDEX idx_job_date ON job (job_date);

CREATE TABLE material_cost (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_material_cost_job_id ON material_cost (job_id);

CREATE TABLE delivery_cost (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    date            DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_delivery_cost_job_id ON delivery_cost (job_id);

CREATE TABLE other_cost (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    date            DATE,
    category        VARCHAR(50),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_other_cost_job_id ON other_cost (job_id);

CREATE TABLE worker_cost (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    worker_name     VARCHAR(150) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_worker_cost_job_id ON worker_cost (job_id);

CREATE TABLE worker_food_cost (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    date            DATE,
    description     VARCHAR(255),
    amount          NUMERIC(12, 2) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_worker_food_cost_job_id ON worker_food_cost (job_id);

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE jobs (
                      id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                      title            VARCHAR(150) NOT NULL,
                      description      TEXT         NOT NULL,
                      company          VARCHAR(100) NOT NULL,
                      employer_id      UUID         NOT NULL,
                      location         VARCHAR(100),
                      job_type         VARCHAR(20)  NOT NULL DEFAULT 'FULL_TIME'
                          CHECK (job_type IN ('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','FREELANCE')),
                      status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT','PUBLISHED','CLOSED','ARCHIVED')),
                      experience_level VARCHAR(20)  NOT NULL DEFAULT 'MID'
                          CHECK (experience_level IN ('JUNIOR','MID','SENIOR','LEAD','EXECUTIVE')),
                      salary_min       NUMERIC(12,2),
                      salary_max       NUMERIC(12,2),
                      currency         CHAR(3)      DEFAULT 'USD',
                      requirements     TEXT,
                      benefits         TEXT,
                      remote           BOOLEAN      NOT NULL DEFAULT FALSE,
                      expires_at       TIMESTAMP,
                      created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
                      updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
                      CONSTRAINT chk_salary CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min)
);

CREATE INDEX idx_jobs_employer_id ON jobs(employer_id);
CREATE INDEX idx_jobs_status      ON jobs(status);
CREATE INDEX idx_jobs_location    ON jobs(location);
CREATE INDEX idx_jobs_job_type    ON jobs(job_type);
CREATE INDEX idx_jobs_created_at  ON jobs(created_at DESC);
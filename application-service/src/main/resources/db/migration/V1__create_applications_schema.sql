CREATE SCHEMA IF NOT EXISTS applications;

CREATE TABLE applications.applications (
                                           id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                           candidate_id UUID         NOT NULL,
                                           job_id       UUID         NOT NULL,
                                           job_title    VARCHAR(150) NOT NULL,
                                           company_name VARCHAR(100) NOT NULL,
                                           status       VARCHAR(30)  NOT NULL DEFAULT 'SUBMITTED'
                                               CHECK (status IN (
                                                                 'SUBMITTED','UNDER_REVIEW','INTERVIEW_SCHEDULED',
                                                                 'OFFER_EXTENDED','ACCEPTED','REJECTED','WITHDRAWN'
                                                   )),
                                           cover_letter TEXT,
                                           resume_id    UUID,
                                           notes        TEXT,
                                           applied_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                                           updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                                           CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id)
);

CREATE TABLE applications.application_status_history (
                                                         id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                                         application_id UUID        NOT NULL
                                                             REFERENCES applications.applications(id) ON DELETE CASCADE,
                                                         from_status    VARCHAR(30) NOT NULL,
                                                         to_status      VARCHAR(30) NOT NULL,
                                                         reason         VARCHAR(255),
                                                         changed_by     UUID        NOT NULL,
                                                         changed_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_candidate_id ON applications.applications(candidate_id);
CREATE INDEX idx_app_job_id       ON applications.applications(job_id);
CREATE INDEX idx_app_status       ON applications.applications(status);
CREATE INDEX idx_app_applied_at   ON applications.applications(applied_at DESC);
CREATE INDEX idx_hist_app_id      ON applications.application_status_history(application_id);
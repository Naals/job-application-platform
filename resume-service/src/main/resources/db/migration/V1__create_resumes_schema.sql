CREATE SCHEMA IF NOT EXISTS resumes;

CREATE TABLE resumes.resumes (
                                 id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                 candidate_id      UUID         NOT NULL,
                                 original_file_name VARCHAR(255) NOT NULL,
                                 object_key        VARCHAR(512) NOT NULL UNIQUE,
                                 content_type      VARCHAR(100) NOT NULL,
                                 file_size_bytes   BIGINT       NOT NULL
                                     CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
                                 parse_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                     CHECK (parse_status IN ('PENDING','PROCESSING','COMPLETED','FAILED')),
                                 extracted_text    TEXT,
                                 is_active         BOOLEAN      NOT NULL DEFAULT FALSE,
                                 uploaded_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resume_candidate_id ON resumes.resumes(candidate_id);
CREATE INDEX idx_resume_parse_status ON resumes.resumes(parse_status);
CREATE INDEX idx_resume_is_active    ON resumes.resumes(is_active);
CREATE INDEX idx_resume_uploaded_at  ON resumes.resumes(uploaded_at DESC);
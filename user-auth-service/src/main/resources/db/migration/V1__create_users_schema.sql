CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       email       VARCHAR(100) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       first_name  VARCHAR(50)  NOT NULL,
                       last_name   VARCHAR(50)  NOT NULL,
                       phone       VARCHAR(20),
                       status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
                            user_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role    VARCHAR(30) NOT NULL
                                CHECK (role IN ('ROLE_CANDIDATE','ROLE_EMPLOYER','ROLE_ADMIN')),
                            PRIMARY KEY (user_id, role)
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- Seed admin (password: Admin@1234)
INSERT INTO users (id, email, password, first_name, last_name, status)
VALUES (
           gen_random_uuid(),
           'admin@jobplatform.com',
           '$2a$12$g.1XeXRQYp7wv9x9e8dZUOmX9cOtMNOdMlZrFtLcgLZB1A.1E.r1K',
           'Platform', 'Admin', 'ACTIVE'
       );

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE email = 'admin@jobplatform.com';
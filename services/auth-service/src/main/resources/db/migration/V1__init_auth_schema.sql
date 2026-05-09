CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       email               VARCHAR(255) NOT NULL UNIQUE,
                       password_hash       VARCHAR(255) NOT NULL,
                       full_name           VARCHAR(100) NOT NULL,
                       phone               VARCHAR(20),
                       role                VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
                       is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
                       is_email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
                       created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
                       is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
                       created_by          UUID
);

CREATE TABLE vendor_profiles (
                                 id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id             UUID         NOT NULL UNIQUE REFERENCES users(id),
                                 store_name          VARCHAR(100) NOT NULL,
                                 store_description   TEXT,
                                 store_logo_url      VARCHAR(500),
                                 approval_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                                 approved_by         UUID         REFERENCES users(id),
                                 approved_at         TIMESTAMP,
                                 created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
                                 created_by          UUID
);

CREATE TABLE refresh_tokens (
                                id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID         NOT NULL REFERENCES users(id),
                                token       VARCHAR(500) NOT NULL UNIQUE,
                                expires_at  TIMESTAMP    NOT NULL,
                                is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_by  UUID
);

CREATE INDEX idx_users_email           ON users(email);
CREATE INDEX idx_users_role            ON users(role);
CREATE INDEX idx_users_is_deleted      ON users(is_deleted);
CREATE INDEX idx_refresh_tokens_user   ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token  ON refresh_tokens(token);
CREATE INDEX idx_vendor_status         ON vendor_profiles(approval_status);

INSERT INTO users (
    id, email, password_hash, full_name, role,
    is_active, is_email_verified, created_by
) VALUES (
             gen_random_uuid(),
             'admin@liftkart.com',
             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsxq/VTK6',
             'LiftKart Admin',
             'ADMIN',
             TRUE, TRUE, NULL
         );
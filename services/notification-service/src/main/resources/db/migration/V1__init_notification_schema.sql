CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE notifications (
                               id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id         UUID         NOT NULL,
                               title           VARCHAR(255) NOT NULL,
                               message         TEXT         NOT NULL,
                               type            VARCHAR(50)  NOT NULL,
                               reference_id    UUID,
                               reference_type  VARCHAR(50),
                               is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
                               read_at         TIMESTAMP,
                               created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                               updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                               is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
                               created_by      UUID
);

CREATE INDEX idx_notifications_user    ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_type    ON notifications(type);
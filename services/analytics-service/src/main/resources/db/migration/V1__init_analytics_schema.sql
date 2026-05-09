CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE daily_sales_summary (
                                     id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                     vendor_id       UUID,
                                     summary_date    DATE          NOT NULL,
                                     total_orders    INTEGER       NOT NULL DEFAULT 0,
                                     total_revenue   DECIMAL(15,2) NOT NULL DEFAULT 0,
                                     total_items_sold INTEGER      NOT NULL DEFAULT 0,
                                     created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                     is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
                                     created_by      UUID,
                                     UNIQUE (vendor_id, summary_date)
);

CREATE TABLE product_view_events (
                                     id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                                     product_id  UUID      NOT NULL,
                                     customer_id UUID,
                                     viewed_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                                     created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                     updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                     is_deleted  BOOLEAN   NOT NULL DEFAULT FALSE,
                                     created_by  UUID
);

CREATE TABLE order_event_log (
                                 id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                                 order_id        UUID      NOT NULL,
                                 event_type      VARCHAR(50) NOT NULL,
                                 event_payload   JSONB       NOT NULL,
                                 occurred_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
                                 created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                 updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                 is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
                                 created_by      UUID
);

CREATE INDEX idx_daily_summary_vendor  ON daily_sales_summary(vendor_id);
CREATE INDEX idx_daily_summary_date    ON daily_sales_summary(summary_date);
CREATE INDEX idx_product_views_product ON product_view_events(product_id);
CREATE INDEX idx_order_log_order       ON order_event_log(order_id);
CREATE INDEX idx_order_log_type        ON order_event_log(event_type);
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE carts (
                       id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                       customer_id     UUID      NOT NULL UNIQUE,
                       last_activity_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       is_deleted      BOOLEAN   NOT NULL DEFAULT FALSE,
                       created_by      UUID
);

CREATE TABLE cart_items (
                            id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                            cart_id         UUID          NOT NULL REFERENCES carts(id),
                            product_id      UUID          NOT NULL,
                            product_name    VARCHAR(255)  NOT NULL,
                            image_url       VARCHAR(500),
                            quantity        INTEGER       NOT NULL DEFAULT 1 CHECK (quantity > 0),
                            price_snapshot  DECIMAL(15,2) NOT NULL,
                            saved_for_later BOOLEAN       NOT NULL DEFAULT FALSE,
                            created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                            updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                            is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
                            created_by      UUID,
                            UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_carts_customer     ON carts(customer_id);
CREATE INDEX idx_cart_items_cart    ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
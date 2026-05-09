CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE addresses (
                           id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                           customer_id UUID         NOT NULL,
                           full_name   VARCHAR(100) NOT NULL,
                           phone       VARCHAR(20)  NOT NULL,
                           line1       VARCHAR(255) NOT NULL,
                           line2       VARCHAR(255),
                           city        VARCHAR(100) NOT NULL,
                           state       VARCHAR(100) NOT NULL,
                           pincode     VARCHAR(10)  NOT NULL,
                           is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
                           created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                           updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                           is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
                           created_by  UUID
);

CREATE TABLE orders (
                        id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                        customer_id      UUID          NOT NULL,
                        address_snapshot JSONB         NOT NULL,
                        status           VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
                        payment_method   VARCHAR(30)   NOT NULL,
                        payment_status   VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
                        subtotal         DECIMAL(15,2) NOT NULL,
                        delivery_charge  DECIMAL(15,2) NOT NULL DEFAULT 0,
                        discount_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
                        total_amount     DECIMAL(15,2) NOT NULL,
                        notes            TEXT,
                        created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
                        updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
                        is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE,
                        created_by       UUID
);

CREATE TABLE order_items (
                             id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id          UUID          NOT NULL REFERENCES orders(id),
                             product_id        UUID          NOT NULL,
                             vendor_id         UUID          NOT NULL,
                             product_name      VARCHAR(255)  NOT NULL,
                             product_image_url VARCHAR(500),
                             quantity          INTEGER       NOT NULL,
                             unit_price        DECIMAL(15,2) NOT NULL,
                             total_price       DECIMAL(15,2) NOT NULL,
                             status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
                             created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
                             updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
                             is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
                             created_by        UUID
);

CREATE TABLE order_status_history (
                                      id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                                      order_id        UUID      NOT NULL REFERENCES orders(id),
                                      old_status      VARCHAR(30),
                                      new_status      VARCHAR(30) NOT NULL,
                                      changed_by      UUID        NOT NULL,
                                      changed_by_role VARCHAR(20) NOT NULL,
                                      comment         TEXT,
                                      created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                      updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                      is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
                                      created_by      UUID
);

CREATE TABLE payments (
                          id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          order_id          UUID          NOT NULL UNIQUE REFERENCES orders(id),
                          method            VARCHAR(30)   NOT NULL,
                          status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
                          amount            DECIMAL(15,2) NOT NULL,
                          simulated_ref_id  VARCHAR(100),
                          processed_at      TIMESTAMP,
                          created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
                          updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
                          is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
                          created_by        UUID
);

CREATE INDEX idx_orders_customer    ON orders(customer_id);
CREATE INDEX idx_orders_status      ON orders(status);
CREATE INDEX idx_order_items_order  ON order_items(order_id);
CREATE INDEX idx_order_items_vendor ON order_items(vendor_id);
CREATE INDEX idx_payments_order     ON payments(order_id);
CREATE INDEX idx_addresses_customer ON addresses(customer_id);
CREATE INDEX idx_status_history_order ON order_status_history(order_id);
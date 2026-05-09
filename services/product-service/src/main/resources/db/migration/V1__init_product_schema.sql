CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Categories (self-referencing hierarchy)
CREATE TABLE categories (
                            id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            name            VARCHAR(100) NOT NULL,
                            slug            VARCHAR(100) NOT NULL UNIQUE,
                            parent_id       UUID         REFERENCES categories(id),
                            icon_url        VARCHAR(500),
                            display_order   INTEGER      NOT NULL DEFAULT 0,
                            is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
                            created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                            updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                            is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
                            created_by      UUID
);

-- Products
CREATE TABLE products (
                          id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          vendor_id           UUID          NOT NULL,
                          category_id         UUID          NOT NULL REFERENCES categories(id),
                          name                VARCHAR(255)  NOT NULL,
                          slug                VARCHAR(255)  NOT NULL UNIQUE,
                          description         TEXT,
                          price               DECIMAL(15,2) NOT NULL,
                          compare_price       DECIMAL(15,2),
                          stock_quantity      INTEGER       NOT NULL DEFAULT 0,
                          low_stock_threshold INTEGER       NOT NULL DEFAULT 10,
                          sku                 VARCHAR(100)  UNIQUE,
                          status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
                          average_rating      DECIMAL(3,2)  NOT NULL DEFAULT 0,
                          review_count        INTEGER       NOT NULL DEFAULT 0,
                          created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
                          updated_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
                          is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
                          created_by          UUID
);

-- Product Images
CREATE TABLE product_images (
                                id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                product_id      UUID         NOT NULL REFERENCES products(id),
                                image_url       VARCHAR(500) NOT NULL,
                                alt_text        VARCHAR(255),
                                display_order   INTEGER      NOT NULL DEFAULT 0,
                                is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                                updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                                is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_by      UUID
);

-- Reviews
CREATE TABLE reviews (
                         id                      UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
                         product_id              UUID    NOT NULL REFERENCES products(id),
                         customer_id             UUID    NOT NULL,
                         rating                  INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         title                   VARCHAR(255),
                         comment                 TEXT,
                         is_verified_purchase    BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
                         is_deleted              BOOLEAN   NOT NULL DEFAULT FALSE,
                         created_by              UUID,
                         UNIQUE (product_id, customer_id)
);

-- Inventory Logs
CREATE TABLE inventory_logs (
                                id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                product_id      UUID          NOT NULL REFERENCES products(id),
                                change_quantity INTEGER       NOT NULL,
                                reason          VARCHAR(50)   NOT NULL,
                                reference_id    UUID,
                                stock_after     INTEGER       NOT NULL,
                                created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
                                created_by      UUID
);

-- Indexes
CREATE INDEX idx_products_vendor        ON products(vendor_id);
CREATE INDEX idx_products_category      ON products(category_id);
CREATE INDEX idx_products_status        ON products(status);
CREATE INDEX idx_products_is_deleted    ON products(is_deleted);
CREATE INDEX idx_products_slug          ON products(slug);
CREATE INDEX idx_categories_parent      ON categories(parent_id);
CREATE INDEX idx_categories_slug        ON categories(slug);
CREATE INDEX idx_reviews_product        ON reviews(product_id);
CREATE INDEX idx_reviews_customer       ON reviews(customer_id);
CREATE INDEX idx_inventory_product      ON inventory_logs(product_id);
CREATE INDEX idx_product_images_product ON product_images(product_id);

-- Seed default categories
INSERT INTO categories (id, name, slug, parent_id, display_order) VALUES
                                                                      (gen_random_uuid(), 'Electronics',      'electronics',       NULL, 1),
                                                                      (gen_random_uuid(), 'Fashion',          'fashion',           NULL, 2),
                                                                      (gen_random_uuid(), 'Home & Kitchen',   'home-kitchen',      NULL, 3),
                                                                      (gen_random_uuid(), 'Sports',           'sports',            NULL, 4),
                                                                      (gen_random_uuid(), 'Books',            'books',             NULL, 5);

-- Sub-categories for Electronics
INSERT INTO categories (id, name, slug, parent_id, display_order)
SELECT gen_random_uuid(), 'Mobile Phones', 'mobile-phones',
       id, 1 FROM categories WHERE slug = 'electronics';

INSERT INTO categories (id, name, slug, parent_id, display_order)
SELECT gen_random_uuid(), 'Laptops', 'laptops',
       id, 2 FROM categories WHERE slug = 'electronics';

INSERT INTO categories (id, name, slug, parent_id, display_order)
SELECT gen_random_uuid(), 'Audio', 'audio',
       id, 3 FROM categories WHERE slug = 'electronics';
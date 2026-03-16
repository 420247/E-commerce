-- Products table — items available for purchase in the store.
-- price: DECIMAL(10,2) — up to 99999999.99, avoids float rounding errors.
-- rating: DECIMAL(3,2) — values from 0.00 to 9.99, typically 0.0–5.0.
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,                              -- TEXT: unlimited length, unlike VARCHAR
    price       DECIMAL(10,2) NOT NULL,            -- e.g. 1299.99
    category    VARCHAR(100),                      -- e.g. 'electronics', 'books', 'clothing'
    rating      DECIMAL(3,2) DEFAULT 0.0,          -- average customer rating
    image_url   VARCHAR(500),                      -- URL to product image
    stock       INTEGER DEFAULT 0,                 -- available inventory count
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

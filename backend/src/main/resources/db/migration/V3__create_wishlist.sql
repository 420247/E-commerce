-- Wishlist items — links users to their saved products.
-- REFERENCES with ON DELETE CASCADE: if user or product is deleted,
--   their wishlist items are automatically deleted too.
-- UNIQUE(user_id, product_id): prevents adding the same product twice.
CREATE TABLE wishlist_items (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)                    -- one product per user in wishlist
);

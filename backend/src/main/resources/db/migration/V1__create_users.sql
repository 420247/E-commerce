-- Users table — stores registered accounts.
-- password: stored as BCrypt hash, never plain text.
-- email: UNIQUE constraint prevents duplicate accounts.
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,              -- auto-increment PK
    email       VARCHAR(255) NOT NULL UNIQUE,       -- login identifier, must be unique
    password    VARCHAR(255) NOT NULL,              -- BCrypt hash e.g. $2a$10$...
    name        VARCHAR(255) NOT NULL,              -- display name
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- set automatically on INSERT
);

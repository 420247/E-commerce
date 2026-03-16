-- Adds role column to existing users table.
-- VARCHAR instead of ENUM — easier to extend with new roles later.
-- DEFAULT 'USER' — existing rows automatically get USER role.
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

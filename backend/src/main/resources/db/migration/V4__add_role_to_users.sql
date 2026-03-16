-- Add role column to users table
-- DEFAULT 'USER' ensures existing rows get a value automatically
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
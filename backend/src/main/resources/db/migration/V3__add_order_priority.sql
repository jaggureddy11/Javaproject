-- V3: Add order priority column
ALTER TABLE orders ADD COLUMN IF NOT EXISTS priority INT DEFAULT 1 CHECK (priority >= 1 AND priority <= 5);

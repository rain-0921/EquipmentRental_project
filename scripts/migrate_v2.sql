-- =====================================================================
-- migrate_v2.sql : add is_final_year to user table (idempotent)
-- =====================================================================
-- Run against an existing smart_rental database created by an earlier
-- version of setup_database.sql. Safe to re-run.

USE smart_rental;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'user'
      AND COLUMN_NAME  = 'is_final_year'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE user ADD COLUMN is_final_year BOOLEAN NOT NULL DEFAULT FALSE AFTER discount_rate',
    'SELECT 1');
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

-- Bring existing seed users in line with the v2 discount policy.
-- (U004 Chia Wei is the demo "final year" student.)
UPDATE user SET discount_rate = 0.0000, is_final_year = FALSE WHERE user_id = 'U001';
UPDATE user SET discount_rate = 0.0000, is_final_year = FALSE WHERE user_id = 'U002';
UPDATE user SET discount_rate = 0.2000, is_final_year = FALSE WHERE user_id = 'U003';
UPDATE user SET discount_rate = 0.1500, is_final_year = TRUE  WHERE user_id = 'U004';

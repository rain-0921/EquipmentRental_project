-- =====================================================================
-- migrate_v3.sql : introduce damage severity tiers
-- =====================================================================
-- Adds rental.damage_level (None / Light / Moderate / Heavy) as the
-- new authoritative column. The legacy damaged BOOLEAN is kept (now
-- unused) for backwards compatibility - the Java code only reads
-- damage_level from this point on.
--
-- Run against an existing smart_rental database. Safe to re-run.
-- =====================================================================

USE smart_rental;

-- 1. Add damage_level if it doesn't exist yet.
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'rental'
      AND COLUMN_NAME  = 'damage_level'
);
SET @stmt := IF(@col_exists = 0,
    "ALTER TABLE rental ADD COLUMN damage_level VARCHAR(16) NOT NULL DEFAULT 'NONE' AFTER damaged",
    'SELECT 1');
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

-- 2. Backfill from the legacy BOOLEAN (only where damage_level is
--    still the default 'NONE' AND damaged was true, to avoid
--    overwriting anything that was set after this migration).
UPDATE rental
   SET damage_level = 'MODERATE'
 WHERE damage_level = 'NONE' AND damaged = TRUE;

SELECT 'v3 migration complete (damage_level)' AS info;
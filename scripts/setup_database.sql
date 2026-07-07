-- =====================================================================
-- Smart Equipment Rental & Billing System - MySQL Schema
-- =====================================================================
-- Run this once against your MySQL server to create the database and
-- seed reference data. The application will auto-create tables on first
-- run, but you still need this script to create the database and the
-- dedicated MySQL user.
--
-- Easiest path on Windows: install XAMPP, start MySQL from the XAMPP
-- control panel, then run:
--   "C:\xampp\mysql\bin\mysql.exe" -u root < scripts\setup_database.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smart_rental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_rental;

-- Drop tables in dependency order so re-running this script is safe.
DROP TABLE IF EXISTS bill;
DROP TABLE IF EXISTS rental;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS user;

-- ---------------------------------------------------------------------
-- user : students / staff who rent equipment
-- ---------------------------------------------------------------------
CREATE TABLE user (
    user_id        VARCHAR(20)  PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    user_type      ENUM('STUDENT','STAFF') NOT NULL,
    discount_rate  DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    is_final_year  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Idempotent migration for existing databases (added in v2).
-- Safe to re-run; MySQL throws a duplicate-column warning we ignore.
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

-- ---------------------------------------------------------------------
-- equipment : items available for rental
-- category drives the Bridge's "refined abstraction" selection,
-- pricing_strategy drives the Bridge's "implementor" selection.
-- ---------------------------------------------------------------------
CREATE TABLE equipment (
    equipment_id      VARCHAR(20)  PRIMARY KEY,
    name              VARCHAR(120) NOT NULL,
    category          ENUM('ELECTRONICS','MEDIA','LABORATORY') NOT NULL,
    daily_rate        DECIMAL(10,2) NOT NULL,
    pricing_strategy  ENUM('STANDARD','TIERED','PROMOTIONAL') NOT NULL,
    available         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- rental : an active or completed rental transaction
-- ---------------------------------------------------------------------
CREATE TABLE rental (
    rental_id     INT          AUTO_INCREMENT PRIMARY KEY,
    user_id       VARCHAR(20)  NOT NULL,
    equipment_id  VARCHAR(20)  NOT NULL,
    rent_date     DATE         NOT NULL,
    due_date      DATE         NOT NULL,
    return_date   DATE         NULL,
    damaged       BOOLEAN      NOT NULL DEFAULT FALSE,  -- legacy; see damage_level
    damage_level  VARCHAR(16)  NOT NULL DEFAULT 'NONE',  -- v3: NONE|LIGHT|MODERATE|HEAVY
    status        ENUM('ACTIVE','RETURNED','OVERDUE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_rental_user
        FOREIGN KEY (user_id) REFERENCES user(user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_rental_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- bill : detailed breakdown for a returned rental
-- ---------------------------------------------------------------------
CREATE TABLE bill (
    bill_id            INT          AUTO_INCREMENT PRIMARY KEY,
    rental_id          INT          NOT NULL UNIQUE,
    base_rental_fee    DECIMAL(10,2) NOT NULL,
    discount_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    penalty_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_payable        DECIMAL(10,2) NOT NULL,
    issued_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_rental
        FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Seed data : a small starter catalogue + a few users
-- ---------------------------------------------------------------------
-- Discount policy (v2):
--   * STUDENT, non-final-year  -> 0% (no discount)
--   * STUDENT, final year      -> 15%
--   * STAFF                    -> 20%
INSERT INTO user (user_id, full_name, email, user_type, discount_rate, is_final_year) VALUES
    ('U001', 'Alice Tan',    'alice.tan@mmu.edu.my',    'STUDENT', 0.0000, FALSE),
    ('U002', 'Bob Lee',      'bob.lee@mmu.edu.my',      'STUDENT', 0.0000, FALSE),
    ('U003', 'Dr. Lim',      'dr.lim@mmu.edu.my',       'STAFF',   0.2000, FALSE),
    ('U004', 'Chia Wei',     'chia.wei@mmu.edu.my',     'STUDENT', 0.1500, TRUE);

INSERT INTO equipment (equipment_id, name, category, daily_rate, pricing_strategy, available) VALUES
    ('E001', 'Dell XPS 13 Laptop',       'ELECTRONICS', 25.00, 'STANDARD',     TRUE),
    ('E002', 'ThinkPad T14',             'ELECTRONICS', 30.00, 'TIERED',       TRUE),
    ('E003', 'MacBook Air M2',           'ELECTRONICS', 45.00, 'PROMOTIONAL',  TRUE),
    ('E004', 'Canon EOS R50 Camera',     'MEDIA',       35.00, 'STANDARD',     TRUE),
    ('E005', 'Sony A6400',               'MEDIA',       40.00, 'TIERED',       TRUE),
    ('E006', 'Epson EB-X51 Projector',   'MEDIA',       20.00, 'STANDARD',     TRUE),
    ('E007', 'Digital Oscilloscope',     'LABORATORY',  50.00, 'PROMOTIONAL',  TRUE),
    ('E008', 'Microscope Set',           'LABORATORY',  18.00, 'STANDARD',     TRUE),
    ('E009', '3D Printer (Creality)',    'LABORATORY',  60.00, 'TIERED',       TRUE);
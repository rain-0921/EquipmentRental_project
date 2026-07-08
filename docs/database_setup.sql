-- Campus Equipment Rental System Database
-- Run this script in phpMyAdmin or MySQL CLI

CREATE DATABASE IF NOT EXISTS campus_rental;
USE campus_rental;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'FINAL_YEAR_STUDENT', 'STAFF') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category ENUM('ELECTRONICS', 'MEDIA', 'LABORATORY') NOT NULL,
    status ENUM('AVAILABLE', 'RENTED', 'DAMAGED') NOT NULL DEFAULT 'AVAILABLE',
    daily_rate DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rentals table
CREATE TABLE IF NOT EXISTS rentals (
    rental_id VARCHAR(30) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    equipment_id VARCHAR(20) NOT NULL,
    rental_days INT NOT NULL,
    rental_date DATE NOT NULL,
    due_date DATE NOT NULL,
    actual_return_date DATE,
    status ENUM('ACTIVE', 'RETURN_REQUESTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'ACTIVE',
    reported_severity ENUM('NONE', 'LIGHT', 'MODERATE', 'HEAVY'),
    final_severity ENUM('NONE', 'LIGHT', 'MODERATE', 'HEAVY'),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
);

-- Bills table
CREATE TABLE IF NOT EXISTS bills (
    bill_id VARCHAR(30) PRIMARY KEY,
    rental_id VARCHAR(30) NOT NULL,
    equipment_name VARCHAR(100) NOT NULL,
    renter_name VARCHAR(100) NOT NULL,
    pricing_plan VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) NOT NULL,
    late_penalty DECIMAL(10,2) NOT NULL,
    damage_penalty DECIMAL(10,2) NOT NULL,
    net_payable DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
);

-- Insert demo users
INSERT INTO users (user_id, name, password, role) VALUES
('student001', 'Alice Chen', '123', 'STUDENT'),
('fyp001', 'Bob Tan', '123', 'FINAL_YEAR_STUDENT'),
('staff001', 'Dr. Wong', '123', 'STAFF')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert demo equipment
INSERT INTO equipment (equipment_id, name, description, category, status, daily_rate) VALUES
('E001', 'Laptop Dell XPS 15', 'High-performance laptop for computing tasks', 'ELECTRONICS', 'AVAILABLE', 50.00),
('E002', 'Wireless Mouse', 'Ergonomic wireless mouse', 'ELECTRONICS', 'AVAILABLE', 10.00),
('E003', 'USB-C Hub', 'Multi-port USB-C hub', 'ELECTRONICS', 'AVAILABLE', 15.00),
('M001', 'Projector Epson', 'HD projector for presentations', 'MEDIA', 'AVAILABLE', 40.00),
('M002', 'Digital Camera', 'Canon DSLR camera with lens kit', 'MEDIA', 'AVAILABLE', 60.00),
('M003', 'Tripod Stand', 'Adjustable tripod for cameras', 'MEDIA', 'AVAILABLE', 12.00),
('L001', 'Oscilloscope', 'Digital oscilloscope for electronics', 'LABORATORY', 'AVAILABLE', 80.00),
('L002', '3D Printer', 'Prusa 3D printer', 'LABORATORY', 'AVAILABLE', 100.00),
('L003', 'Soldering Station', 'Temperature-controlled soldering station', 'LABORATORY', 'AVAILABLE', 25.00)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert rental counter (for ID generation)
CREATE TABLE IF NOT EXISTS counters (
    counter_name VARCHAR(50) PRIMARY KEY,
    counter_value INT NOT NULL DEFAULT 0
);

INSERT INTO counters (counter_name, counter_value) VALUES ('rental_counter', 0), ('bill_counter', 0)
ON DUPLICATE KEY UPDATE counter_value = counter_value;

CREATE DATABASE ocean_view_resort;
USE ocean_view_resort;

CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

ALTER TABLE Users 
-- !!! DO NOT REMOVE ROLE !!! ADMIN/STAFF FEATURES WILL CRASH WITHOUT IT.
ADD role VARCHAR(20) DEFAULT 'staff',
ADD email VARCHAR(100);

-- Insert a default admin user (password: admin123)
INSERT INTO Users (username, password, role) VALUES ('admin', 'admin123', 'admin') 
ON DUPLICATE KEY UPDATE role='admin';

CREATE TABLE IF NOT EXISTS Rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_type VARCHAR(50) NOT NULL,
    rate_per_night DECIMAL(10, 2) NOT NULL
);

-- Insert room types
INSERT INTO Rooms (room_type, rate_per_night) VALUES 
	('Single', 100.00),
	('Double', 150.00),
	('Deluxe', 250.00),
	('Suite', 400.00)
ON DUPLICATE KEY UPDATE id=id;

CREATE TABLE IF NOT EXISTS Reservations (
    reservation_number VARCHAR(20) PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL
);

ALTER TABLE Reservations
ADD email VARCHAR(100) NOT NULL;

ALTER TABLE Reservations
ADD payment_status VARCHAR(20) DEFAULT 'Pending';

CREATE TABLE IF NOT EXISTS Payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_number VARCHAR(20) NOT NULL,
    card_holder VARCHAR(100) NOT NULL,
    card_last_four VARCHAR(4) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_number) REFERENCES Reservations(reservation_number)
);

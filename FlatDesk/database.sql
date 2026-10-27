CREATE DATABASE IF NOT EXISTS flatdesk_db;
USE flatdesk_db;

CREATE TABLE IF NOT EXISTS users (
 id INT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(100) NOT NULL,
 email VARCHAR(150) UNIQUE NOT NULL,
 password VARCHAR(255) NOT NULL COMMENT 'SHA-256 hash for demo project',
 role ENUM('ADMIN','BROKER','USER') NOT NULL DEFAULT 'USER',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS properties (
 id INT PRIMARY KEY AUTO_INCREMENT,
 title VARCHAR(180) NOT NULL,
 country VARCHAR(80) DEFAULT 'India',
 city VARCHAR(100),
 address VARCHAR(255),
 type VARCHAR(50),
 bhk VARCHAR(30),
 rent DECIMAL(12,2) DEFAULT 0,
 area DECIMAL(12,2) DEFAULT 0,
 price DECIMAL(15,2) DEFAULT 0,
 owner VARCHAR(120),
 notes TEXT,
 status ENUM('Available','Sold','Rented') DEFAULT 'Available',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inquiries (
 id INT PRIMARY KEY AUTO_INCREMENT,
 property_id INT NOT NULL,
 user_id INT NOT NULL,
 message TEXT,
 status ENUM('NEW','CONTACTED','CLOSED') DEFAULT 'NEW',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE CASCADE,
 FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS favorites (
 id INT PRIMARY KEY AUTO_INCREMENT,
 property_id INT NOT NULL,
 user_id INT NOT NULL,
 UNIQUE(property_id,user_id),
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE CASCADE,
 FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Demo passwords: admin123 / broker123 / user123
INSERT INTO users(name,email,password,role) VALUES
('System Admin','admin@flatdesk.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('Demo Broker','broker@flatdesk.com', 'ce16be2079a6dbb36eb9ff6b0d7d9f6c4c76a3064f31805142e13b05a44bb434', 'BROKER'),
('Demo User','user@flatdesk.com', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER');

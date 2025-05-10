-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS siemens_internship;
USE siemens_internship;

-- Create the item table
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO item (name, description, status, email) VALUES
('Laptop Dell XPS', 'High-performance laptop for development', 'PENDING', 'john.doe@example.com'),
('Monitor LG 27"', '4K Ultra HD Monitor', 'PENDING', 'jane.smith@example.com'),
('Keyboard Mechanical', 'RGB Mechanical Gaming Keyboard', 'PENDING', 'alex.wilson@example.com'),
('Mouse Gaming', 'Wireless Gaming Mouse', 'PENDING', 'sarah.johnson@example.com'),
('Headphones Sony', 'Noise Cancelling Headphones', 'PENDING', 'mike.brown@example.com'); 
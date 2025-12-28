-- GreenGrocer Database Schema
-- Run this in MySQL before starting the application

CREATE DATABASE IF NOT EXISTS greengrocer;
USE greengrocer;

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('customer', 'carrier', 'owner') NOT NULL DEFAULT 'customer',
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    loyalty_points INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('vegetable', 'fruit') NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock DECIMAL(10, 2) NOT NULL DEFAULT 0,
    threshold DECIMAL(10, 2) NOT NULL DEFAULT 5.0,
    image LONGBLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    carrier_id INT,
    order_time DATETIME NOT NULL,
    delivery_time DATETIME,
    actual_delivery_time DATETIME,
    status ENUM('pending', 'available', 'in_progress', 'delivered', 'cancelled') DEFAULT 'available',
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount DECIMAL(10, 2) DEFAULT 0,
    vat_amount DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0,
    invoice LONGBLOB,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (carrier_id) REFERENCES users(id)
);

-- Order items table
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Messages table
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    reply_to_id INT,
    sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    FOREIGN KEY (reply_to_id) REFERENCES messages(id)
);

-- Coupons table
CREATE TABLE coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_value DECIMAL(10, 2) NOT NULL,
    is_percentage BOOLEAN DEFAULT FALSE,
    expiry_date DATE,
    assigned_user_id INT,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id)
);

-- Carrier ratings table
CREATE TABLE carrier_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    carrier_id INT NOT NULL,
    customer_id INT NOT NULL,
    order_id INT NOT NULL UNIQUE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (carrier_id) REFERENCES users(id),
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Insert sample data

-- Owner account (password: admin)
INSERT INTO users (username, password, role, full_name, email) VALUES
('admin', 'admin', 'owner', 'Store Owner', 'owner@greengrocer.com');

-- Carrier accounts (password: 1234)
INSERT INTO users (username, password, role, full_name, phone) VALUES
('carrier1', '1234', 'carrier', 'John Carrier', '555-0101'),
('carrier2', '1234', 'carrier', 'Jane Delivery', '555-0102');

-- Customer accounts (password: 1234)
INSERT INTO users (username, password, role, full_name, email, address, completed_orders) VALUES
('customer1', '1234', 'customer', 'Alice Customer', 'alice@email.com', '123 Main St', 5),
('customer2', '1234', 'customer', 'Bob Buyer', 'bob@email.com', '456 Oak Ave', 2);

-- Sample products
INSERT INTO products (name, type, price, stock, threshold) VALUES
('Tomato', 'vegetable', 15.00, 50.0, 10.0),
('Cucumber', 'vegetable', 12.00, 40.0, 8.0),
('Carrot', 'vegetable', 10.00, 60.0, 10.0),
('Potato', 'vegetable', 8.00, 100.0, 15.0),
('Onion', 'vegetable', 7.00, 80.0, 12.0),
('Apple', 'fruit', 20.00, 45.0, 10.0),
('Banana', 'fruit', 18.00, 55.0, 10.0),
('Orange', 'fruit', 22.00, 35.0, 8.0),
('Grape', 'fruit', 35.00, 25.0, 5.0),
('Strawberry', 'fruit', 45.00, 20.0, 5.0);

-- Sample coupon
INSERT INTO coupons (code, discount_value, is_percentage, expiry_date) VALUES
('WELCOME10', 10, TRUE, DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
('FLAT20', 20, FALSE, DATE_ADD(CURDATE(), INTERVAL 60 DAY));

SELECT 'Database setup complete!' AS Status;

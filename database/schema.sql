CREATE DATABASE IF NOT EXISTS cloth_factory
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cloth_factory;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(30) NOT NULL UNIQUE,
    product_name VARCHAR(120) NOT NULL,
    category ENUM('RAW_MATERIAL', 'FABRIC', 'FINISHED_PRODUCT', 'CHEMICAL', 'OTHER') NOT NULL,
    unit VARCHAR(20) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    current_quantity DECIMAL(14,3) NOT NULL DEFAULT 0.000,
    minimum_stock DECIMAL(14,3) NOT NULL DEFAULT 0.000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_products_price CHECK (unit_price >= 0),
    CONSTRAINT chk_products_quantity CHECK (current_quantity >= 0),
    CONSTRAINT chk_products_minimum_stock CHECK (minimum_stock >= 0),
    INDEX idx_products_name (product_name),
    INDEX idx_products_category (category)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(120) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_suppliers_name (supplier_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchases (
    purchase_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT UNSIGNED NOT NULL,
    invoice_number VARCHAR(50) UNIQUE,
    purchase_date DATE NOT NULL,
    remarks VARCHAR(255),
    created_by BIGINT UNSIGNED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchase_items (
    purchase_item_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    purchase_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id),
    CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_purchase_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_purchase_items_price CHECK (unit_price >= 0),
    INDEX idx_purchase_items_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS production_batches (
    batch_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    batch_code VARCHAR(40) NOT NULL UNIQUE,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity_produced DECIMAL(14,3) NOT NULL,
    production_date DATE NOT NULL,
    status ENUM('PLANNED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNED',
    remarks VARCHAR(255),
    created_by BIGINT UNSIGNED,
    CONSTRAINT fk_batches_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_batches_user FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT chk_batches_quantity CHECK (quantity_produced > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT UNSIGNED NOT NULL,
    transaction_type ENUM('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT') NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    reference_type VARCHAR(30),
    reference_id BIGINT UNSIGNED,
    reason VARCHAR(255) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED,
    CONSTRAINT fk_transactions_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_transactions_user FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT chk_transactions_quantity CHECK (quantity > 0),
    INDEX idx_transactions_product_date (product_id, transaction_date),
    INDEX idx_transactions_type_date (transaction_type, transaction_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS production_materials (
    production_material_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity_consumed DECIMAL(14,3) NOT NULL,
    CONSTRAINT fk_materials_batch FOREIGN KEY (batch_id) REFERENCES production_batches(batch_id),
    CONSTRAINT fk_materials_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_materials_quantity CHECK (quantity_consumed > 0)
) ENGINE=InnoDB;

INSERT INTO users (username, password_hash, full_name, role)
VALUES ('admin', '{REPLACE_WITH_BCRYPT_HASH}', 'System Administrator', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO suppliers (supplier_name, contact_number, email, address)
SELECT 'Maharashtra Textile Suppliers', '+91-9876543210', 'sales@example.invalid', 'Bhiwandi, Maharashtra'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE supplier_name = 'Maharashtra Textile Suppliers');

INSERT INTO products (product_code, product_name, category, unit, unit_price, current_quantity, minimum_stock)
VALUES
    ('RM-YARN-001', 'Cotton Yarn', 'RAW_MATERIAL', 'kg', 260.00, 500.000, 100.000),
    ('FAB-COT-001', 'Cotton Fabric', 'FABRIC', 'meter', 180.00, 1200.000, 250.000),
    ('FAB-DEN-001', 'Denim Fabric', 'FABRIC', 'meter', 320.00, 700.000, 150.000),
    ('CHEM-DYE-001', 'Reactive Blue Dye', 'CHEMICAL', 'kg', 950.00, 80.000, 20.000),
    ('FG-SHIRT-001', 'Finished Cotton Shirt', 'FINISHED_PRODUCT', 'piece', 650.00, 150.000, 40.000)
ON DUPLICATE KEY UPDATE product_code = product_code;
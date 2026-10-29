CREATE DATABASE IF NOT EXISTS expense_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE expense_management;

CREATE TABLE IF NOT EXISTS expenses (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  category VARCHAR(40) NOT NULL,
  expenseDate DATE NOT NULL,
  paymentMethod VARCHAR(30) NOT NULL,
  description VARCHAR(500),
  INDEX idx_expenses_date (expenseDate),
  INDEX idx_expenses_category (category)
);

CREATE TABLE IF NOT EXISTS app_settings (
  setting_key VARCHAR(64) NOT NULL PRIMARY KEY,
  setting_value VARCHAR(64) NOT NULL
);

INSERT IGNORE INTO app_settings (setting_key, setting_value) VALUES ('currency', 'USD');

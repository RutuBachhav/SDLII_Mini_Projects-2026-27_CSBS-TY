# Food Stock Management System

**SDL-II Mini Project (Academic Year 2026–27)**  
**Department of Computer Science & Business Systems (CSBS) – Third Year (TY)**

---

## 👥 Team Information

| Sr. No. | Name | PRN / Roll Number | Role |
|:---:|:---|:---:|:---|
| 1 | **Shivraj Chavan** | `RBT24CB047` | Full-stack & Database Development |
| 2 | **Yogesh Bhawal** | `RBT24CB037` | System Design & Testing |
| 3 | **Vedant Deshmukh** | `RBT24CB050` | UI/UX & Documentation |

---

## 📌 Project Overview

The **Food Stock Management System** is a robust web-based enterprise inventory solution engineered to streamline the tracking, storage, and movement of perishable and non-perishable food inventory. 

In food service industries, supermarkets, and catering businesses, improper stock visibility leads to severe stockouts, inventory obsolescence, and financial loss due to spoilage. This project automates stock-in and stock-out workflows, tracks shelf-life and expiration thresholds, and generates real-time inventory insights.

---

## ✨ Key Features & Modules

1. **Executive Dashboard**:
   - Live metrics for total items in stock, items running low, items expiring soon, and total transaction volume.
   - Quick action shortcuts to key inventory operations.

2. **Inventory Management**:
   - Add new food items with name, category, quantity, unit, unit price, minimum stock threshold, supplier details, and expiry dates.
   - Edit, update, and remove existing items with immediate data persistence.

3. **Stock Transactions (In / Out)**:
   - Record inward stock arrivals (procurement/restocking) and outward stock consumption (sales/usage).
   - Dynamic inventory deduction/addition with transactional integrity.
   - Comprehensive transaction audit log with timestamps.

4. **Expiry Date Tracking & Alerts**:
   - Real-time filtering and status badges for items expiring within critical thresholds.
   - Proactive warnings to minimize food wastage.

5. **Low Stock Monitoring**:
   - Automated identification of items where quantity drops below minimum threshold levels.
   - Prevents stockouts through timely reorder notifications.

6. **Reports & Summaries**:
   - Aggregated analytical reports on total inventory valuation, category distributions, and consumption trends.

---

## 🛠️ Technology Stack

- **Backend / Platform:** Java SE 21, Jakarta EE / Java EE (Servlets, JSP, JPA)
- **ORM / Persistence:** JPA (EclipseLink)
- **Application Server:** Eclipse GlassFish 7.x
- **Database:** MySQL 8.x
- **Frontend:** JSP, HTML5, Vanilla CSS, Responsive Grid/Flexbox
- **Build System:** Apache Ant (NetBeans project structure)
- **Deployment:** Docker support (`Dockerfile`)

---

## 🗄️ Database Architecture

The system utilizes two primary relational entities created via `setup/schema.sql`:

1. **`FOOD_ITEM`**:
   - `ID` (BIGINT, Primary Key, Auto Increment)
   - `FOOD_NAME` (VARCHAR)
   - `CATEGORY` (VARCHAR)
   - `QUANTITY` (INT)
   - `UNIT` (VARCHAR)
   - `PRICE` (DOUBLE)
   - `MINIMUM_STOCK` (INT)
   - `SUPPLIER` (VARCHAR)
   - `EXPIRY_DATE` (DATE)

2. **`STOCK_TRANSACTION`**:
   - `ID` (BIGINT, Primary Key, Auto Increment)
   - `FOOD_ID` (BIGINT, Foreign Key referencing `FOOD_ITEM(ID)`)
   - `TYPE` (VARCHAR - `STOCK IN` / `STOCK OUT`)
   - `QUANTITY` (INT)
   - `TRANSACTION_DATE` (DATE)

---

## 🚀 Setup & Execution Guide

### Prerequisites
- Java Development Kit (JDK 21 or compatible)
- NetBeans IDE (v19 or higher)
- GlassFish Server 7.x
- MySQL Server 8.x

### 1. Database Initialization
1. Launch MySQL CLI or MySQL Workbench.
2. Execute the provided schema file:
   ```sql
   source setup/schema.sql;
   ```
   *(Or import `setup/schema.sql` into your MySQL instance).*

### 2. Configure Database Credentials
- Open `src/conf/persistence.xml` and verify the JDBC connection properties:
  - `jakarta.persistence.jdbc.url`: `jdbc:mysql://localhost:3306/foodstockdb`
  - `jakarta.persistence.jdbc.user`: your MySQL username (default: `root`)
  - `jakarta.persistence.jdbc.password`: your MySQL password

### 3. Open and Run in NetBeans
1. Open **NetBeans IDE**.
2. Click **File** -> **Open Project...** and select `FoodStockManagement_RBT24CB047_37_50`.
3. In the **Services** tab, ensure **GlassFish Server** is configured and started.
4. Right-click the project -> **Clean and Build**.
5. Right-click the project -> **Run**.
6. The application will launch in your browser at:
   ```
   http://localhost:8080/FoodStockManagement/
   ```

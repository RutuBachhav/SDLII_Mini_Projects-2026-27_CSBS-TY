# Complaint Management System

**Team Number:** 8

**Project Name:** Complaint Management System

## 📌 Description

The Complaint Management System is a web-based application designed to simplify the process of registering, tracking, and managing complaints. It allows users to submit complaints and monitor their status, while supporting efficient complaint management through database persistence.

## ✨ Features

- **Complaint Registration:** Register complaints through a web form.
- **Complaint Status Tracking:** Track complaints with statuses such as Pending, In Progress, and Resolved.
- **Complaint Management:** Manage and persist complaint records using JPA.
- **Database Integration:** Store complaint information in MySQL.
- **Server-Side Processing:** Process requests using Java Servlets.
- **Validation:** Validate input data using Jakarta Bean Validation.
- **Transaction Management:** Support transaction handling through Jakarta Transactions (JTA).

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Programming Languages | Java 21, HTML, CSS, JavaScript, SQL |
| Enterprise APIs | Jakarta EE |
| Web Layer | Jakarta Servlets |
| Persistence | Jakarta Persistence (JPA) |
| JPA Provider | EclipseLink |
| Validation | Jakarta Bean Validation |
| Transaction Management | Jakarta Transactions (JTA) |
| Application Server | Eclipse GlassFish 7 |
| Database | MySQL 8.0 |
| JDBC Driver | MySQL Connector/J 9.7.0 |
| Connection Pool | `jdbc/complaintdb` |
| Database Name | `complaintdb` |
| Build Tool | Apache Ant |
| IDE | Apache NetBeans |

## ⚙️ Prerequisites

Before running the project, ensure that you have:

- JDK 21 installed and configured.
- Eclipse GlassFish 7 installed.
- MySQL Server 8.0 installed and running.
- MySQL Connector/J configured for GlassFish.
- Apache NetBeans installed.
- The `complaintdb` database created in MySQL.

## 🚀 Setup and Installation

1. Clone or download this repository.
2. Open the project in Apache NetBeans.
3. Create the `complaintdb` database in MySQL.
4. Configure the database connection details according to your local MySQL setup.
5. Configure the GlassFish JDBC connection pool and the JDBC resource `jdbc/complaintdb`.
6. Verify that the persistence configuration matches the database and JNDI resource.
7. Build and deploy the application to GlassFish 7.
8. Open the application using the deployed application's URL.

**Note:** Update the database credentials and server configuration according to your local environment. Do not commit passwords or other sensitive credentials to GitHub.

## 🗄️ Database

**Database Name:** `complaintdb`

**Connection Resource:** `jdbc/complaintdb`

The database stores complaint-related information required by the application. The database schema and entity mappings should match the implementation included in the project.

## 📁 Project Structure

The project may contain the following components, depending on the actual NetBeans project structure:

- `src/` — Java source files, Servlets, entities, and persistence logic.
- `web/` — HTML pages, CSS, JavaScript, and web resources.
- `nbproject/` — NetBeans project configuration.
- `build.xml` — Apache Ant build configuration.
- `README.md` — Project documentation.

## 👥 Team Members

Add the names and roll numbers of all team members below.

| Sr. No. | Name | Roll Number |
|---|---|---|
| 1 | Mrudula Alande  | RBT24CB023 |
| 2 | Renuka Bagade   | RBT24CB024 |
| 3 | Sanidya Gawande | RBT24CB025 |


## 🎯 Objective

The objective of this project is to provide a structured and efficient platform for complaint registration, status tracking, and complaint record management using Java enterprise technologies and a relational database.

## 📄 Academic Submission

**Subject:** SDL-II Mini Project  
**Academic Year:** 2026–27  
**Team Number:** 8  
**Project:** Complaint Management System

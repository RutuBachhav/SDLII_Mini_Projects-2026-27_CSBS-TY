# Expense Management System

A compact Java web application for recording and maintaining business expenses. It uses Java 11, Maven, Servlet 4, JPA 2.2 with EclipseLink, MySQL and Tomcat 9. The interface uses a restrained navy, slate and white accounting style.

## Features

- Create, list, search, edit and delete expenses
- All-time and current-month totals, plus record count
- Saved display currency shared by this installation: USD, INR, EUR, GBP, JPY, CAD, AUD, CHF, CNY, SGD, NZD or AED
- Previous-calendar-month total and a category-wise horizontal bar chart; Food is available as a separate category
- Labeled demo expenses for the previous and current month, reloadable with `database/sample_data.sql`
- Required-field and positive-amount validation
- Prepared JPA queries and output escaping in the view
- Responsive expense register and form

## Requirements

- JDK 11 or newer
- Maven 3.8+
- MySQL 8+
- Apache Tomcat 9 (Servlet 4 / `javax.*`)

Tomcat 10+ uses `jakarta.*` APIs and is not compatible with this project without migrating the dependencies and imports.

## Database setup

Run `database/schema.sql` in MySQL Workbench or the MySQL client. To populate the register and chart with clearly labeled examples, run `database/sample_data.sql` after the schema. It is safe to rerun: existing demo records for the same month are skipped. Example amounts are illustrative and use the currently selected display currency. Configure the following environment variables for the Tomcat process before starting it:

```text
DB_URL=jdbc:mysql://localhost:3306/expense_management?useSSL=false&serverTimezone=UTC
DB_USER=your_mysql_user
DB_PASSWORD=your_mysql_password
```

The application reads these values from the Tomcat process environment. The URL defaults to the local database shown above and the username defaults to `root`; set `DB_PASSWORD` explicitly. Do not put production credentials in source control.

## Build and run

From this project directory:

```bash
mvn clean package
```

Copy `target/expense-management.war` to Tomcat 9's `webapps` folder, start Tomcat, then open `http://localhost:8080/expense-management/`. The welcome file routes to the expense register. The application expects the schema to exist and does not modify database structure automatically.

## VS Code

Open this folder in VS Code with the Extension Pack for Java installed. Use the integrated terminal for Maven commands. For local development, start Tomcat with the three database environment variables set in the same environment.

## Start locally from VS Code

This project includes a portable Tomcat 9 runtime in `.local-run` (kept out of source control). After `DB_USER` and `DB_PASSWORD` are set in Windows machine environment variables and the schema has been created, open the project folder in VS Code and run:

```powershell
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' clean package
powershell -ExecutionPolicy Bypass -File .\start-local.ps1
```

The start script deploys the WAR, starts Tomcat on port 8080, checks the app, and opens it in the browser. Use `powershell -ExecutionPolicy Bypass -File .\stop-local.ps1` to stop Tomcat.

The selected currency is stored in `app_settings` and remains selected until changed. It applies to the shared installation because there are no individual user accounts. Changing it changes displayed symbols only; it does not convert saved amounts.



# DriveFlow — Vehicle Rental Management System

A Java web application for Apache NetBeans + GlassFish 7.1.x, using Jakarta EE 10, Maven, JSP/Servlets, JDBC and MySQL 8.x.

## Modules
- Admin login/logout and session-protected application routes
- Dashboard counts and recent rentals (live MySQL queries)
- Vehicle management: create, list/search, edit, safe deactivation
- Customer management: create, list/search, edit, safe deactivation
- Rental booking with date checks and overlap prevention
- Returns with final charge calculation and vehicle availability update
- Payment recording with outstanding-balance and overpayment checks
- Rental financial report and print stylesheet
- Responsive CSS, shared navigation, form validation and confirmations

## Requirements
- JDK 17 or newer
- Apache NetBeans with Maven support
- GlassFish 7.1.x
- MySQL 8.x

## 1. Create the database
1. Start MySQL.
2. Open MySQL Workbench or a MySQL terminal.
3. Run all of `database/driveflow.sql`.
4. For a local college demo, the project defaults to MySQL `root` with an empty password unless a `db.properties` resource is supplied. Recommended: create a dedicated database user and set its password in `src/main/resources/db.properties` (copy the example file first). Never commit real credentials.

Example dedicated account (run as a MySQL administrator):
```sql
CREATE USER 'driveflow_user'@'localhost' IDENTIFIED BY 'choose_a_strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX ON driveflow.* TO 'driveflow_user'@'localhost';
FLUSH PRIVILEGES;
```
Then copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and update the password.

## 2. Open in NetBeans
1. Extract the ZIP.
2. In NetBeans choose **File → Open Project** and select the extracted `DriveFlow` folder containing `pom.xml`.
3. Set the project JDK to 17+ and choose GlassFish 7.1.x as the server.
4. Right-click project → **Clean and Build**. Maven downloads the MySQL Connector/J dependency.
5. Right-click project → **Run**. Open the context URL printed by NetBeans, normally `http://localhost:8080/DriveFlow/`.

## 3. Demo login
- Username: `admin`
- Password: `admin123`

The SQL seed stores a PBKDF2-HMAC-SHA256 password hash. Before real use, change the demo password and add a secure password-change flow. Do not expose this demo account on a public deployment.

## 4. Common problems
- **No suitable driver / ClassNotFoundException**: confirm Maven resolved `com.mysql:mysql-connector-j`, then Clean and Build. The app explicitly loads `com.mysql.cj.jdbc.Driver`.
- **Access denied for user**: verify MySQL username/password in `db.properties`; ensure the account host is `localhost`.
- **Unknown database driveflow**: run `database/driveflow.sql` before starting the app.
- **Port already in use**: change GlassFish's HTTP listener port or stop the conflicting process.
- **JDK mismatch**: configure NetBeans/Maven to use JDK 17+ and rebuild.
- **Duplicate registration, email or licence**: these fields are constrained unique in MySQL; use a distinct value.

## Business rules implemented
- Rental estimate uses `daily_rate × (days between start and expected return + 1)`.
- Start date cannot be in the past; expected return cannot precede start date.
- Booking checks vehicle status and overlapping booked/active rental dates in a transaction.
- Returning a booked/active rental records actual return date and final charge, marks the rental completed, and makes a rented vehicle available again.
- Payments must be positive and cannot exceed the remaining balance.
- Vehicle/customer deactivation is blocked while booked/active rentals exist.

## Important limitations
This is a local educational/admin application, not a production-hardened rental platform. Before public deployment, add CSRF protection, role-based permissions, rate limiting, a robust password-reset/change flow, audit logs, secret management, and integration tests. Review the demo login seed before any deployment.

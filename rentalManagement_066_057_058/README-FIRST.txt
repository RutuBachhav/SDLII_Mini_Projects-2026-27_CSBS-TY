VEHICLE RENTAL MANAGEMENT SYSTEM — FULL PROJECT PACKAGE

Stack: Java 17, Maven WAR, Jakarta EE 10, GlassFish 7.1.x, JSP/Servlets, MySQL 8.

IMPORTANT ABOUT THE ZIP:
This package is a complete replacement project. BACK UP your current NetBeans project first.
Do not extract the zip over the existing project while NetBeans is running. The simplest route:
1. Close or keep NetBeans open, but do not overwrite the live folder blindly.
2. Extract this ZIP to a new folder, e.g. C:\Users\<you>\Downloads\NetBeans\VehicleRentalManagementSystem.
3. In NetBeans choose File > Open Project and select the extracted project folder.
4. Set project Java Platform to JDK 17 (or change pom.xml release only if your setup supports a different JDK).
5. Make sure GlassFish 7.1.1 is registered in NetBeans and selected as the project server.
6. In MySQL Workbench run database/schema.sql.
7. Open src/main/java/com/mycompany/rentalmanagement/util/DBConnection.java and set your local MySQL username/password.
8. Right-click project > Reload Project, then Clean and Build, then Run.
9. If asked to choose server, select GlassFish 7.1.1.
URL: usually http://localhost:8080/rentalManagement/

LOGIN:
Username: admin
Password: admin123
This is a demo credential for a local college project. Change it before any real deployment.
The login is session-based and not intended for production security.

FUNCTIONAL MODULES:
- Dashboard counts and revenue
- Vehicle add/edit/delete/search/status
- Customer add/edit/delete/search
- Rental creation (only available vehicles), rental charge calculation, vehicle status update
- Return vehicle, late fee calculation, rental completion and vehicle availability update
- Payments: record payments, list history, payment status/method
- Reports: rental history, revenue summary
- Login/logout

DATABASE:
The SQL script creates vehicle_rental_db and its tables, and inserts a few sample vehicles/customers only if they do not already exist.
Use unique registration and license numbers for new entries.

TROUBLESHOOTING:
- "No suitable driver": ensure Maven Dependencies includes mysql-connector-j; clean/build/redeploy. DBConnection explicitly loads the driver.
- "Access denied": username/password in DBConnection do not match local MySQL.
- 404: check deployment context path and GlassFish logs.
- Compilation failure: make sure NetBeans uses JDK 17 and GlassFish 7.1.x / Jakarta EE 10.
- Do not use an old Java EE server that only supports javax.servlet APIs.

# CourierBay - Courier Management System

An online courier booking and tracking web application developed using Java EE, Servlets, JSP/HTML, and MySQL.

## Features
- **Courier Booking**: Users can book couriers by specifying sender/receiver details, address, weight, and courier type.
- **Real-Time Tracking**: Users can track the delivery status using an auto-generated unique Tracking ID.
- **Admin Dashboard**: Administrators can log in, view all booked couriers, and update delivery statuses (Booked, In Transit, Out for Delivery, Delivered).

## Tech Stack
- **Backend**: Java Servlets (Jakarta EE)
- **Frontend**: HTML5, CSS3, JavaScript
- **Database**: MySQL (`courier_bay`)
- **Server**: Apache Tomcat / GlassFish Server
- **Build Tool**: Maven

## Database Setup
1. Open MySQL Command Line or MySQL Workbench.
2. Execute the queries inside `database.sql`:
   ```sql
   CREATE DATABASE courier_bay;
   USE courier_bay;
   ```
3. Run the schema creation query provided in `database.sql`.
4. Ensure MySQL server is running on `localhost:3306` with user `root`.

# Hostel Management System

NetBeans + Maven + GlassFish 7.1.1 + Jakarta EE 10 + MySQL.

## Modules

### Admin
- Login
- Dashboard
- Add/Edit/Delete rooms
- Admit students
- Automatically create student login credentials
- Student list
- Add fee records
- Mark fees as paid
- Delete fee records
- View, add, resolve and delete complaints
- Logout

### Student
- Login
- Dashboard
- View assigned room
- View fee history
- View complaint history
- Raise a complaint
- Logout

## Software
- NetBeans
- JDK 11 or newer
- GlassFish 7.1.1
- MySQL 8
- Maven

## Database
1. Open MySQL Workbench.
2. Open `database.sql`.
3. Run the complete script.
4. If your MySQL root password is not `123456789`, change it in:
   `src/main/resources/META-INF/persistence.xml`

## NetBeans
1. File -> Open Project.
2. Select this `HostelManagementSystem` folder.
3. Make sure GlassFish 7.1.1 is registered.
4. Right click project -> Clean and Build.
5. Right click project -> Run.

Open:
`http://localhost:8080/HostelManagementSystem/`

## Default admin
Username: `admin`
Password: `admin123`

## Student account
When the admin admits a student, the form asks for:
- Student username
- Student password

The account is automatically linked to that student.

## Important
This is an academic/demo project. Passwords are stored as plain text to keep the project simple for a college practical. For production, use password hashing such as BCrypt/Argon2.

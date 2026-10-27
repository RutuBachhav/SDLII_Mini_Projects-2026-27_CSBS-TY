PARKEASE — PARKING MANAGEMENT SYSTEM
=====================================

Stack: Java 21, Jakarta EE 10 (JSP/Servlet), Maven WAR, MySQL, MySQL Workbench, NetBeans, GlassFish 7.

FEATURES / PAGES
1. Dashboard — occupancy summary and quick actions.
2. Vehicle Entry — register a vehicle into an available slot.
3. Parking Slots — live slot status and release a parked vehicle.
4. Parking History — recent parking sessions and fee calculation.

IMPORTANT
- This starter project uses a local MySQL database named parkease_db.
- Default database settings are in src/main/java/com/parkease/util/DBUtil.java.
- Update DB_USER and DB_PASSWORD to match your local MySQL account.
- The project creates tables automatically when the app starts, but you should also run database/schema.sql in MySQL Workbench first.
- The project is configured for JDK 21 + GlassFish 7. It uses jakarta.* packages, not javax.*.
- First build may need internet access so Maven can download dependencies.

FEE RULE
The current starter uses ₹40 for the first hour and ₹20 for every additional started hour. This is a demo rule and can be changed in ParkingServlet.java.

NEXT
Extract the ZIP, open the folder in NetBeans using File > Open Project, run schema.sql in Workbench, set your DB password, then Run the project.

# LabLoop – Smart Laboratory Resource & Equipment Management System

SDL-II Mini Project (TY CSBS, 2026-27) · Team: **[Team Name]**

LabLoop replaces the paper issue register used in college labs. Students and faculty book lab equipment online, lab assistants approve, issue and collect it, and faulty or overdue items are tracked automatically.

## Problem
- No easy way to check if equipment (oscilloscope, Raspberry Pi kit, robotic arm...) is free
- Same item promised to two batches for the same slot
- Late or missing returns, because the register reminds nobody
- Faulty equipment noticed only in the middle of a practical; service dates forgotten

## Features
| User | What they can do |
|---|---|
| Student | Search equipment, book one item or a full **Practical Kit** (several items, one slot), cancel, report a fault |
| Faculty | Same as students, bookings approved instantly |
| Lab Assistant | Approve / reject requests, issue and collect items, mark damage, handle repairs, add equipment and labs |
| Admin | Everything above + add members, change booking rules |

Other features: overlap (clash) check on every booking, overdue tracking, service-due dates, activity log, live dashboard, REST API.

Booking flow: **Requested → Approved → Issued → Returned** (or Rejected / Cancelled).

## Technology
JDK 21 · NetBeans 30 · GlassFish 7.1.1 · Jakarta EE 10 · Java DB (Derby) / MySQL

| SDL-II concept | Where it is used |
|---|---|
| Stateless EJB | `service/BookingService`, `EquipmentService`, `LabService`, `MaintenanceService`, `UserService` |
| Stateful EJB | `service/PracticalKitBean` – each user's Practical Kit |
| Singleton EJB | `service/LabLoopSettingsBean` (settings, demo data at startup), `service/LabMonitorBean` (dashboard cache, timer) |
| JPA + Bean Validation | `entity/*` – Lab, Equipment, LabUser, Booking, MaintenanceTicket, ActivityLog |
| Relationships | Lab 1→* Equipment, Equipment 1→* Booking, Equipment 1→* MaintenanceTicket, LabUser 1→* Booking |
| Servlets (CRUD) | `web/*Servlet` + JSP pages in `web/WEB-INF/views` |
| REST (JAX-RS) | `rest/*` → `/api/equipment`, `/api/labs`, `/api/bookings`, `/api/stats` |
| CDI | `cdi/UserSession`, `cdi/BookingPolicy`, `cdi/LabActivity` (event), `cdi/AuditInterceptor` |

Architecture: JSP (view) → Servlets / REST (controller) → EJB + CDI (business logic) → JPA → database.

## How to run (NetBeans)
1. Install JDK 21, NetBeans and GlassFish 7, and add GlassFish in NetBeans (Services → Servers).
2. **File → Open Project** → select this `LabLoop` folder.
3. If NetBeans shows a problem, right-click the project → **Resolve Project Problems** → choose your GlassFish 7 and JDK 21.
4. Start Java DB: Services → Databases → Java DB → Start Server.
5. Right-click the project → **Clean and Build**, then **Run**.
6. Open `http://localhost:8080/LabLoop/`. Demo data loads automatically on first run.

Demo logins (password `labloop123`): `admin@labloop.edu`, `assistant@labloop.edu`, `faculty@labloop.edu`, `student@labloop.edu`

**Using MySQL instead of Java DB:** put your MySQL password in `src/java/com/labloop/service/LabLoopSettingsBean.java`, swap the two `jta-data-source` lines in `src/java/META-INF/persistence.xml`, and copy `mysql-connector-j` into `glassfish7/glassfish/domains/domain1/lib`. The `labloop` database is created automatically.

## Future scope
QR code stickers on equipment, email/SMS reminders before the due time, hosting online for the whole college.

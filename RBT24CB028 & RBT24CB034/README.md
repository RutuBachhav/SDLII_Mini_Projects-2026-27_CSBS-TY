# CampusCare – College Complaint & Service Management System

> **Enterprise Java (Jakarta EE 10) Full-Stack Web Application**  
> Developed for Academic Evaluation and NetBeans IDE Integration

![CampusCare admin dashboard](Report_Assets/screenshots/10_admin_dashboard.png)

---

## 1. Project Overview

**CampusCare** is a full-stack Enterprise Java web platform designed to streamline, track, and resolve campus service requests and complaints across educational institutions. The application connects three primary stakeholders:

- **Students**: Lodge complaints across campus categories, track live resolution status with visual timelines, receive instant notifications, and confirm closure when satisfied.
- **Staff**: Manage assigned complaints, accept service assignments, log real-time progress notes, and report resolutions.
- **Administrators**: Review college-wide analytics, monitor departmental resolution rates, manage departments, students, and staff, and route complaints to qualified staff members.

The application adheres strictly to **Jakarta EE / Enterprise Java specifications** (EJB, JPA, JAX-RS, CDI, JMS, MDB, Bean Validation) without Spring Boot.

---

## 2. Key Features

### Student Features
- **Account Registration & Authentication**: Secure registration with roll number, branch, year, division, and password hashing (PBKDF2).
- **Interactive Dashboard**: Summary metrics (Total Complaints, Pending, In Progress, Resolved).
- **Lodge Complaint**: Categorized submissions (Infrastructure, Computer/Lab, Electricity, Water, Cleanliness, Library, Wi-Fi, etc.) with priority levels (`LOW`, `MEDIUM`, `HIGH`, `URGENT`).
- **Live Complaint Tracking**: Complete chronological lifecycle timeline (`PENDING` → `ASSIGNED` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`).
- **Resolution Confirmation**: Students review staff resolution notes and mark tickets as closed.
- **Notifications**: Real-time notifications powered by asynchronous JMS messaging.

### Staff Features
- **Staff Portal & Dashboard**: Overview of assigned, active, and resolved tickets.
- **Accept Assignment**: Transition complaints from `ASSIGNED` to `IN_PROGRESS`.
- **Progress Updates**: Attach remarks and status updates.
- **Mark as Resolved**: Mark tickets as `RESOLVED` with detailed repair notes.

### Administrator Features
- **Executive Analytics Dashboard**: System metrics, status counts, category distribution, and department breakdown.
- **Staff Assignment**: Route pending complaints to designated staff with optional priority escalation.
- **Department Management**: View departments, track staff/complaint counts, and register new departments.
- **User Directory**: Browse registered students and staff directory.

---

## Screenshots

### Sign in & Registration

| Login | Student Registration | Staff Registration |
|---|---|---|
| <img src="Report_Assets/screenshots/01_login.png" alt="Login" width="100%"> | <img src="Report_Assets/screenshots/02_register.png" alt="Student Registration" width="100%"> | <img src="Report_Assets/screenshots/03_register_staff.png" alt="Staff Registration" width="100%"> |

### Student Portal

| Dashboard | Submit Complaint | My Complaints |
|---|---|---|
| <img src="Report_Assets/screenshots/04_student_dashboard.png" alt="Dashboard" width="100%"> | <img src="Report_Assets/screenshots/05_submit_complaint.png" alt="Submit Complaint" width="100%"> | <img src="Report_Assets/screenshots/06_my_complaints.png" alt="My Complaints" width="100%"> |

| Complaint Timeline | Notifications | Profile |
|---|---|---|
| <img src="Report_Assets/screenshots/07_complaint_timeline.png" alt="Complaint Timeline" width="100%"> | <img src="Report_Assets/screenshots/08_notifications.png" alt="Notifications" width="100%"> | <img src="Report_Assets/screenshots/09_student_profile.png" alt="Profile" width="100%"> |

### Admin Portal

| Dashboard | All Complaints | Assign Staff |
|---|---|---|
| <img src="Report_Assets/screenshots/10_admin_dashboard.png" alt="Dashboard" width="100%"> | <img src="Report_Assets/screenshots/11_admin_all_complaints.png" alt="All Complaints" width="100%"> | <img src="Report_Assets/screenshots/12_assign_modal.png" alt="Assign Staff" width="100%"> |

| Departments | New Department | Staff Directory |
|---|---|---|
| <img src="Report_Assets/screenshots/13_departments.png" alt="Departments" width="100%"> | <img src="Report_Assets/screenshots/14_new_department_modal.png" alt="New Department" width="100%"> | <img src="Report_Assets/screenshots/15_staff_directory.png" alt="Staff Directory" width="100%"> |

| Student Directory |
|---|
| <img src="Report_Assets/screenshots/16_student_directory.png" alt="Student Directory" width="100%"> |

### Staff Portal

| Dashboard | Assigned Complaints | Update Status |
|---|---|---|
| <img src="Report_Assets/screenshots/17_staff_dashboard.png" alt="Dashboard" width="100%"> | <img src="Report_Assets/screenshots/18_staff_assigned.png" alt="Assigned Complaints" width="100%"> | <img src="Report_Assets/screenshots/19_staff_update_modal.png" alt="Update Status" width="100%"> |

---

## 3. Technology Stack & Environment

| Component | Technology / Version | Description |
|---|---|---|
| **Language** | Java 17 LTS / Java 25 | Compiles with Java 17 release target |
| **Enterprise Standard** | Jakarta EE 10 Full Platform | Core enterprise profile |
| **Application Server** | Eclipse GlassFish 7.0.18 | Official Jakarta EE 10 reference implementation |
| **Enterprise Beans** | Jakarta Enterprise Beans 4.0 (EJB) | `@Stateless` Session Beans, `@Singleton` Startup Bean |
| **Asynchronous Messaging** | Jakarta Messaging 3.1 (JMS) + OpenMQ | Queue Producer & `@MessageDriven` Bean (MDB) |
| **Persistence** | Jakarta Persistence 3.1 (JPA) + EclipseLink | Container-managed JTA datasource |
| **Dependency Injection** | Jakarta CDI 4.0 | `@Inject`, `@ApplicationScoped`, `@RequestScoped` |
| **REST APIs** | Jakarta RESTful Web Services 3.1 (JAX-RS) | Clean JSON API endpoints with `@Path`, `@Produces` |
| **Validation** | Jakarta Bean Validation 3.0 | `@NotBlank`, `@Size`, `@Email`, `@NotNull` annotations |
| **Database** | MySQL Server 26.7 | Port 3306, utf8mb4 encoding, JDBC Connection Pool |
| **Build Tool** | Apache Maven 3.9.16 | Maven standard directory layout |
| **Frontend** | HTML5, CSS3, Modern JavaScript (SPA) | Fully responsive, university-grade UI without external frameworks |
| **IDE Support** | Apache NetBeans 21/24/31 | Native Maven web project ready to open and run |

---

## 4. Syllabus Mapping

| Syllabus Requirement | Implementation in CampusCare | Source Location |
|---|---|---|
| **Enterprise Java Beans (EJB)** | `@Stateless` session beans separating business logic from presentation | `com.campuscare.ejb.*` (`ComplaintServiceBean`, `UserServiceBean`, `DepartmentServiceBean`, `NotificationServiceBean`, `DashboardServiceBean`) |
| **Persistence Entities & Validations** | JPA entities with Jakarta Bean Validation annotations | `com.campuscare.entity.*` (`User`, `Student`, `Staff`, `Department`, `Complaint`, `ComplaintUpdate`, `Notification`) |
| **JPA Entity Relationships** | Real relational mappings (`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@JoinColumn`, cascading, lazy loading) | Student 1:* Complaint, Dept 1:* Staff, Dept 1:* Complaint, Staff 1:* Complaint, Complaint 1:* ComplaintUpdate, User 1:1 Student/Staff |
| **REST APIs using JAX-RS** | Clean JSON endpoints covering auth, complaints, departments, staff, students, notifications, dashboards | `com.campuscare.rest.*` (`AuthResource`, `ComplaintResource`, `DepartmentResource`, `StaffResource`, `StudentResource`, `NotificationResource`, `DashboardResource`) |
| **CDI Dependency Injection** | Layered injection between REST resources, services, and messaging producers | `com.campuscare.messaging.ComplaintJmsProducer`, `com.campuscare.config.CorsFilter`, `META-INF/beans.xml` |
| **JMS Messaging** | Asynchronous decoupling: Event creation sends JMS message to destination queue | `com.campuscare.messaging.ComplaintJmsProducer`, `jms/CampusCareQueue`, `jms/CampusCareConnectionFactory` |
| **Message-Driven Bean (MDB)** | `@MessageDriven` bean listening to queue and processing notifications asynchronously | `com.campuscare.messaging.ComplaintNotificationMDB` |
| **MySQL Persistence** | Container-managed JDBC Connection Pool and JTA Datasource connected to MySQL 26.7 | `CampusCarePool`, `jdbc/CampusCareDS`, `persistence.xml` |

---

## 5. Architecture & Layered Design

```
┌──────────────────────────────────────────────────────────┐
│                   Frontend (SPA)                         │
│         HTML5 / Modern CSS / Vanilla JavaScript          │
└────────────────────────────┬─────────────────────────────┘
                             │ HTTP JSON / REST
┌────────────────────────────▼─────────────────────────────┐
│              Presentation Layer (JAX-RS)                 │
│   AuthResource, ComplaintResource, DashboardResource...  │
└────────────────────────────┬─────────────────────────────┘
                             │ @EJB / @Inject
┌────────────────────────────▼─────────────────────────────┐
│               Business Layer (Stateless EJBs)            │
│  UserServiceBean, ComplaintServiceBean, DashboardBean...  │
└──────────────┬─────────────────────────────┬─────────────┘
               │                             │
    EntityManager (JPA)               JMS Event Producer
               │                             │
┌──────────────▼─────────────┐ ┌─────────────▼─────────────┐
│  EclipseLink / Hibernate   │ │   GlassFish JMS (OpenMQ)  │
│      JTA Datasource        │ │     jms/CampusCareQueue   │
└──────────────┬─────────────┘ └─────────────┬─────────────┘
               │                             │ Consumes
┌──────────────▼─────────────┐ ┌─────────────▼─────────────┐
│      MySQL Server 26.7     │ │  ComplaintNotificationMDB │
│     `campuscare` database  │ │ (Creates Notifications)   │
└────────────────────────────┘ └───────────────────────────┘
```

---

## 6. Project Directory Structure

```
CampusCare/
├── pom.xml                               # Maven project configuration
├── README.md                             # Documentation & Setup guide
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/campuscare/
│   │   │       ├── config/
│   │   │       │   ├── ApplicationConfig.java    # JAX-RS @ApplicationPath("/api")
│   │   │       │   ├── CorsFilter.java           # Container response filter
│   │   │       │   └── DataInitializer.java     # Singleton Startup EJB for seed data
│   │   │       ├── dto/                          # Transfer objects for REST APIs
│   │   │       │   ├── ApiResponse.java
│   │   │       │   ├── AuthResponse.java
│   │   │       │   ├── LoginRequest.java
│   │   │       │   ├── RegisterRequest.java
│   │   │       │   ├── ComplaintRequest.java
│   │   │       │   ├── ComplaintResponse.java
│   │   │       │   ├── ComplaintUpdateRequest.java
│   │   │       │   ├── AssignRequest.java
│   │   │       │   ├── StatusChangeRequest.java
│   │   │       │   ├── DepartmentDto.java
│   │   │       │   ├── StudentDto.java
│   │   │       │   ├── StaffDto.java
│   │   │       │   ├── NotificationDto.java
│   │   │       │   └── DashboardStatsDto.java
│   │   │       ├── ejb/                          # Stateless Session Beans (Business logic)
│   │   │       │   ├── UserServiceBean.java
│   │   │       │   ├── ComplaintServiceBean.java
│   │   │       │   ├── DepartmentServiceBean.java
│   │   │       │   ├── NotificationServiceBean.java
│   │   │       │   └── DashboardServiceBean.java
│   │   │       ├── entity/                       # JPA Relational Entities
│   │   │       │   ├── User.java
│   │   │       │   ├── Student.java
│   │   │       │   ├── Staff.java
│   │   │       │   ├── Department.java
│   │   │       │   ├── Complaint.java
│   │   │       │   ├── ComplaintUpdate.java
│   │   │       │   └── Notification.java
│   │   │       ├── exception/
│   │   │       │   ├── AppException.java
│   │   │       │   └── GlobalExceptionMapper.java
│   │   │       ├── messaging/                    # JMS Producer & Message-Driven Bean
│   │   │       │   ├── ComplaintEvent.java
│   │   │       │   ├── ComplaintJmsProducer.java
│   │   │       │   └── ComplaintNotificationMDB.java
│   │   │       └── security/
│   │   │           ├── AuthContext.java
│   │   │           ├── PasswordUtil.java         # PBKDF2 with SHA-256 secure hashing
│   │   │           └── TokenUtil.java            # HMAC-SHA256 signed stateless tokens
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       ├── beans.xml                     # CDI descriptor
│   │   │       └── persistence.xml               # JPA Persistence Unit & Datasource
│   │   └── webapp/
│   │       ├── css/
│   │       │   └── style.css                     # Responsive styling & themes
│   │       ├── js/
│   │       │   ├── api.js                        # REST API client
│   │       │   └── app.js                        # SPA routing, view rendering, modals
│   │       ├── WEB-INF/
│   │       │   ├── beans.xml
│   │       │   ├── glassfish-web.xml             # Context root /CampusCare
│   │       │   └── web.xml                       # Servlet descriptor
│   │       └── index.html                        # Single Page Application UI
```

---

## 7. Demo Credentials

The database is pre-seeded with sample users on first deployment:

| Role | Email Address | Password | Name / Details |
|---|---|---|---|
| **Admin** | `admin@campuscare.com` | `admin123` | System Administrator |
| **Staff** | `staff@campuscare.com` | `staff123` | Prof. Rajesh Sharma (Computer & IT Labs) |
| **Staff** | `electrical.staff@campuscare.com` | `staff123` | Sunil Verma (Electrical Maintenance) |
| **Student** | `student@campuscare.com` | `student123` | Aarav Patel (CS-2024-001, Computer Engg.) |
| **Student** | `neha.student@campuscare.com` | `student123` | Neha Kulkarni (IT-2024-042, IT) |

*Note: The login page includes 1-click **Quick Demo** buttons for instant testing without typing credentials.*

---

## 8. Database Configuration (MySQL 26.7)

- **Host**: `localhost`
- **Port**: `3306`
- **Database**: `campuscare`
- **Root User**: `root`
- **Root Password**: `160306`

### GlassFish JDBC Resources
- **Connection Pool**: `CampusCarePool`
  - DataSource Class: `com.mysql.cj.jdbc.MysqlDataSource`
  - Properties: `User=root:Password=160306:ServerName=localhost:PortNumber=3306:DatabaseName=campuscare:sslMode=DISABLED:useSSL=false:allowPublicKeyRetrieval=true:serverTimezone=UTC`
- **JNDI Resource**: `jdbc/CampusCareDS`

---

## 9. JMS Messaging Workflow

1. A student lodges a complaint or an admin assigns a staff member.
2. `ComplaintServiceBean` invokes `ComplaintJmsProducer.sendEvent(ComplaintEvent)`.
3. The producer creates an `ObjectMessage` and publishes it to `jms/CampusCareQueue`.
4. `ComplaintNotificationMDB` consumes the message from `jms/CampusCareQueue` asynchronously.
5. `NotificationServiceBean` persists a `Notification` entity into the MySQL database.
6. The frontend periodically polls `/api/notifications`, updating the notification bell badge and dropdown.

---

## 10. REST API Reference

All endpoints are rooted at `/CampusCare/api`.

### Authentication
- `POST /api/auth/login` – Authenticate user and receive token
- `POST /api/auth/register` – Register Student, Staff, or Admin
- `GET  /api/auth/me` – Get current authenticated profile

### Complaints
- `GET    /api/complaints` – List complaints (supports filters: `status`, `category`, `priority`, `departmentId`)
- `GET    /api/complaints/{id}` – View full complaint details with lifecycle updates
- `POST   /api/complaints` – Submit new complaint (Student role)
- `PUT    /api/complaints/{id}/assign` – Assign complaint to staff (Admin role)
- `PUT    /api/complaints/{id}/status` – Update complaint status
- `POST   /api/complaints/{id}/updates` – Add progress note / update status (Staff role)
- `DELETE /api/complaints/{id}` – Delete complaint (Student/Admin role)

### Departments
- `GET    /api/departments` – List all departments with counts
- `GET    /api/departments/{id}` – Get department details
- `POST   /api/departments` – Create new department (Admin role)
- `PUT    /api/departments/{id}` – Update department (Admin role)

### Staff & Students
- `GET    /api/staff` – List staff members
- `GET    /api/staff/{id}/complaints` – Complaints assigned to staff member
- `GET    /api/students` – List registered students (Admin/Staff role)
- `GET    /api/students/{id}/complaints` – Complaints lodged by student

### Notifications
- `GET    /api/notifications` – List user notifications
- `PUT    /api/notifications/{id}/read` – Mark notification as read
- `PUT    /api/notifications/read-all` – Mark all notifications as read

### Dashboards
- `GET    /api/admin/dashboard` – Overall statistics, category & department counts
- `GET    /api/staff/dashboard` – Staff specific metrics
- `GET    /api/students/dashboard` – Student specific metrics

---

## 11. How to Open and Run in NetBeans

1. **Open NetBeans**:
   - Launch Apache NetBeans from `C:\Users\CHINMAY\Downloads\netbeans-31-bin\netbeans\bin\netbeans64.exe` or desktop shortcut.
2. **Open Project**:
   - Go to **File → Open Project...**
   - Browse to the folder where you cloned this repository.
   - NetBeans will recognize the Maven project (`CampusCare`) indicated by the project icon.
3. **Register GlassFish 7 in NetBeans** (if not already listed):
   - In NetBeans, open the **Services** tab (Window → Services).
   - Right-click **Servers → Add Server...**
   - Select **GlassFish Server**, click **Next**.
   - Server Location: `C:\Users\CHINMAY\glassfish7\glassfish`
   - Java Platform: Select Java 17.
   - Click **Finish**.
4. **Build & Run**:
   - Right-click the `CampusCare` project in the Projects panel and select **Clean and Build**.
   - Right-click `CampusCare` and select **Run**.
   - NetBeans will automatically deploy to GlassFish and open your default browser at:  
     `http://localhost:8080/CampusCare/`

---

## 12. Command-Line Build & Deploy

If you prefer building and deploying via terminal:

```powershell
# 1. Build the WAR with Maven (Java 17)
$env:JAVA_HOME = "C:\Users\CHINMAY\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2"
& "C:\Users\CHINMAY\Downloads\netbeans-31-bin\netbeans\java\maven\bin\mvn.cmd" clean package

# 2. Deploy to GlassFish 7
& "C:\Users\CHINMAY\glassfish7\glassfish\bin\asadmin.bat" deploy --force=true target/CampusCare.war
```

The application is accessible in the browser at:  
**`http://localhost:8080/CampusCare/`**

---

## Cloud Deployment (Docker + Render + MySQL)

The repository includes a `Dockerfile` that builds the WAR and runs it on GlassFish 7.0.18 with the JMS resources pre-configured. The MySQL datasource is created at container start from environment variables:

| Variable | Example |
|---|---|
| `DB_HOST` | `mysql-xxxx.aivencloud.com` |
| `DB_PORT` | `12345` |
| `DB_NAME` | `defaultdb` |
| `DB_USER` | `avnadmin` |
| `DB_PASSWORD` | *(your database password)* |
| `DB_SSL_MODE` | `REQUIRED` (use `DISABLED` for a local MySQL) |

**Deploy on Render:** New → Blueprint → select this repository (uses `render.yaml`), then fill in the database variables. Tables and demo accounts are created automatically on first start.

**Run locally with Docker:**

```bash
docker build -t complaintcampus .
docker run -p 8080:8080 -e DB_HOST=host.docker.internal -e DB_USER=root -e DB_PASSWORD=yourpass -e DB_NAME=campuscare -e DB_SSL_MODE=DISABLED complaintcampus
```

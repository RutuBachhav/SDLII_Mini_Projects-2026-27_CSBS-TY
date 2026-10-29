# Placement Management System

**SDL-II Mini Project (Academic Year 2026–27)**  
**Department of Computer Science & Business Systems (CSBS) – Third Year (TY)**

A centralized web-based Placement Management System connecting students, the placement cell, and recruiters. Built as a standard Maven WAR (Java 17, Jakarta EE 10 Servlets/JSP, JDBC, MySQL 8) and deployed on Eclipse GlassFish 7.1.0 with Apache NetBeans. No Spring Boot, no embedded Tomcat.

---

## 👥 Team Information

| Sr. No. | Name | Roll Number |
| --- | --- | --- |
| 1 | **Purvika Patil** | `RBT24CB052` |
| 2 | **Aarya Kashikar** | `RBT24CB053` |
| 3 | **Karan Rathod** | `RBT24CB061` |

---

## 📌 Project Overview

The Placement Management System automates end-to-end placement workflows:
- **Students** register, log in, view/edit their profile (incl. CGPA), browse eligible companies/drives, apply once per company, and track application status and placement results.
- **Admins** (one-time secure setup) manage students, add/edit/delete companies, view and update application statuses (APPLIED → SHORTLISTED → INTERVIEW → SELECTED → REJECTED), and maintain placement results with statistics.
- Shared dashboard with live counts of students, companies, applications, and selected candidates.

---

## 🛠️ Technology Stack

- **Language:** Java 17
- **Web:** Jakarta EE 10 (Jakarta Servlet 6.0, JSP 3.1), JSTL
- **Persistence:** JDBC + MySQL Connector/J 8.0.33, MySQL 8.x
- **Build:** Apache Maven (WAR packaging)
- **Application Server:** Eclipse GlassFish 7.1.0
- **Frontend:** HTML5, CSS, Bootstrap 5.3, Bootstrap Icons
- **IDE:** Apache NetBeans
- **Testing:** JUnit 5 (Jupiter)

**Security:** Passwords hashed with PBKDF2WithHmacSHA256 (random salt, 210,000 iterations); session-based auth with HttpOnly cookies, session-fixation protection, CSRF tokens on state-changing POSTs; role-based authorization; no credentials hardcoded (read from environment variables).

---

## 🗄️ Database Architecture

Database: `placement_db` (schema already exists; this project does **not** create or reset it).

| Table | Key Columns |
| --- | --- |
| `users` | `user_id` (PK), `email` (UNIQUE), `password_hash`, `role` (STUDENT/ADMIN), `created_at` |
| `students` | `student_id` (PK), `user_id` (FK→users), `roll_number` (UNIQUE), `full_name`, `branch`, `cgpa`, `graduation_year`, `skills`, `phone` |
| `companies` | `company_id` (PK), `company_name`, `job_role`, `package_lpa`, `minimum_cgpa`, `eligible_branches`, `application_deadline`, `description`, `created_at` |
| `applications` | `application_id` (PK), `student_id` (FK), `company_id` (FK), `application_date`, `status` |
| `placement_results` | `result_id` (PK), `application_id` (FK), `interview_date`, `final_status`, `remarks` |

No `target/`, build artifacts, or database files are committed.

---

## 🚀 Prerequisites

- **JDK 17** (Oracle/Temurin) — set `JAVA_HOME`
- **Apache Maven 3.8+** (bundled with NetBeans)
- **Apache NetBeans IDE** (17+ recommended)
- **Eclipse GlassFish 7.1.0**
- **MySQL Server 8.x**
- Git (for version control)

Verify:

```bash
java -version
mvn -version
```

---

## 🔐 Database Configuration (Environment Variables — No Hardcoded Credentials)

Database credentials are **never** committed. Set these environment variables before starting GlassFish so the application can connect to `placement_db`:

| Variable | Purpose |
| --- | --- |
| `PMS_DB_USER` | MySQL username |
| `PMS_DB_PASSWORD` | MySQL password |
| `PMS_ADMIN_SETUP_TOKEN` | One-time token required to create the first admin account |

### On Windows (PowerShell, current session)

```powershell
$env:PMS_DB_USER="your_mysql_user"
$env:PMS_DB_PASSWORD="your_mysql_password"
$env:PMS_ADMIN_SETUP_TOKEN="choose-a-strong-random-token"
```

### To persist across reboots (Windows)

```powershell
setx PMS_DB_USER "your_mysql_user"
setx PMS_DB_PASSWORD "your_mysql_password"
setx PMS_ADMIN_SETUP_TOKEN "choose-a-strong-random-token"
```

> After using `setx`, restart NetBeans / the terminal so the new variables take effect.

GlassFish inherits these variables from the process that starts it. If you start GlassFish from NetBeans, NetBeans must be launched from a shell where the variables are set.

### One-Time Admin Setup

1. Start GlassFish with `PMS_ADMIN_SETUP_TOKEN` set.
2. Open `http://localhost:8080/PlacementManagementSystem/admin-setup`
3. Enter admin email, password (≥8 chars), and the setup token.
4. After the first admin exists, `/admin-setup` returns **404** and cannot create additional admins.
5. Students self-register at `/student-registration.jsp`.

---

## 💻 Running in NetBeans

1. **File → Open Project…** → select the folder `Placement Management system RBT24CB052,RBT24CB053,RBT24CB061` (contains `pom.xml`).
2. NetBeans detects it as a Maven Web Application. Wait for dependency resolution.
3. **Tools → Java Platforms** → ensure **JDK 17** is active for the project.
4. **Tools → Servers → Add Server → GlassFish Server** → point to your GlassFish 7.1.0 install (default `localhost:8080 / 4848`).
5. Right-click project → **Properties → Run** → Server = GlassFish 7.1.0, Context Path = `/PlacementManagementSystem`.
6. Right-click project → **Clean and Build**, then **Run** (or **Deploy**).
7. Open `http://localhost:8080/PlacementManagementSystem/`

### Build (command line)

From the project folder (the one containing `pom.xml`):

```bash
mvn clean package
```

Output: `target\PlacementManagementSystem.war`

### Deploy to GlassFish

**From NetBeans:** Right-click project → **Run** (builds WAR and deploys).

**GlassFish Admin Console:** `http://localhost:4848/` → **Applications → Deploy…** → upload `target\PlacementManagementSystem.war` → Context Root `PlacementManagementSystem`.

**asadmin CLI:**

```bash
<glassfish7>\bin\asadmin start-domain domain1
<glassfish7>\bin\asadmin deploy --contextroot PlacementManagementSystem target\PlacementManagementSystem.war
```

---

## 🔗 Application URLs

| Page | URL |
| --- | --- |
| Home (live stats) | `http://localhost:8080/PlacementManagementSystem/` |
| Student Registration | `…/student-registration.jsp` |
| Student Login | `…/student-login.jsp` |
| Student Dashboard | `…/student/dashboard` |
| Student Profile (edit CGPA) | `…/student/profile` |
| Admin Login | `…/admin-login.jsp` |
| One-Time Admin Setup | `…/admin-setup` |
| Admin Dashboard | `…/admin/dashboard` |
| Admin Students (edit CGPA) | `…/admin/students` |
| Admin Companies | `…/admin/companies` |
| Admin Applications | `…/admin/applications` |
| Admin Results | `…/admin/results` |
| Database Test | `…/test-db` |
| Logout | `…/logout` |

---

## 🧪 Running Tests

```bash
mvn test
```

Existing tests cover password hashing (`PasswordUtilTest`) and CGPA/eligibility rules (`EligibilityTest`).

---

## 📋 Notes

- Generated `target/` output, IDE caches (`.idea/`, `.nb-cache/`, `.settings/`), `*.war`, `*.class`, `*.jar`, and logs are excluded from version control.
- No database credentials, API keys, or tokens are stored in the repository.
- The existing `placement_db` schema and records are **not** modified, reset, or reseeded by this project.

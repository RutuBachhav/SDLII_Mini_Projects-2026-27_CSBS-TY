# CityCare Hospital — Patient & Doctor Portal

**Complete Technical Documentation**

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Database Schema](#database-schema)
6. [Entity Models](#entity-models)
7. [Data Access Layer](#data-access-layer)
8. [Authentication & Security](#authentication--security)
9. [Web Layer (Servlets & JSPs)](#web-layer-servlets--jsps)
10. [User Flows](#user-flows)
11. [Frontend Design System](#frontend-design-system)
12. [Deployment](#deployment)
13. [Configuration](#configuration)
14. [Known Limitations & Future Work](#known-limitations--future-work)

---

## Project Overview

**CityCare Hospital** is a Java EE web application for outpatient appointment management. It provides two distinct portals:

- **Patient Portal**: Register, browse doctors, book appointments, track visit history
- **Doctor Portal**: Review/accept/decline requests, manage schedule, save clinical notes, configure availability

### Key Features

| Feature | Patient | Doctor |
|---------|---------|--------|
| Account registration | ✅ Self-serve | ❌ Hospital-issued only |
| Browse/search doctors | ✅ By department & name | — |
| Book appointments | ✅ Slot-based, conflict-free | — |
| View upcoming visits | ✅ | ✅ Dashboard + full schedule |
| View history | ✅ | ✅ |
| Cancel appointments | ✅ (own upcoming) | ✅ (own schedule) |
| Accept/Decline requests | — | ✅ |
| Complete visits + notes | — | ✅ |
| Edit profile | ✅ Contact, DOB, gender, address | ✅ Professional + availability |
| Configure working hours | — | ✅ Start/end, slot length (15/20/30/45/60 min) |
| Pause bookings | — | ✅ Active toggle |

### Live Deployment

- **Repository**: https://github.com/ombisen10/hospital-management-system
- **Render (Docker)**: Auto-deploys from `render.yaml` + `Dockerfile`
- **URL pattern**: `https://<service>.onrender.com/hospital/`
- **Free tier**: Sleeps after inactivity (~1 min cold start), database resets on redeploy

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 11 |
| Build | Maven | 3.9+ |
| Packaging | WAR | — |
| Servlet Container | Jetty (dev) / Tomcat 9 (prod) | 9.4.54 / 9.0 |
| Web Framework | Servlets 4.0 + JSP 2.3 | Java EE 8 |
| ORM | JPA 2.2 (EclipseLink) | 2.7.12 |
| Database (dev) | H2 (in-memory) | 1.4.200 |
| Database (prod option) | H2 (file) / MySQL | 8.3.0 |
| Password Hashing | PBKDF2-HMAC-SHA256 (JDK built-in) | — |
| Frontend | Vanilla CSS + ES6 JS (no framework) | — |
| Fonts | Crimson Pro (display) + Inter (UI) | Google Fonts |

### Dependency Tree (pom.xml)

```xml
<!-- Java EE Web API (Servlet, JSP, EL) -->
javax:javaee-web-api:8.0.1 (provided)

<!-- JPA API -->
javax.persistence:javax.persistence-api:2.2

<!-- EclipseLink Implementation -->
org.eclipse.persistence:org.eclipse.persistence.jpa:2.7.12

<!-- H2 Database -->
com.h2database:h2:1.4.200

<!-- Optional MySQL -->
com.mysql:mysql-connector-j:8.3.0
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (HTML/CSS/JS)                    │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Tomcat / Jetty (Servlet Container)         │
│  ┌──────────────────┐  ┌────────────────────────────────────┐  │
│  │  AuthFilter      │  │  SessionUtil (CSRF, login, roles)  │  │
│  │  (/patient/*,    │  └────────────────────────────────────┘  │
│  │   /doctor/*)     │                                          │
│  └────────┬─────────┘                                          │
└───────────│────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Servlet Controllers                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Auth:       │ │ Patient:    │ │ Doctor:     │ │ Legacy:   │ │
│  │ Login,      │ │ Dashboard,  │ │ Dashboard,  │ │ Assign    │ │
│  │ Register,   │ │ Profile,    │ │ Appointments│ │ Servlet   │ │
│  │ Logout      │ │ Doctors,    │ │ Profile,    │ │           │ │
│  │             │ │ Book,       │ │ Action      │ │           │ │
│  │             │ │ Appointments│ │             │ │           │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └───────────┘ │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      HospitalDAO (Data Access)                  │
│  • EntityManagerFactory (singleton)                             │
│  • Transactional methods (synchronized where needed)            │
│  • Seeding logic (doctors + logins)                             │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      JPA Entities + H2 Database                 │
│  AppUser ↔ Patient (1:1)    AppUser ↔ Doctor (1:1)              │
│  Patient → Doctor (N:1, legacy walk-in)                         │
│  Appointment → Patient (N:1), Appointment → Doctor (N:1)        │
└─────────────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Defence in depth**: AuthFilter gates URLs, but every servlet re-verifies ownership on data operations
2. **Server-authoritative roles**: `role` field never read from client; public registration always creates `PATIENT`
3. **Transactional integrity**: Booking & state transitions are `synchronized` + JTA transactions with overlap checks
4. **No client trust**: All input validated server-side; all output HTML-escaped
5. **Progressive enhancement**: JavaScript only adds UX (confirm dialogs, busy states, focus management); all flows work without JS

---

## Project Structure

```
hospital/
├── pom.xml                          # Maven build configuration
├── README.md                        # Project overview & quick start
├── render.yaml                      # Render.com deployment config
├── Dockerfile                       # Multi-stage Docker build
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/
│   │   │       ├── module/          # Domain layer (entities, DAO, enums, utils)
│   │   │       │   ├── Doctor.java
│   │   │       │   ├── Patient.java
│   │   │       │   ├── AppUser.java
│   │   │       │   ├── Appointment.java
│   │   │       │   ├── AppointmentStatus.java
│   │   │       │   ├── Role.java
│   │   │       │   ├── HospitalDAO.java
│   │   │       │   ├── PasswordUtil.java
│   │   │       │   └── ValidationUtil.java
│   │   │       └── web/             # Presentation layer (servlets, filters, utils)
│   │   │           ├── AuthFilter.java
│   │   │           ├── SessionUtil.java
│   │   │           ├── LoginServlet.java
│   │   │           ├── RegisterServlet.java
│   │   │           ├── LogoutServlet.java
│   │   │           ├── patient/
│   │   │           │   ├── PatientDashboardServlet.java
│   │   │           │   ├── PatientProfileServlet.java
│   │   │           │   ├── DoctorDirectoryServlet.java
│   │   │           │   ├── DoctorDetailServlet.java
│   │   │           │   ├── BookAppointmentServlet.java
│   │   │           │   ├── PatientAppointmentsServlet.java
│   │   │           │   └── CancelAppointmentServlet.java
│   │   │           └── doctor/
│   │   │               ├── DoctorDashboardServlet.java
│   │   │               ├── DoctorAppointmentsServlet.java
│   │   │               ├── DoctorProfileServlet.java
│   │   │               └── DoctorAppointmentActionServlet.java
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml  # JPA configuration
│   │   └── webapp/
│   │       ├── index.jsp            # Landing page
│   │       ├── assets/
│   │       │   ├── styles.css       # Design system (CSS custom properties)
│   │       │   └── app.js           # Progressive enhancement JS
│   │       └── WEB-INF/
│   │           ├── web.xml          # Servlet 3.1 descriptor (minimal)
│   │           └── views/
│   │               ├── _head.jsp    # Shared header + nav + notices
│   │               ├── _foot.jsp    # Shared footer + confirm dialog
│   │               ├── login.jsp
│   │               ├── register.jsp
│   │               ├── error.jsp
│   │               ├── patient/
│   │               │   ├── dashboard.jsp
│   │               │   ├── profile.jsp
│   │               │   ├── doctors.jsp
│   │               │   ├── doctor-detail.jsp
│   │               │   └── appointments.jsp
│   │               └── doctor/
│   │                   ├── dashboard.jsp
│   │                   ├── appointments.jsp
│   │                   └── profile.jsp
│   └── test/                        # (empty - no automated tests yet)
├── target/                          # Maven build output (ignored in VCS)
│   ├── hospital-1.0-SNAPSHOT.war
│   └── classes/                     # Compiled classes
└── nb-configuration.xml             # NetBeans IDE config
```

---

## Database Schema

### Tables (generated via `drop-and-create` on startup)

```sql
-- Login accounts (patients + doctors)
CREATE TABLE app_users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    password_salt VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,          -- 'PATIENT' or 'DOCTOR'
    patient_id BIGINT UNIQUE,           -- FK to patients (nullable)
    doctor_id BIGINT UNIQUE,            -- FK to doctors (nullable)
    created_at TIMESTAMP
);

-- Doctors (hospital staff)
CREATE TABLE doctors (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    doctor_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(32),
    qualification VARCHAR(255),
    bio VARCHAR(2000),
    consultation_fee INTEGER,
    work_start VARCHAR(8) DEFAULT '09:00',   -- HH:mm
    work_end VARCHAR(8) DEFAULT '17:00',     -- HH:mm
    slot_minutes INTEGER DEFAULT 30,
    active BOOLEAN DEFAULT TRUE
);

-- Patients (portal registrants + walk-ins)
CREATE TABLE patients (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    doctor_id BIGINT,                    -- nullable: legacy walk-in assignment
    email VARCHAR(255),
    phone VARCHAR(32),
    date_of_birth DATE,
    gender VARCHAR(16),
    address VARCHAR(500),
    created_at TIMESTAMP
);

-- Appointments
CREATE TABLE appointments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    patient_id BIGINT NOT NULL,          -- FK to patients
    doctor_id BIGINT NOT NULL,           -- FK to doctors
    appt_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',  -- PENDING|CONFIRMED|REJECTED|CANCELLED|COMPLETED
    reason VARCHAR(500),
    notes VARCHAR(2000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Key Schema Notes

- **`app_users.email`** has a unique constraint — prevents duplicate accounts
- **`patients.doctor_id`** is nullable so portal-registered patients exist without a walk-in doctor assignment
- **`appointments.status`** uses string enum values matching `AppointmentStatus` enum
- **Schema generation**: `drop-and-create` on every start (dev); change to `none` + migrations for production

---

## Entity Models

### `Doctor` (`com.hospital.module.Doctor`)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | @Id, Generated | Primary key |
| name | String | @Column(nullable=false) | Display name |
| department | String | @Column(nullable=false) | Specialty/department |
| email | String | @Column(unique=true) | Login email |
| phone | String | — | Contact phone |
| qualification | String | — | e.g., "MD Cardiology" |
| bio | String | VARCHAR(2000) | Professional summary |
| consultationFee | Integer | — | Fee in ₹ (null = not specified) |
| workStart | String | DEFAULT '09:00' | Day start HH:mm |
| workEnd | String | DEFAULT '17:00' | Day end HH:mm (exclusive) |
| slotMinutes | Integer | DEFAULT 30 | Slot length: 15/20/30/45/60 |
| active | Boolean | DEFAULT true | Accepting bookings? |
| patients | List<Patient> | @OneToMany(mappedBy="assignedDoctor") | Legacy walk-in patients |

### `Patient` (`com.hospital.module.Patient`)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | @Id, Generated | Primary key |
| name | String | @Column(nullable=false) | Full name |
| assignedDoctor | Doctor | @ManyToOne(nullable=true) | Legacy walk-in doctor |
| email | String | — | Contact email |
| phone | String | — | Contact phone |
| dateOfBirth | LocalDate | — | DOB |
| gender | String | — | Female/Male/Other/Prefer not to say |
| address | String | VARCHAR(500) | Address |
| createdAt | LocalDateTime | — | Registration timestamp |

### `AppUser` (`com.hospital.module.AppUser`)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | @Id, Generated | Primary key |
| fullName | String | @Column(nullable=false) | Display name |
| email | String | @Column(nullable=false, unique) | Login email (unique) |
| passwordHash | String | @Column(nullable=false) | PBKDF2 hex |
| passwordSalt | String | @Column(nullable=false) | Random salt hex |
| role | Role | @Enumerated(STRING) | PATIENT or DOCTOR |
| patient | Patient | @OneToOne(unique) | Linked patient (if PATIENT) |
| doctor | Doctor | @OneToOne(unique) | Linked doctor (if DOCTOR) |
| createdAt | LocalDateTime | — | Account creation |

**Critical**: Role is **never** accepted from client input. Public registration hardcodes `Role.PATIENT`. Doctor accounts are seeded/created by admin only.

### `Appointment` (`com.hospital.module.Appointment`)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | @Id, Generated | Primary key |
| patient | Patient | @ManyToOne(optional=false) | Patient |
| doctor | Doctor | @ManyToOne(optional=false) | Doctor |
| date | LocalDate | @Column(nullable=false) | Appointment date |
| startTime | LocalTime | @Column(nullable=false) | Slot start |
| endTime | LocalTime | @Column(nullable=false) | Slot end |
| status | AppointmentStatus | @Enumerated(STRING) | Lifecycle state |
| reason | String | VARCHAR(500) | Patient-provided reason |
| notes | String | VARCHAR(2000) | Doctor clinical notes |
| createdAt | LocalDateTime | — | Booking timestamp |
| updatedAt | LocalDateTime | — | Last modification |

### `AppointmentStatus` (`com.hospital.module.AppointmentStatus`)

```java
public enum AppointmentStatus {
    PENDING,      // Patient requested, awaiting doctor decision
    CONFIRMED,    // Doctor accepted
    REJECTED,     // Doctor declined
    CANCELLED,    // Cancelled by patient or doctor
    COMPLETED;    // Visit finished, notes saved

    public boolean blocksSlot() {      // Active bookings block the slot
        return this == PENDING || this == CONFIRMED;
    }
    public boolean isTerminal() {      // No further transitions allowed
        return this == REJECTED || this == CANCELLED || this == COMPLETED;
    }
}
```

### `Role` (`com.hospital.module.Role`)

```java
public enum Role {
    PATIENT,
    DOCTOR
}
```

---

## Data Access Layer

### `HospitalDAO` (`com.hospital.module.HospitalDAO`)

Singleton-style DAO with `EntityManagerFactory` built once at class load. All methods create/close their own `EntityManager`.

#### Configuration & Initialization

```java
private static final EntityManagerFactory emf = buildEmf();

private static EntityManagerFactory buildEmf() {
    Map<String, String> props = new HashMap<>();
    String url = System.getenv("JDBC_URL");
    if (url != null && !url.trim().isEmpty()) {
        props.put("javax.persistence.jdbc.url", url.trim());
        props.put("javax.persistence.jdbc.user", envOr("JDBC_USER", "sa"));
        props.put("javax.persistence.jdbc.password", envOr("JDBC_PASSWORD", ""));
        String driver = System.getenv("JDBC_DRIVER");
        if (driver != null && !driver.trim().isEmpty()) {
            props.put("javax.persistence.jdbc.driver", driver.trim());
        }
    }
    return Persistence.createEntityManagerFactory("HospitalPU", props);
}
```

**Environment variable override** (for hosted deployments):
- `JDBC_URL` — e.g., `jdbc:h2:file:/tmp/hospitaldb;DB_CLOSE_DELAY=-1` or MySQL URL
- `JDBC_USER` — defaults to `sa`
- `JDBC_PASSWORD` — defaults to empty
- `JDBC_DRIVER` — defaults to H2 driver

#### Seeding (static initializer)

```java
static {
    seedDoctorsIfEmpty();
}

private static synchronized void seedDoctorsIfEmpty() {
    // Creates 4 doctors if table empty
    // Then ensures each has an AppUser with password "doc123"
}
```

**Seeded doctors** (all password `doc123`):
| Name | Department | Email |
|------|------------|-------|
| Dr. Sarah Rao | Cardiology | sarah@hospital.local |
| Dr. Rajesh Sharma | General Medicine | rajesh@hospital.local |
| Dr. Anita Desai | Orthopedics | anita@hospital.local |
| Dr. Kevin Miller | Pediatrics | kevin@hospital.local |

#### Core Query Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getAllDoctors()` | `List<Doctor>` | All doctors (admin/legacy) |
| `getActiveDoctors()` | `List<Doctor>` | Active only, ordered by dept+name |
| `findDoctorById(Long)` | `Doctor` | Single doctor lookup |
| `getAllPatients()` | `List<Patient>` | All patients with doctor fetch |
| `savePatient(Patient)` | `void` | Persist new patient |
| `findUserByEmail(String)` | `AppUser` | Login lookup (case-insensitive) |
| `findUserById(Long)` | `AppUser` | Session rehydration |
| `emailTaken(String)` | `boolean` | Registration check |
| `registerPatient(name, email, password)` | `AppUser` | Creates Patient + AppUser (PATIENT) |
| `findPatientById(Long)` | `Patient` | Patient lookup |
| `updatePatient(Patient)` | `void` | Merge detached patient |
| `updateDoctor(Doctor)` | `void` | Merge detached doctor |
| `updateUserName(Long, String)` | `void` | Sync display name |

#### Appointment Methods

| Method | Return | Description |
|--------|--------|-------------|
| `bookAppointment(patientId, doctorId, date, start, reason)` | `Appointment` | **Synchronized** — transactional overlap check |
| `findAppointmentById(Long)` | `Appointment` | With doctor+patient fetch |
| `findAppointmentsByPatient(Long)` | `List<Appointment>` | All for patient |
| `findAppointmentsByDoctor(Long)` | `List<Appointment>` | All for doctor |
| `findUpcomingByDoctor(Long, limit)` | `List<Appointment>` | PENDING+CONFIRMED, date≥today |
| `findUpcomingByPatient(Long, limit)` | `List<Appointment>` | PENDING+CONFIRMED, date≥today |
| `doctorCounts(Long)` | `Map<String,Long>` | PENDING, CONFIRMED, TODAY, COMPLETED |
| `transitionAppointment(id, target, actorRole, actorPatientId, actorDoctorId, notes)` | `Appointment` | **Synchronized** — state machine + ownership |
| `saveDoctorNote(id, doctorId, notes)` | `void` | Notes update with ownership check |
| `availableSlots(doctorId, date)` | `List<LocalTime>` | Grid minus taken slots |

#### Booking Logic (`bookAppointment`)

```java
public static synchronized Appointment bookAppointment(...) {
    // 1. Validate date range (today .. +30 days)
    // 2. Load doctor + patient
    // 3. Verify doctor.active == true
    // 4. Compute end = start + slotMinutes
    // 5. Verify within workStart/workEnd
    // 6. Verify slot alignment to grid from workStart
    // 7. If today, verify start > now
    // 8. Overlap query: COUNT where doctor+date+status∈{PENDING,CONFIRMED}
    //    AND startTime < newEnd AND endTime > newStart
    // 9. If clashes > 0 → throw IllegalStateException
    // 10. Persist Appointment(PENDING) with timestamps
}
```

#### State Machine (`transitionAppointment`)

```
PATIENT can:
  PENDING → CANCELLED
  CONFIRMED → CANCELLED

DOCTOR can:
  PENDING → CONFIRMED (accept)
  PENDING → REJECTED (decline)
  PENDING → CANCELLED
  CONFIRMED → COMPLETED (finish + optional note)
  CONFIRMED → CANCELLED
```

Enforced server-side with ownership checks:
- Patient: `actorPatientId == appointment.patient.id`
- Doctor: `actorDoctorId == appointment.doctor.id`

---

## Authentication & Security

### Password Hashing (`PasswordUtil`)

- **Algorithm**: PBKDF2-HMAC-SHA256
- **Iterations**: 65,536 (fixed)
- **Key length**: 256 bits
- **Salt**: 16 bytes (SecureRandom), hex-encoded
- **Storage**: `password_hash` + `password_salt` in `app_users`
- **Verification**: Constant-time `MessageDigest.isEqual()`

```java
public static Hash createHash(String password) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return new Hash(toHex(salt), toHex(derive(password, salt)));
}

public static boolean verify(String password, String saltHex, String expectedHashHex) {
    byte[] salt = fromHex(saltHex);
    byte[] expected = fromHex(expectedHashHex);
    byte[] actual = derive(password, salt);
    return MessageDigest.isEqual(actual, expected);
}
```

### Session Management (`SessionUtil`)

| Attribute | Type | Description |
|-----------|------|-------------|
| `uid` | Long | AppUser.id |
| `urole` | String | "PATIENT" or "DOCTOR" (from Role.name()) |
| `uname` | String | AppUser.fullName (display) |
| `csrf` | String | 24-byte hex token per session |

```java
public static void login(HttpServletRequest req, AppUser user) {
    HttpSession old = req.getSession(false);
    if (old != null) old.invalidate();
    HttpSession s = req.getSession(true);
    s.setAttribute("uid", user.getId());
    s.setAttribute("urole", user.getRole().name());
    s.setAttribute("uname", user.getFullName());
    s.setAttribute("csrf", token());
    s.setMaxInactiveInterval(30 * 60);  // 30 minutes
}
```

### CSRF Protection

- Token generated per session (`SessionUtil.token()` → 24 random bytes → hex)
- Included as hidden `_csrf` field in **every POST form**
- Validated in every POST servlet: `SessionUtil.validCsrf(req)`
- Returns 403 if missing/mismatched

### Authorization Filter (`AuthFilter`)

```java
@WebFilter(urlPatterns = {"/patient/*", "/doctor/*"})
public class AuthFilter implements Filter {
    // 1. Check session for uid + role
    // 2. Redirect to /login?next=originalPath if missing
    // 3. Verify role matches area (PATIENT for /patient/*, DOCTOR for /doctor/*)
    // 4. 403 + error.jsp if role mismatch
}
```

**Defence in depth**: Filter is the *outer gate*. Every servlet re-verifies ownership on the actual data (e.g., `HospitalDAO.transitionAppointment` checks `actorPatientId`/`actorDoctorId` against the appointment).

### Input Validation (`ValidationUtil`)

| Method | Rules |
|--------|-------|
| `isEmail` | RFC-like regex, ≤255 chars |
| `isName` | 2–120 chars after trim |
| `isPassword` | 8–128 chars, ≥1 letter + ≥1 digit |
| `isPhone` | Optional; if present: starts with `+` or digit, 5–20 chars of digits/spaces/()- |
| `parseDate` | ISO `yyyy-MM-dd` → `LocalDate` |
| `parseTime` | `HH:mm` 24h → `LocalTime` |
| `clean(String, max)` | Trim, null if empty, truncate to max |
| `esc(String)` | HTML escape: `& < > " '` → entities |

**All POST handlers** use `ValidationUtil` before any DAO call.

### Output Encoding

Every JSP uses `ValidationUtil.esc(...)` on dynamic values. No raw `${...}` or `<%= ... %>` without escaping.

---

## Web Layer (Servlets & JSPs)

### URL Mapping

| Pattern | Servlet | Methods | Auth |
|---------|---------|---------|------|
| `/` | (welcome-file → `index.jsp`) | GET | Public |
| `/login` | `LoginServlet` | GET, POST | Public |
| `/register` | `RegisterServlet` | GET, POST | Public |
| `/logout` | `LogoutServlet` | GET, POST | Any |
| `/patient/dashboard` | `PatientDashboardServlet` | GET | PATIENT |
| `/patient/profile` | `PatientProfileServlet` | GET, POST | PATIENT |
| `/patient/doctors` | `DoctorDirectoryServlet` | GET | PATIENT |
| `/patient/doctor` | `DoctorDetailServlet` | GET | PATIENT |
| `/patient/book` | `BookAppointmentServlet` | POST | PATIENT |
| `/patient/appointments` | `PatientAppointmentsServlet` | GET | PATIENT |
| `/patient/cancel` | `CancelAppointmentServlet` | POST | PATIENT |
| `/doctor/dashboard` | `DoctorDashboardServlet` | GET | DOCTOR |
| `/doctor/appointments` | `DoctorAppointmentsServlet` | GET | DOCTOR |
| `/doctor/appointments/action` | `DoctorAppointmentActionServlet` | POST | DOCTOR |
| `/doctor/profile` | `DoctorProfileServlet` | GET, POST | DOCTOR |
| `/AssignServlet` | `AssignServlet` (legacy) | POST | Public |

### Auth Servlets

#### `LoginServlet` (`/login`)

- **GET**: Shows form; if already logged in → redirect to role home
- **POST**: 
  - `HospitalDAO.findUserByEmail(email)`
  - `PasswordUtil.verify(password, salt, hash)`
  - Generic error: "Incorrect email or password" (no field leakage)
  - On success: `SessionUtil.login()`, redirect to `next` param (validated) or role home
  - `safeNext()` only allows redirects into same role area

#### `RegisterServlet` (`/register`)

- **GET**: Shows form; redirects logged-in users to role home
- **POST**:
  - Validates name, email, password (8+ chars, letter+digit), confirm
  - `HospitalDAO.emailTaken()` check
  - `HospitalDAO.registerPatient()` → creates Patient + AppUser(PATIENT)
  - Auto-login via `SessionUtil.login()`
  - Redirect to `/patient/dashboard`

#### `LogoutServlet` (`/logout`)

- Invalidates session, redirects to `/login?notice=signed-out`

### Patient Servlets

#### `PatientDashboardServlet` (`/patient/dashboard`)

- Loads `AppUser` → `Patient` → upcoming (limit 5) + total count
- Greeting based on hour (morning/afternoon/evening)
- Quick actions: "Find a doctor", "All appointments"

#### `PatientProfileServlet` (`/patient/profile`)

- **GET**: Shows read-only account email + patient ID + editable form
- **POST**: CSRF check → validates name, phone, DOB (past, ≥130 years), gender, address
- Updates `Patient` + `AppUser.fullName` + session `uname`

#### `DoctorDirectoryServlet` (`/patient/doctors`)

- **GET**: Query params `specialty` (department) + `q` (name search)
- Loads all active doctors → filters in memory (small dataset)
- Extracts distinct departments for dropdown
- Renders table with: Name, Department, Qualification, Fee, Hours, "View & book" link

#### `DoctorDetailServlet` (`/patient/doctor?id=...`)

- **GET**: Loads doctor (404 if null/inactive)
- Date param (defaults to today, clamped to booking window)
- Generates next 14 days for dropdown
- `HospitalDAO.availableSlots(doctorId, date)` → grid of radio buttons
- Form POSTs to `/patient/book` with doctorId, date, time, reason

#### `BookAppointmentServlet` (`/patient/book`)

- **POST**: CSRF → loads patient → `HospitalDAO.bookAppointment()`
- On success: redirect to `/patient/appointments?notice=booked`
- On failure: redirect back to doctor-detail with `error=message`

#### `PatientAppointmentsServlet` (`/patient/appointments`)

- **GET**: Splits all appointments into:
  - `upcoming`: PENDING + CONFIRMED (sorted soonest first)
  - `history`: REJECTED, CANCELLED, COMPLETED (sorted newest first)
- Notice params: `booked`, `cancelled`

#### `CancelAppointmentServlet` (`/patient/cancel`)

- **POST**: CSRF → `HospitalDAO.transitionAppointment(id, CANCELLED, PATIENT, patientId, null, null)`
- Redirect with `notice=cancelled` or `error=message`

### Doctor Servlets

#### `DoctorDashboardServlet` (`/doctor/dashboard`)

- Loads doctor → `HospitalDAO.doctorCounts()` → PENDING, CONFIRMED, TODAY, COMPLETED
- `findUpcomingByDoctor(doctorId, 8)` → today subset + full upcoming
- Quick actions: "Review requests" (filter=pending), "Edit profile & hours"

#### `DoctorAppointmentsServlet` (`/doctor/appointments?status=...`)

- **GET**: Filter param: `upcoming` (default), `pending`, `past`
- Partitions all appointments into three lists
- Renders tabs with counts
- **PENDING rows**: Accept / Decline buttons (POST to action servlet)
- **CONFIRMED rows**: Mark complete / Cancel buttons
- **All active rows**: Inline "Save note" form (POST action=note)

#### `DoctorAppointmentActionServlet` (`/doctor/appointments/action`)

- **POST**: CSRF → loads doctor → parses `action` param:
  - `accept` → `transitionAppointment(CONFIRMED, DOCTOR, ..., doctorId, notes)`
  - `reject` → `transitionAppointment(REJECTED, DOCTOR, ..., doctorId, notes)`
  - `complete` → `transitionAppointment(COMPLETED, DOCTOR, ..., doctorId, notes)`
  - `cancel` → `transitionAppointment(CANCELLED, DOCTOR, ..., doctorId, notes)`
  - `note` → `saveDoctorNote(id, doctorId, notes)` (only for PENDING/CONFIRMED)
- Redirects back to `/doctor/appointments?notice=updated` or `error=message`

#### `DoctorProfileServlet` (`/doctor/profile`)

- **GET**: Shows form with current values
- **POST**: CSRF → validates phone, fee (0–100000), workStart < workEnd, slotMinutes ∈ {15,20,30,45,60}
- Updates Doctor entity + `active` flag
- Redirect with `notice=saved`

### Legacy Servlet

#### `AssignServlet` (`/AssignServlet`)

- **POST**: `patientName` + optional `doctorId` (or "auto" for random)
- Creates `Patient(name, assignedDoctor)` via `HospitalDAO.savePatient()`
- Redirects to `index.jsp`
- Preserved for backward compatibility with walk-in workflows

---

## User Flows

### Patient Registration & First Booking

```
1. GET /register
   → register.jsp (form)
2. POST /register (name, email, password, confirm)
   → ValidationUtil checks
   → HospitalDAO.registerPatient() → Patient + AppUser(PATIENT)
   → SessionUtil.login() → session(uid, urole=PATIENT, uname, csrf)
   → Redirect /patient/dashboard
3. GET /patient/dashboard
   → PatientDashboardServlet → dashboard.jsp
   → Clicks "Find a doctor"
4. GET /patient/doctors
   → DoctorDirectoryServlet → doctors.jsp
   → Filters by department, searches name
   → Clicks "View & book" on a doctor
5. GET /patient/doctor?id=X
   → DoctorDetailServlet → doctor-detail.jsp
   → Selects date (dropdown reloads page)
   → Sees slot grid (radio buttons)
   → Enters optional reason
   → Clicks "Request appointment"
6. POST /patient/book (doctorId, date, time, reason, _csrf)
   → BookAppointmentServlet
   → HospitalDAO.bookAppointment() [synchronized, overlap check]
   → Redirect /patient/appointments?notice=booked
7. GET /patient/appointments
   → PatientAppointmentsServlet → appointments.jsp
   → Sees new row: PENDING
```

### Doctor Review & Complete Visit

```
1. GET /login → login.jsp
2. POST /login (doctor email + "doc123")
   → LoginServlet → SessionUtil.login(DOCTOR)
   → Redirect /doctor/dashboard
3. GET /doctor/dashboard
   → DoctorDashboardServlet → dashboard.jsp
   → Sees stat strip: "Awaiting decision: 2"
   → Clicks "Review requests"
4. GET /doctor/appointments?status=pending
   → DoctorAppointmentsServlet → appointments.jsp (filter=pending)
   → Sees PENDING rows with Accept/Decline buttons
5. Clicks "Accept" on a row
   → POST /doctor/appointments/action (id, action=accept, _csrf)
   → DoctorAppointmentActionServlet
   → HospitalDAO.transitionAppointment(CONFIRMED, DOCTOR, doctorId)
   → Redirect /doctor/appointments?notice=updated
6. Later: Clicks "Mark complete"
   → POST /doctor/appointments/action (id, action=complete, notes="...")
   → HospitalDAO.transitionAppointment(COMPLETED, DOCTOR, doctorId, notes)
   → Redirect with notice
7. Patient sees COMPLETED in their /patient/appointments history with doctor's note
```

### Doctor Configures Availability

```
1. GET /doctor/profile
   → DoctorProfileServlet → profile.jsp
2. Edits:
   - Phone, Qualification, Bio
   - Consultation fee (number)
   - Work start (time input), Work end (time input)
   - Slot minutes (select: 15/20/30/45/60)
   - "Accepting new bookings" checkbox
3. POST /doctor/profile (_csrf + all fields)
   → DoctorProfileServlet validates
   → HospitalDAO.updateDoctor()
   → Redirect /doctor/profile?notice=saved
4. Subsequent patient slot grids reflect new hours/length immediately
```

---

## Frontend Design System

### Design Direction: "Warm Clinical"

> Medical journal precision meets patient-care warmth. Ruled tables, real hierarchy, one accent.

### CSS Custom Properties (tokens in `:root`)

```css
:root {
  /* TYPE — Major third scale (1.250) */
  --font-display: "Crimson Pro", Georgia, serif;
  --font-text:    "Inter", -apple-system, sans-serif;
  --step--1: 0.8rem;    --step-0: 1rem;
  --step-1: 1.25rem;    --step-2: 1.563rem;
  --step-3: 1.953rem;   --step-4: 2.441rem;

  /* SPACE — 4px base unit */
  --space-3xs: 0.25rem; --space-2xs: 0.5rem; --space-xs: 0.75rem;
  --space-s:   1rem;    --space-m:   1.5rem; --space-l: 2rem;
  --space-xl:  3rem;    --space-2xl: 4.5rem;

  /* COLOR — Warm clinical palette */
  --base:       #fdfcf8;   /* warm paper background */
  --surface:    #ffffff;   /* raised surfaces */
  --ink:        #1a2428;   /* deep slate text */
  --ink-2:      #475563;   /* mid slate */
  --ink-3:      #7a8d9d;   /* muted */
  --line:       #e8e4dc;   /* warm rules */
  --line-soft:  #f0ebe3;   /* hairlines */

  --accent:      #5a7d6a;   /* muted sage — primary actions */
  --accent-ink:  #3d5c48;   /* hover */
  --accent-soft: #eef5f0;   /* soft bg */

  --urgent:      #8a5200;   /* amber — pending (4.5:1 on surface) */
  --urgent-soft: #fff7e6;
  --danger:      #a82c2c;   /* red — cancelled, errors */
  --danger-soft: #fef2f2;
  --ok:          #2d7a3d;   /* green — confirmed, completed */
  --ok-soft:     #eef7ef;

  /* FORM — Zero radius, rules only */
  --radius:     0;
  --border:     1px solid var(--line);
  --border-soft: 1px solid var(--line-soft);

  /* MOTION */
  --dur-fast: 100ms; --dur: 200ms; --dur-slow: 350ms;
  --ease:     cubic-bezier(0.2, 0, 0, 1);
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);

  /* Z-index scale */
  --z-raised: 10; --z-sticky: 100; --z-overlay: 1000; --z-modal: 1100;
}
```

### Key Components

| Component | Classes | Notes |
|-----------|---------|-------|
| Layout | `.wrap` (max 1080px), `.page-head`, `.columns.two` | Responsive grid |
| Tables | `.table-scroll` + `table.ledger` | Sticky headers, hover highlight |
| Status badges | `.status-word--pending/confirmed/rejected/cancelled/completed` | Text-first, color as reinforcement |
| Forms | `.form`, `.field`, `.btn` / `.btn-secondary` / `.btn-danger` | 44px min-height, focus-visible |
| Notices | `.notice` + `.notice-success` / `.notice-error` | Left border accent, dismissible |
| Empty states | `.empty` | Illustration-free, action-oriented |
| Stat strip | `.stat-strip` | Rules not cards |
| Slot grid | `.slot-grid label` + hidden radio | Keyboard accessible, checked = inverted |
| Dialog | `<dialog class="confirm">` | Native `<dialog>`, backdrop, focus return |

### Accessibility Features

- **Skip link**: `<a class="skip-link" href="#main">Skip to content</a>`
- **Focus visible**: 2px accent outline on all interactive elements
- **ARIA**: `aria-live="polite"` status region, `role="alert"` for errors, `aria-invalid` on fields
- **Labels**: Every input has explicit `<label for="id">`
- **Reduced motion**: `@media (prefers-reduced-motion: reduce)` disables transitions
- **Touch targets**: Minimum 44×44px on all buttons/links
- **Color contrast**: All text ≥4.5:1; status colors meet WCAG AA on surface
- **No color alone**: Status words have text labels (PENDING, CONFIRMED, etc.)

### JavaScript (`assets/app.js`) — Progressive Enhancement Only

| Feature | Implementation |
|---------|----------------|
| Dismissible notices | Click `[data-dismiss]` → remove `.notice` |
| Submit busy state | `button[aria-busy="true"]` + announce to `#status-region` |
| Focus first invalid | On load, `document.querySelector("[aria-invalid='true']")?.focus()` |
| Confirm dialog | Native `<dialog>` replaces `window.confirm()`; triggers on `[data-confirm]`; returns focus to trigger on close |

**All flows work without JavaScript** — forms submit normally, full page reloads.

---

## Deployment

### Local Development

```bash
# Prerequisites: Java 11, Maven 3.9+
mvn compile          # Compile all sources
mvn jetty:run        # Start at http://localhost:8080/hospital
mvn package          # Build target/hospital-1.0-SNAPSHOT.war
```

- Jetty config in `pom.xml`: context `/hospital`, port 8080, scan interval 10s
- H2 in-memory: `jdbc:h2:mem:hospitaldb` (data resets on restart)

### Docker (Production)

**Dockerfile** (multi-stage):
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage
FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/hospital-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/hospital.war
EXPOSE 8080
# Bind to Render's $PORT
CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"'${PORT:-8080}'\"/' /usr/local/tomcat/conf/server.xml && catalina.sh run"]
```

### Render.com Deployment

**render.yaml**:
```yaml
services:
  - type: web
    name: hospital-management-system
    runtime: docker
    plan: free
    healthCheckPath: /hospital/
    envVars:
      - key: JDBC_URL
        value: jdbc:h2:file:/tmp/hospitaldb;DB_CLOSE_DELAY=-1
```

**Steps**:
1. Push to GitHub
2. Render dashboard → New → Web Service → connect repo
3. Render auto-detects `render.yaml`
4. Deploy → live at `https://<service>.onrender.com/hospital/`

**Notes**:
- Free tier sleeps after inactivity (cold start ~1 minute)
- File-based H2 at `/tmp/hospitaldb` survives restarts but **not redeploys** (ephemeral filesystem)
- For persistent data: use managed PostgreSQL/MySQL + set `JDBC_URL`/`JDBC_USER`/`JDBC_PASSWORD`/`JDBC_DRIVER` env vars

### Environment Variables (Production)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JDBC_URL` | No | `jdbc:h2:mem:hospitaldb` | Database connection URL |
| `JDBC_USER` | No | `sa` | DB username |
| `JDBC_PASSWORD` | No | (empty) | DB password |
| `JDBC_DRIVER` | No | `org.h2.Driver` | JDBC driver class |

**To use MySQL**:
```yaml
envVars:
  - key: JDBC_URL
    value: jdbc:mysql://<host>:3306/hospital?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
  - key: JDBC_USER
    value: <username>
  - key: JDBC_PASSWORD
    value: <password>
  - key: JDBC_DRIVER
    value: com.mysql.cj.jdbc.Driver
```
Then change `persistence.xml`:
```xml
<property name="javax.persistence.schema-generation.database.action" value="none"/>
```
And run migrations manually (Flyway/Liquibase recommended).

---

## Configuration

### `persistence.xml` (`src/main/resources/META-INF/persistence.xml`)

```xml
<persistence-unit name="HospitalPU" transaction-type="RESOURCE_LOCAL">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    
    <class>com.hospital.module.Doctor</class>
    <class>com.hospital.module.Patient</class>
    <class>com.hospital.module.AppUser</class>
    <class>com.hospital.module.Appointment</class>

    <properties>
        <!-- Embedded H2 Database Configuration -->
        <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
        <property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:hospitaldb;DB_CLOSE_DELAY=-1"/>
        <property name="javax.persistence.jdbc.user" value="sa"/>
        <property name="javax.persistence.jdbc.password" value=""/>

        <!-- Auto-generate DDL schema -->
        <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
        <property name="eclipselink.logging.level" value="WARNING"/>
    </properties>
</persistence-unit>
```

**Production changes**:
- Change `schema-generation.database.action` to `none`
- Use external DB via environment variables (handled in `HospitalDAO.buildEmf()`)

### `web.xml` (`src/main/webapp/WEB-INF/web.xml`)

```xml
<web-app version="3.1">
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
</web-app>
```

- Servlets use `@WebServlet` annotations (Servlet 3.0+)
- Filter uses `@WebFilter`
- Minimal descriptor — only session timeout + welcome file

### Build Plugins (`pom.xml`)

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.3.2</version>
    </plugin>
    <plugin>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-maven-plugin</artifactId>
        <version>9.4.54.v20240208</version>
        <configuration>
            <scanIntervalSeconds>10</scanIntervalSeconds>
            <webApp>
                <contextPath>/hospital</contextPath>
            </webApp>
            <httpConnector>
                <port>8080</port>
            </httpConnector>
        </configuration>
    </plugin>
</plugins>
```

---

## Known Limitations & Future Work

### Current Limitations

| Area | Limitation | Mitigation / Plan |
|------|------------|-------------------|
| **Concurrency** | Single-JVM `synchronized` booking guard | For multi-instance: add DB-level exclusion (partial unique index on `doctor_id, appt_date, start_time, end_time` WHERE status IN ('PENDING','CONFIRMED')) |
| **Notifications** | No email/SMS for booking confirmations, reminders, cancellations | Add notification service (email/SMS provider) + async queue |
| **Password reset** | No "forgot password" flow | Implement token-based reset via email |
| **Admin UI** | No interface to create/manage doctor accounts | Build admin portal or CLI seeding script |
| **Testing** | No automated test suite | Add JUnit + Testcontainers for integration tests; Selenium/Cypress for E2E |
| **Persistence** | `drop-and-create` loses data on restart | Switch to `none` + Flyway/Liquibase migrations for production |
| **Audit trail** | No audit log for appointment changes | Add `AppointmentAudit` entity + trigger/listener |
| **Multi-tenancy** | Single hospital only | Add `hospital_id` to all entities + tenant filter |
| **File uploads** | No document/attachment support | Add multipart handling + object storage (S3) |

### Technical Debt

1. **In-memory filtering** in `DoctorDirectoryServlet` — fine for ~100 doctors, replace with JPQL `WHERE` for scale
2. **No connection pooling config** — relies on EclipseLink defaults; add HikariCP for production
3. **Hardcoded seed password** — `SEED_DOCTOR_PASSWORD = "doc123"` in `HospitalDAO`; change before real use
4. **No rate limiting** on auth endpoints — consider bucket4j or filter-based throttling
5. **CSP headers** not set — add `Content-Security-Policy` via filter

### Suggested Next Steps

1. **Add Flyway migrations** + switch `schema-generation` to `none`
2. **Implement email notifications** (Jakarta Mail + template engine)
3. **Add password reset flow** (token → email → form → hash update)
4. **Write integration tests** with Testcontainers (H2/MySQL)
5. **Add admin servlet** for doctor account management
6. **Configure HikariCP** in `persistence.xml` for connection pooling
7. **Add CSP + security headers** via `HttpServletResponse` wrapper filter

---

## Appendix: Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Doctor | sarah@hospital.local | doc123 |
| Doctor | rajesh@hospital.local | doc123 |
| Doctor | anita@hospital.local | doc123 |
| Doctor | kevin@hospital.local | doc123 |

> **⚠️ Change `HospitalDAO.SEED_DOCTOR_PASSWORD` and/or seed data before any real use.**

---

## Appendix: Quick Reference

### Common Commands

```bash
# Build WAR
mvn clean package

# Run locally (Jetty)
mvn jetty:run

# Run WAR in Tomcat
cp target/hospital-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/hospital.war
$CATALINA_HOME/bin/catalina.sh run

# Docker build & run
docker build -t hospital .
docker run -p 8080:8080 hospital

# With MySQL (Docker)
docker run -p 8080:8080 \
  -e JDBC_URL="jdbc:mysql://host.docker.internal:3306/hospital" \
  -e JDBC_USER="root" \
  -e JDBC_PASSWORD="secret" \
  -e JDBC_DRIVER="com.mysql.cj.jdbc.Driver" \
  hospital
```

### Key Files to Modify for Customization

| File | Purpose |
|------|---------|
| `src/main/resources/META-INF/persistence.xml` | DB connection, schema generation |
| `src/main/java/com/hospital/module/HospitalDAO.java` | Seeding, booking window (`MAX_BOOK_AHEAD_DAYS`), seed password |
| `src/main/webapp/assets/styles.css` | Design tokens (colors, type, spacing) |
| `src/main/webapp/WEB-INF/views/_head.jsp` | Global header/nav structure |
| `src/main/webapp/WEB-INF/views/_foot.jsp` | Footer, confirm dialog, JS include |

---

*Generated from source code analysis — CityCare Hospital v1.0-SNAPSHOT*
# CityCare Hospital — Patient & Doctor Portal

Live repo: https://github.com/ombisen10/hospital-management-system

Java 11 · Maven WAR · Servlets + JSP · JPA (EclipseLink) · H2 (in-memory) · Jetty 9.4.
No new runtime dependencies were added; password hashing uses JDK PBKDF2.

## Live deployment (Render)

1. Code is on GitHub with a `Dockerfile` and `render.yaml` ready.
2. Go to dashboard.render.com → **New → Web Service** → connect the
   `hospital-management-system` repo. Render auto-detects `render.yaml`.
3. Deploy with the defaults. The app will be live at
   `https://<service-name>.onrender.com/hospital/`.

Notes: the free tier sleeps after inactivity (first load takes ~1 minute),
and redeploys reset the demo data. The database URL is configurable via the
`JDBC_URL` / `JDBC_USER` / `JDBC_PASSWORD` / `JDBC_DRIVER` environment
variables; without them the app uses its built-in in-memory H2.

## Run

```bash
mvn compile          # compile everything
mvn jetty:run        # start at http://localhost:8080/hospital
mvn package          # build target/hospital-1.0-SNAPSHOT.war
```

No environment variables or external database needed. The H2 database is
in-memory (`jdbc:h2:mem:hospitaldb`), so data resets on every restart —
this is a development setup, not production storage.

## Demo accounts

Doctor accounts are issued by the hospital (seeded at startup).
Public registration only ever creates **patient** accounts.

| Role    | Email                        | Password    |
|---------|------------------------------|-------------|
| Doctor  | sarah@hospital.local     | doc123  |
| Doctor  | rajesh@hospital.local | doc123  |
| Doctor  | anita@hospital.local   | doc123  |
| Doctor  | kevin@hospital.local  | doc123  |

Change `HospitalDAO.SEED_DOCTOR_PASSWORD` / seed data before any real use.

## What patients can do

- Register, sign in, edit profile (`/register`, `/login`, `/patient/profile`)
- Browse/search doctors by department and name (`/patient/doctors`)
- View a doctor, pick a day, book an open slot (`/patient/doctor?id=…`)
- Track upcoming visits and history, cancel upcoming visits (`/patient/appointments`)

## What doctors can do

- Sign in (`/login`), see counts + today's list (`/doctor/dashboard`)
- Accept / decline / complete / cancel own appointments, save visit notes (`/doctor/appointments`)
- Edit professional profile and availability: hours, slot length (15/20/30/45/60),
  fee, accepting-bookings toggle (`/doctor/profile`)

## Security model

- Passwords: PBKDF2-HMAC-SHA256, random 16-byte salt per account (`PasswordUtil`).
- Roles come from the server session only; the `role` field is never read
  from client input. `AuthFilter` gates `/patient/*` and `/doctor/*`, and every
  data operation re-checks ownership (patient sees own appointments; doctor
  sees own schedule + patient name/phone/reason only).
- State machine enforced server-side:
  PENDING → CONFIRMED / REJECTED / CANCELLED;
  CONFIRMED → COMPLETED / CANCELLED. Patients may only cancel their own
  upcoming visits.
- Double-booking is blocked by a transactional overlap check
  (`HospitalDAO.bookAppointment`): same-doctor overlapping PENDING/CONFIRMED
  slots cannot be created, including under concurrent requests.
- All POSTs carry a per-session CSRF token; all user input is validated
  server-side and HTML-escaped on output.

## Database notes

- New tables: `app_users`, `appointments`. Extended: `doctors` (contact,
  qualification, bio, fee, working hours, slot length, active flag),
  `patients` (contact, dob, gender, address; `doctor_id` is now nullable so
  portal patients can exist without a walk-in assignment).
- Schema is generated (`drop-and-create`) on each start; existing rows do not
  survive restarts by design here. For MySQL/file persistence, change the
  JDBC properties in `src/main/resources/META-INF/persistence.xml` and switch
  `schema-generation.database.action` to `none` with managed migrations.
- The legacy `AssignServlet` (`POST /AssignServlet`) is unchanged and still
  stores walk-in assignments.

## Known limitations

- Single-JVM synchronisation guards concurrent booking; for multi-instance
  deployment add a DB-level exclusion (partial unique index) per database.
- No email/SMS notifications, no password reset, no admin UI for issuing
  doctor accounts (seeded instead).
- No automated test suite in the repo; flows were verified with live HTTP
  tests against Jetty (see manual test log in the project summary).

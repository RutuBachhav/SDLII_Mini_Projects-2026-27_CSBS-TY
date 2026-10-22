# DeadlineBuddy

A deadline tracker for students. Add your subjects, add the assignments,
practicals, submissions and exams that belong to them, and see at a glance what
is overdue, what is due today and what is coming up.

Built as a conventional **NetBeans Ant web application**: Java Servlets, JSP with
JSTL, JPA (EclipseLink) over MySQL, running on Eclipse GlassFish. No Maven, no
Spring, no ORM other than JPA, no front-end build step.

---

## 1. Setup

### 1.1 What you need

| Thing | Version used here | Notes |
|---|---|---|
| Apache NetBeans | 30 | Any version with "Java with Ant → Java Web" |
| JDK | Azul Zulu OpenJDK 25.0.2 (`D:\Apache NetBeans\jdk`) | compiled to bytecode 17, see §1.5 |
| Eclipse GlassFish | 7.1.1, domain1, HTTP 8080, admin 4848 | Jakarta EE 10 |
| MySQL | 8.0.44, service `MySQL80` | user `root` |
| MySQL Connector/J | 9.7.0, vendored at `lib\mysql-connector-j-9.7.0.jar` | nothing to install - see §7.2 |

### 1.2 Create the database

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\schema.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\seed.sql
```

`schema.sql` drops and recreates `deadlinebuddy_db`, so it doubles as a reset.
`seed.sql` is safe to re-run: it clears the three tables and restarts the ids
first. It prints the counts it created, which should match what the dashboard
then shows.

### 1.3 Tell the application how to reach MySQL

Everything lives in one file, `src/java/deadlinebuddy.properties`, which the
build copies to `WEB-INF/classes/deadlinebuddy.properties`:

```properties
jdbc.url=jdbc:mysql://localhost:3306/deadlinebuddy_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
jdbc.user=root
jdbc.password=123456789
```

Each key is resolved in this order, first hit wins:

1. a JVM system property — `-Djdbc.password=...`
2. an environment variable — `DEADLINEBUDDY_JDBC_PASSWORD=...`
3. this file
4. a default built into `com.util.Config`

So a different lab machine needs no source edit:

```bash
set DEADLINEBUDDY_JDBC_PASSWORD=whatever
```

No credential appears anywhere else in the source. `persistence.xml` carries
placeholder values only; `com.util.Jpa` overrides all four at startup.

### 1.4 Build, deploy, run

In NetBeans: **File → Open Project →** `DeadlineBuddy`, then **Properties → Run →**
pick your GlassFish server, then **F6**. Full click-by-click steps are in
[HOW-TO-RUN.md](HOW-TO-RUN.md).

From the command line:

```bash
ant clean dist
asadmin deploy --contextroot /DeadlineBuddy --name DeadlineBuddy dist\DeadlineBuddy.war
```

Then open <http://localhost:8080/DeadlineBuddy/> and sign in as
`ananya.pawar@student.edu` / `123456789`.

`asadmin` needs a JDK on `JAVA_HOME` or `AS_JAVA`, or it fails with "The java
command is not executable".

### 1.5 Version compatibility — read this one

**Namespace.** GlassFish 7.1.1 is Jakarta EE 10, so the application uses
`jakarta.servlet.*` and `jakarta.persistence.*` throughout. There is no
`javax.*` import anywhere. On a GlassFish 5 or earlier (Java EE 8) server this
code will not deploy, and the fix is not a find-and-replace — the Servlet 6 and
JPA 3 APIs differ from Servlet 4 and JPA 2.2.

**JDK.** GlassFish 7.1.1's supported JDKs are 11 through 21. JDK 25 is outside
that range. In this environment it nevertheless runs: the GlassFish process was
verified to be running on `D:\Apache NetBeans\jdk` (Zulu 25.0.2) and the
application deployed, served and persisted correctly on it. Treat that as
"works here", not "supported".

The project therefore compiles with `javac.source=17` and `javac.target=17`
rather than 25. That is the minimum necessary configuration change: the class
files load on any JDK from 17 up, the project opens on an older lab machine, and
nothing in the code needs a language feature newer than 17. If you hit a JDK 25
problem at runtime, point GlassFish at a JDK 17–21 by setting `AS_JAVA` in
`D:\glassfish7\glassfish\config\asenv.bat` — no application change is needed.

**JPA transactions.** The persistence unit is `RESOURCE_LOCAL`, not JTA. That is
a deliberate choice: there is **nothing to configure in GlassFish before
deploying** — no JDBC connection pool, no JNDI resource, no server-level driver
install, no restart. The driver travels inside the WAR at `WEB-INF/lib`, and
`com.util.Jpa` opens and commits the transactions itself. If you would rather
use a server-managed pool, §6.4 says exactly what to change.

---

## 2. The database

Three tables. `db/schema.sql` is the authoritative definition; the JPA entities
mirror it exactly and EclipseLink is configured with
`eclipselink.ddl-generation=none` so it never alters it.

### 2.1 users

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `full_name` | VARCHAR(100) NOT NULL | |
| `email` | VARCHAR(190) NOT NULL | `uq_users_email` UNIQUE — this is the login |
| `password_hash` | VARCHAR(255) NOT NULL | PBKDF2-HMAC-SHA256, never plaintext |
| `created_at`, `updated_at` | DATETIME NOT NULL | set by `@PrePersist` / `@PreUpdate` |

190 characters, not 255, because `utf8mb4` uses 4 bytes per character and
InnoDB's index key limit is 3072 bytes — 190 × 4 fits, 255 × 4 does not.

### 2.2 subjects

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `user_id` | BIGINT NOT NULL | → `users(id)` **ON DELETE CASCADE** |
| `name` | VARCHAR(80) NOT NULL | |
| `color` | VARCHAR(7) NULL | `ck_subjects_color` CHECK `^#[0-9a-fA-F]{6}$` |
| `created_at`, `updated_at` | DATETIME NOT NULL | |

Constraints:

- `uq_subjects_user_name UNIQUE (user_id, name)` — one student cannot have
  "DBMS" twice, two students both can.
- `uq_subjects_id_user UNIQUE (id, user_id)` — looks redundant beside the
  primary key, and is the whole trick behind §2.3.
- `ix_subjects_user` on `(user_id)`.

### 2.3 tasks

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `user_id` | BIGINT NOT NULL | → `users(id)` ON DELETE CASCADE |
| `subject_id` | BIGINT NOT NULL | part of the composite FK below |
| `title` | VARCHAR(150) NOT NULL | |
| `description` | TEXT NULL | |
| `task_type` | VARCHAR(16) NOT NULL | CHECK in `ASSIGNMENT, PRACTICAL, SUBMISSION, EXAM, OTHER` |
| `priority` | VARCHAR(8) NOT NULL | CHECK in `LOW, MEDIUM, HIGH` |
| `status` | VARCHAR(12) NOT NULL | CHECK in `PENDING, IN_PROGRESS, COMPLETED` |
| `due_date` | DATE NOT NULL | a calendar date, no zone |
| `due_time` | TIME NULL | optional |
| `created_at`, `updated_at` | DATETIME NOT NULL | |
| `completed_at` | DATETIME NULL | |

The interesting constraint:

```sql
CONSTRAINT fk_tasks_subject_owner FOREIGN KEY (subject_id, user_id)
    REFERENCES subjects (id, user_id) ON DELETE RESTRICT
```

**"A task's subject must belong to the same user as the task" is enforced by the
database, not only by the code.** A row whose `subject_id` points at another
student's subject cannot be inserted at all — the composite foreign key has
nothing to match. That is what `uq_subjects_id_user` exists for: a foreign key
needs a unique index on the columns it references.

`ON DELETE RESTRICT` means deleting a subject that still has tasks is refused by
MySQL. The application never hits that error, because it asks the user what
should happen first (§4.3).

And:

```sql
CONSTRAINT ck_tasks_completed_at CHECK (
    (status = 'COMPLETED' AND completed_at IS NOT NULL)
    OR (status <> 'COMPLETED' AND completed_at IS NULL))
```

`completed_at` is set exactly when the task is completed and cleared when it is
reopened. The database refuses any other combination, so the two columns cannot
drift apart.

Indexes: `(user_id, status)`, `(user_id, due_date)`, `(user_id, priority)`,
`(subject_id)` — one for each way the list page filters.

### 2.4 Relationships, in words

```
users 1 ──── n subjects 1 ──── n tasks
  └────────────────────── n tasks
```

- A user has many subjects; a subject belongs to one user.
- A subject has many tasks; a task belongs to one subject.
- A user has many tasks directly as well. The redundant `tasks.user_id` is what
  makes every query ownership-scoped in one predicate instead of a join, and it
  is what the composite foreign key uses to tie the two paths together.

Deleting a user cascades to their subjects and tasks. Deleting a subject is
restricted while tasks remain.

### 2.5 No comma-separated columns

Nothing is stored as a delimited list. Each relationship is its own column with
a real foreign key.

---

## 3. Package by package

```
src/java/
  deadlinebuddy.properties      the one config file -> WEB-INF/classes
  com/entity/   JPA entities and the enums they persist
  com/dao/      JPQL queries, one class per table
  com/service/  validation, business rules, transaction boundaries
  com/util/     config, hashing, dates, JPA plumbing, request helpers
  com/web/      servlets
  com/filter/   authentication and CSRF filters
src/conf/
  persistence.xml               -> WEB-INF/classes/META-INF/persistence.xml
  MANIFEST.MF
web/
  index.jsp                     the welcome file; redirects
  assets/css/app.css            the hand-written stylesheet
  assets/js/app.js              progressive enhancement only
  assets/vendor/                Bootstrap 5.3 CSS + JS, vendored, no CDN
  WEB-INF/web.xml               welcome file, session, error pages, no scriptlets
  WEB-INF/glassfish-web.xml     context root, class-loader delegation
  WEB-INF/jsp/                  every view - not reachable by URL
  WEB-INF/jsp/fragments/        reusable JSP fragments
lib/
  jakarta.servlet.jsp.jstl-3.0.1.jar    the JSTL implementation (see §7.1)
db/
  schema.sql, seed.sql
```

### 3.1 com.entity

| Class | What it is |
|---|---|
| `User` | A student account. `getInitials()` feeds the header badge. |
| `Subject` | A course. `@Transient taskCount` / `completedCount` are filled in by the DAO from a grouped query; `getProgressPercent()` divides them, returning 0 when there are no tasks. |
| `DeadlineTask` | One deadline. Named `DeadlineTask` so it does not collide with `java.util.concurrent` or Ant's `Task`. |
| `TaskType` | ASSIGNMENT, PRACTICAL, SUBMISSION, EXAM, OTHER — with a display label and a null-safe `parse`. |
| `Priority` | HIGH, MEDIUM, LOW — with a `rank` the sort uses and a CSS class. |
| `TaskStatus` | PENDING, IN_PROGRESS, COMPLETED, and `canMoveTo()` — the single definition of a legal transition. |
| `DeadlineCategory` | OVERDUE, DUE_TODAY, UPCOMING, COMPLETED — derived, never stored. |

All enums are `@Enumerated(EnumType.STRING)`. Storing ordinals would mean that
inserting a new task type later silently reinterprets existing rows.

`DeadlineTask` carries the derived getters the views read — `getCategory()`,
`isOverdue()`, `isDueToday()`, `isUrgent()`, `getDueLabel()`,
`getDueDateDisplay()`. The JSPs contain no date logic at all.

### 3.2 com.dao

Every method takes the `EntityManager` it should use; no DAO opens a transaction.
That keeps the transaction boundary in one place and lets two DAO calls share
one transaction — which is what makes the subject delete atomic.

| Class | Notable methods |
|---|---|
| `UserDao` | `findByEmail` (case-insensitive), `emailExists`, `insert` |
| `SubjectDao` | `listWithCounts` — one grouped `LEFT JOIN` query for all the subject cards; `findOwned`; `nameTaken(…, excludeId)`; `reassignTasks`; `deleteTasks` |
| `TaskDao` | `search`, `countMatching`, `upcoming`, `overdue`, `changeStatus`, `counts` |
| `TaskQuery` | Not a DAO: the filter state, built from the query string |

There is deliberately **no `findById(id)` that ignores the owner**. Every lookup
is `findOwned(id, userId)` and every statement carries `t.user.id = :userId`, so
a servlet cannot accidentally load someone else's row by trusting a parameter.

`TaskDao.counts` returns all six dashboard figures from a single query — one pass
over the user's tasks with `SUM(CASE WHEN …)` per bucket.

`TaskDao.search` assembles its WHERE clause from whichever filters are set.
Values always go in as bound parameters. Two details worth pointing out:

- The keyword is escaped before it becomes a `LIKE` pattern, with
  `ESCAPE '!'`, so a `%` or `_` typed into the search box is a literal
  character and not a wildcard.
- Sorting by priority uses `ORDER BY CASE WHEN t.priority = :pHigh THEN 0 WHEN
  t.priority = :pMedium THEN 1 ELSE 2 END`. JPQL has no notion of an enum's own
  ordering, and the enum names are bound as parameters so the SQL cannot drift
  away from the `Priority` enum.

### 3.3 com.service

| Class | Responsibility |
|---|---|
| `AuthService` | Registration and sign-in validation, hashing, the duplicate-email rule |
| `SubjectService` | Subject CRUD and the delete policy (§4.3) |
| `TaskService` | Task CRUD, the deadline reads, and the status machine (§4.2) |
| `DashboardStats` | An immutable carrier for the six counts and the percentage |

The create and update methods take the **raw form strings** and do all the
parsing and validation themselves. That is why there is exactly one definition
of a task's rules, and why the servlets are thin.

`TaskService.parse` validates everything in one pass and collects the problems
into a `ValidationException`, so a form with three mistakes reports all three
rather than one per submission.

### 3.4 com.util

| Class | Responsibility |
|---|---|
| `Config` | The four-level key resolution described in §1.3 |
| `Jpa` | Owns the single `EntityManagerFactory`; `read(…)` and `write(…)` are the transaction boundaries |
| `Passwords` | PBKDF2-HMAC-SHA256 hashing and verification; `main` prints a hash |
| `Dates` | Calendar helpers, HTML date/time parsing, relative labels |
| `Web` | Parameter reading, the CSRF token, session keys, flash messages, `safePath` |
| `AppLifecycle` | `@WebListener`: starts the persistence unit on deploy, closes it on undeploy |
| `AppException` and friends | `ValidationException`, `NotFoundException`, `ConflictException`, `DataAccessException` |

`Jpa.write` commits on success and rolls back on any exception; `Jpa.read` always
closes the entity manager. `translate()` passes application exceptions through
untouched and wraps everything else — driver errors, dead connections, unexpected
constraint violations — in `DataAccessException`, so the servlet layer can show a
sentence instead of a stack trace.

### 3.5 com.web

| Servlet | Mapping | Does |
|---|---|---|
| `AuthServlet` | `/login`, `/register`, `/logout` | sign in, register, sign out |
| `DashboardServlet` | `/dashboard` | the overview; read-only |
| `TaskServlet` | `/tasks`, `/tasks/*` | list, forms, create, update, delete, status |
| `SubjectServlet` | `/subjects`, `/subjects/*` | cards, forms, create, update, two-step delete |
| `BaseServlet` | — | shared rendering, redirecting and error handling |

Mappings are declared with `@WebServlet` only. `web.xml` holds no servlet or
filter mappings, so the two cannot disagree, and there are no duplicate or
conflicting patterns.

`BaseServlet.service` is where error handling lives, once, for every request:

| Exception | Status | Page |
|---|---|---|
| `NotFoundException` | 404 | "Not found" |
| `ConflictException` | 409 | "Already changed" |
| `ValidationException` / `AppException` | 400 | "That did not work" |
| `DataAccessException` | 500 | "Database unavailable", with a hint about MySQL |
| anything else | 500 | generic; the trace goes to the server log |

Errors are **forwarded**, not redirected. `setStatus(403)` followed by
`sendRedirect` would reach the browser as a 302 and the real status would be
lost.

### 3.6 com.filter

| Filter | Covers | Does |
|---|---|---|
| `AuthFilter` | `/dashboard`, `/tasks`, `/tasks/*`, `/subjects`, `/subjects/*` | requires a session; otherwise redirects to `/login?next=…`; sets `Cache-Control: no-store` on authenticated pages |
| `CsrfFilter` | `/*`, POST only | rejects any POST whose `csrf` parameter does not match the session's token, with a forwarded 403 |

Because `AuthFilter` covers the prefixes, no servlet has to remember to check the
session. Because `CsrfFilter` covers every POST, every state change is protected
— including sign-in, registration and sign-out.

The views live under `WEB-INF/jsp/`, so they are not reachable by URL at all:
`/WEB-INF/jsp/dashboard.jsp` is a 404.

### 3.7 The views

Each JSP declares its **own** `<%@ page %>` and `<%@ taglib %>` directives at the
very top, before any `<c:…>` tag, and the shared fragments declare none. This is
not a style preference — a taglib directive that lands mid-page after the prefix
has been used produces `PWC6307: The prefix c … has been previously used` and
every page 500s. See the comment at the top of `fragments/head.jspf`.

| View | Used by |
|---|---|
| `login.jsp`, `register.jsp` | own layout; there is no navigation to draw yet |
| `dashboard.jsp` | the six tiles, completion, quick add, overdue, upcoming, subject cards |
| `tasks.jsp` | deadline tabs, search, four filters, sort, reset, results |
| `task-form.jsp` | one form for both create and edit |
| `task-delete.jsp`, `subject-delete.jsp` | confirmation pages |
| `subjects.jsp`, `subject-form.jsp` | subject cards and the add/edit form |
| `error.jsp` | all errors, from the servlets, the CSRF filter and the container |
| `fragments/head.jspf`, `foot.jspf`, `task-row.jspf` | the shared chrome and the task row |

`web.xml` sets `<scripting-invalid>true</scripting-invalid>`, which makes "no
Java in the JSPs" a rule the translator enforces rather than a habit. Everything
is JSTL and EL, and all user text goes through `<c:out>`, which is what escapes
it.

---

## 4. The flows and the state machines

### 4.1 Signing in

1. `GET /login` renders the form with a CSRF token.
2. `POST /login` → `AuthService.login` looks the email up case-insensitively and
   verifies the hash.
3. Failure gives **one** message — "Email or password is incorrect." — for both
   an unknown email and a wrong password, so the form cannot be used to discover
   which addresses have accounts. When the email is unknown the hash is verified
   anyway against a throwaway value, so the two cases take about the same time.
4. Success: the old session is **invalidated** and a new one created before the
   user id is stored. That is what defeats session fixation — a session id
   planted before sign-in is not the one that ends up authenticated.
5. Redirect to `next` if it is one of this application's own pages, otherwise to
   the dashboard. `AuthServlet.safeNext` drops anything with a scheme, a host, a
   backslash or a leading `//`, so `next` cannot become an open redirect.

Only the id, name and email go in the session. Never the password or its hash.

### 4.2 The task status machine

```
          ┌──────────────┐  start   ┌──────────────┐
          │   PENDING    │ ───────▶ │ IN_PROGRESS  │
          │              │ ◀─────── │              │
          └──────────────┘   (edit) └──────────────┘
                 │ complete              │ complete
                 ▼                       ▼
          ┌─────────────────────────────────────┐
          │             COMPLETED               │
          └─────────────────────────────────────┘
                 │ reopen
                 ▼
             PENDING
```

`TaskStatus.canMoveTo` is the only definition of a legal move. A completed task
cannot jump straight back to IN_PROGRESS; it is reopened to PENDING first. The
edit form offers all three statuses and obeys exactly the same rule.

Every change is written as a **conditional update**:

```sql
UPDATE tasks SET status = ?, updated_at = ?, completed_at = ?
 WHERE id = ? AND user_id = ? AND status = ?   -- the status we expect to replace
```

If that matches no row — the task was already completed, the form was submitted
twice, two tabs raced — it returns 0 rows and the service raises a
`ConflictException` instead of half-applying the change. A conflict on a status
button is shown as a message on the page the student came from, because their
view is simply out of date; reloading fixes it.

`completed_at` is set on the way into COMPLETED and cleared on the way out, and
`ck_tasks_completed_at` makes the database refuse any other combination.

### 4.3 Deleting a subject

The foreign key is `ON DELETE RESTRICT`, so this choice is not cosmetic — without
it the delete would simply fail.

1. `GET /subjects/delete?id=…` shows how many tasks are attached.
2. If there are none, the only option is to delete.
3. If there are some, the student must choose:
   - **MOVE** — reassign them to another of their own subjects, then delete this
     one. The target is loaded with `findOwned`, so it cannot be another
     student's subject, and it cannot be the subject being deleted.
   - **DELETE** — delete the tasks with the subject.
4. `POST /subjects/delete` carries it out. Both branches run **inside one
   transaction** with the delete, so a subject is never left half-emptied.
5. If this is the student's only subject and it has tasks, there is nowhere to
   move them, and the page says so and offers to add another subject first.

A bulk update or delete bypasses the persistence context, so the service calls
`em.clear()` and re-reads the subject before removing it — otherwise EclipseLink
would delete a stale copy.

### 4.4 The deadline buckets

```
status = COMPLETED            -> COMPLETED
due_date <  today             -> OVERDUE
due_date =  today             -> DUE_TODAY
due_date >  today             -> UPCOMING
```

Computed in `DeadlineTask.getCategory()` every time it is asked for. Nothing is
stored and nothing refreshes it, so the buckets are correct the moment the date
rolls over — there is no scheduled job and no cache to invalidate.

A completed task is COMPLETED and nothing else. Finishing late does not leave it
counted as overdue — verified, see QA-REPORT.md §10.

`due_date` is a `DATE` and `due_time` a `TIME`; neither carries a zone, and the
application compares calendar dates with `LocalDate`, never instants. That is
what a student means by "due today", and it is what stops a task looking overdue
at 05:30 because something stored a UTC timestamp.

### 4.5 Completion percentage

```
total == 0   ->  0
otherwise    ->  round(completed / total * 100)
```

One formula, in two places that cannot disagree because they are the only two:
`DashboardStats.getCompletionPercent()` for the overall figure and
`Subject.getProgressPercent()` for a subject card. Both return 0 rather than
dividing by zero.

Every figure is read from the database on each request, so creating, editing,
completing, reopening or deleting a task changes the next page load with no
counter to keep in step.

### 4.6 Redirect after POST

Every write ends in `303`/`302` to a GET. Refreshing after saving re-runs the
redirect's GET, so no operation can be repeated with F5 or the back button. The
message that follows the redirect is a one-shot flash held in the session and
cleared when it is read.

---

## 5. Security

| Concern | How it is handled |
|---|---|
| Passwords | PBKDF2-HMAC-SHA256, 210 000 iterations, 16-byte random salt, 256-bit key, from the JDK. Stored as `pbkdf2_sha256$iterations$salt$hash`; the iteration count travels with the hash, so raising it does not invalidate existing passwords. Compared with `MessageDigest.isEqual`. |
| Session | Session id replaced on sign-in; `HttpOnly` cookie; `COOKIE` tracking only, so no `jsessionid` in URLs; 60-minute idle timeout; invalidated on sign-out. |
| Authorisation | `AuthFilter` on every protected prefix, plus `user_id` in every single query. There is no DAO method that can load a row without an owner. |
| Horizontal privilege | A composite foreign key makes cross-owner data impossible at the database level, not just in code. Another student's id gives a **404**, deliberately the same answer as a nonexistent id — "that task exists but is not yours" is already a leak. |
| CSRF | A 32-byte random per-session token in a hidden field on every form; `CsrfFilter` rejects any POST without a match, forwarding a real 403. A token from another session is rejected. |
| XSS | Every user value is written with `<c:out>`. A `<script>` tag in a task title is stored verbatim and rendered escaped — verified. |
| SQL injection | JPQL with bound parameters everywhere. The only string that is ever concatenated into a query is the choice of which clause to add, never a value. `%` and `_` in the search box are escaped. |
| Open redirect | `AuthServlet.safeNext` and `Web.safePath` accept only this application's own paths. |
| Error detail | Nothing from an exception reaches the browser. The log gets the trace; the user gets a sentence. |
| Credentials in source | None. One properties file, overridable by environment variable; `persistence.xml` holds placeholders. |
| Views | Under `WEB-INF`, so unreachable by URL. |

---

## 6. Design decisions worth defending

### 6.1 Why `tasks.user_id` as well as `tasks.subject_id`

It is derivable through the subject, so it is formally redundant. It is there
because it makes **ownership a single predicate** on the table being queried
rather than a join, and because it gives the composite foreign key something to
anchor to — which is what turns "a task's subject must be the same user's" from a
code convention into a database guarantee. The redundancy cannot drift: the
foreign key is what keeps the two paths consistent.

### 6.2 Why the relationships are unidirectional

`Subject → User` and `DeadlineTask → User`/`Subject` are `@ManyToOne`. There is
no `@OneToMany` collection on `User` or `Subject`. A lazy collection on a
detached entity is the classic source of a `LazyInitializationException` halfway
through rendering a JSP, and nothing in this application needs to walk from a
user to their tasks in memory — the DAO asks the database, with the filters
already applied.

`DeadlineTask.subject` is `EAGER` because every task that is listed is listed
with its subject's name and colour; the list queries also `JOIN FETCH` it, so
there is no query per row.

### 6.3 Why `RESOURCE_LOCAL` instead of a JTA data source

A JTA unit would need a GlassFish JDBC connection pool, a JNDI resource, the
driver installed at server level, and a restart, all before the first deployment
— and all of it would have to be redone on the next machine. `RESOURCE_LOCAL`
with the driver in `WEB-INF/lib` deploys as-is. The cost is that transactions are
explicit, which `com.util.Jpa` handles in one place.

### 6.4 If you want a JTA data source instead

1. Copy the connector to `D:\glassfish7\glassfish\domains\domain1\lib\ext\` and
   restart the domain.
2. Create the pool and resource:

```bash
asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource ^
  --restype javax.sql.DataSource ^
  --property user=root:password=123456789:URL="jdbc\:mysql\://localhost\:3306/deadlinebuddy_db" ^
  DeadlineBuddyPool
asadmin create-jdbc-resource --connectionpoolid DeadlineBuddyPool jdbc/deadlinebuddy
asadmin ping-connection-pool DeadlineBuddyPool
```

3. In `persistence.xml`: `transaction-type="JTA"`, add
   `<jta-data-source>jdbc/deadlinebuddy</jta-data-source>`, and drop the
   `jakarta.persistence.jdbc.*` properties.
4. In `com.util.Jpa`, replace the `read`/`write` helpers with an injected
   `@PersistenceContext EntityManager` and `@Resource UserTransaction`, or move
   the service methods into EJBs and let the container manage the transactions.

Nothing else in the application changes — that is the point of keeping the
transaction boundary in one class.

### 6.5 Why a past due date is allowed

Students do add work they are already late on, and it should appear as overdue.
Only nonsense is refused: before 2000, or more than ten years ahead, which is
what catches a mistyped year like `0202`.

---

## 7. The two build gotchas

### 7.1 JSTL

GlassFish 7 ships the JSTL **API** (`jakarta.servlet.jsp.jstl-api.jar`) but not
an implementation. Without one, every `<c:…>` tag fails to resolve. The
implementation is therefore vendored at
`lib/jakarta.servlet.jsp.jstl-3.0.1.jar` and ends up in `WEB-INF/lib`.

The JSTL 3.0 taglib URIs are `jakarta.tags.core`, `jakarta.tags.functions` and
`jakarta.tags.fmt` — not the old `http://java.sun.com/jsp/jstl/...` ones.

### 7.2 A library has to be declared in three places

Being present on disk is not enough. For each jar:

- `nbproject/project.xml` → a `<library>` entry under `<web-module-libraries>`
  with `<path-in-war>WEB-INF/lib</path-in-war>`
- `nbproject/project.properties` → a `file.reference.*` property, and the same
  reference listed in `javac.classpath`
- `nbproject/build-impl.xml` → matching `<copyfiles>` tasks in both
  `library-inclusion-in-archive` and `library-inclusion-in-manifest`

If those three disagree, `ant dist` fails with
`Could not find file ${file.reference...} to copy`. Both jars are declared in
all three here, and both by a path **relative to the project** -
`lib\mysql-connector-j-9.7.0.jar` and
`lib\jakarta.servlet.jsp.jstl-3.0.1.jar`.

That is a deliberate change from the other labs in this workspace, which point
`file.reference.mysql-connector-j-9.7.0.jar` at
`D:\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar`. An absolute path is
fine on one machine and fatal on another: the build stops with
`Could not find file ${file.reference...} to copy` as soon as the project is
opened somewhere that has Connector/J in a different place, or nowhere at all.
Vendoring the jar into `lib/` costs 2.6 MB in the project folder and makes it
build anywhere.

To go back to the shared copy on `D:`, change that one line in
`nbproject/project.properties` and delete `lib\mysql-connector-j-9.7.0.jar`;
`project.xml` and `build-impl.xml` refer to the property, not the path, so they
need no edit.

---

## 8. Likely viva questions

**Why `jakarta.*` and not `javax.*`?**
Jakarta EE 9 renamed every package when the specification moved to the Eclipse
Foundation. GlassFish 7 is Jakarta EE 10, so it only has `jakarta.*`. Mixing the
two does not fail at compile time if both jars are around — it fails at deploy
time, when the container cannot find a servlet it recognises.

**What is the difference between `RESOURCE_LOCAL` and `JTA`?**
With `JTA` the container owns the transaction and can span several resources;
with `RESOURCE_LOCAL` the application owns it and it covers one `EntityManager`.
`RESOURCE_LOCAL` needs explicit `begin`/`commit`/`rollback` — here in
`com.util.Jpa` — and needs no server configuration. See §6.3.

**Why is the `EntityManagerFactory` created once and the `EntityManager` per
request?**
The factory is expensive and thread-safe; it owns the connection pool.
`EntityManager` is cheap and **not** thread-safe — it is a unit of work and must
not be shared between requests.

**Why `@Enumerated(EnumType.STRING)` and not the default?**
The default is `ORDINAL`, which stores the position. Insert a new constant in the
middle of the enum later and every existing row silently means something else.

**How does the application stop one student reading another's tasks?**
Three layers. `AuthFilter` requires a session; every query carries
`user_id = :userId` and there is no DAO method that can load a row without an
owner; and the composite foreign key makes a cross-owner row impossible to
insert in the first place. A foreign id gets a 404, the same as a nonexistent
one.

**What is CSRF and what stops it here?**
A third-party page making the browser submit a request with the victim's cookies.
Every form carries a per-session random token and `CsrfFilter` rejects any POST
without a match, so a page that cannot read the session cannot forge one.

**Why store a hash instead of the password, and why PBKDF2?**
So a database dump does not hand over everyone's password. PBKDF2 is
deliberately slow and salted: the salt defeats rainbow tables and precomputation
across users, and the iteration count makes brute force expensive. It is in the
JDK, so it needs no dependency.

**What does redirect-after-POST solve?**
A POST response that renders a page leaves the POST as the current request, so
F5 repeats the operation. Redirecting means the page the user ends on is a GET,
and refreshing it is harmless.

**Why is `completed_at` a column when it can be derived?**
It cannot be derived — nothing else records *when* a task was finished.
`updated_at` changes on every edit. The CHECK constraint keeps it in step with
the status.

**Why are the deadline buckets not stored?**
They depend on today's date, so a stored value would be wrong the next morning
and would need a scheduled job to fix. Deriving them costs a date comparison and
is always right.

**How are duplicate subject names prevented, and why twice?**
`UNIQUE (user_id, name)` in the database, plus a lookup in the service so the
message appears next to the field. The lookup is a convenience; the index is the
guarantee, and it is what holds when two requests arrive at the same moment.

**What happens if two tabs complete the same task?**
The first wins. The second's conditional `UPDATE … AND status = 'IN_PROGRESS'`
matches no row, so it reports "that task was changed in another tab" instead of
applying a second time.

**Why is `uq_subjects_id_user` not redundant?**
A composite foreign key needs a unique index on exactly the columns it
references. `(id, user_id)` is unique anyway because `id` is — but without the
declared constraint MySQL will not let `tasks` reference that pair.

---

## 9. What is deliberately not here

No roles or admin module — FR-07 asks for simple accounts, and a permission
system would be scope the project does not need. No file uploads, no email, no
notifications, no scheduled jobs, no REST API, no pagination (a term's worth of
tasks is tens of rows, and the filters are the answer to volume).

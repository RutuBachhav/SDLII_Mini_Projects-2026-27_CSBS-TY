# DeadlineBuddy — how to run it

Everything you need to get from a fresh checkout to a working demo, plus the
walkthrough to show and the queries to prove it with.

---

## 1. Credentials

Both accounts are created by `db/seed.sql`, with real PBKDF2-HMAC-SHA256 hashes
— no plaintext password is stored anywhere.

| Who | Email | Password | What they have |
|---|---|---|---|
| Ananya Pawar | `ananya.pawar@student.edu` | `123456789` | The demo account: 5 subjects, 16 tasks, work already completed, 2 overdue, 2 due today |
| Rohan Mehta | `rohan.mehta@student.edu` | `rohan1234` | A second student: 2 subjects, 4 tasks — use this one to show that students cannot see each other's data |

There are no roles. Every account is a student and sees only its own subjects
and tasks. Sign up for a third account from the registration page at any time.

To change a password, generate a new hash with the same code the application
verifies with and paste it into the `users` row:

```bash
java -cp build\web\WEB-INF\classes com.util.Passwords newpassword
```

---

## 2. The MySQL to paste

Open a terminal in the project folder:

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\schema.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\seed.sql
```

Or inside the MySQL client:

```sql
SOURCE D:/Java college lab/DeadlineBuddy/db/schema.sql;
SOURCE D:/Java college lab/DeadlineBuddy/db/seed.sql;
```

`schema.sql` drops and recreates `deadlinebuddy_db`, so it is also the reset
button. `seed.sql` clears the tables and restarts the ids first, so running it
twice is the same as running it once.

`seed.sql` finishes by printing what it created. It should say:

```
email                     total  completed  in_progress  pending  overdue  due_today  pct
ananya.pawar@student.edu     16          6            2        8        2          2   38
rohan.mehta@student.edu       4          1            1        2        0          0   25
```

Those are exactly the numbers the dashboard then shows. The due dates are
written relative to `CURDATE()`, so there is always something overdue, something
due today and something coming up, whenever you install it.

If your MySQL root password is not `123456789`, change it in **one** place —
`src/java/deadlinebuddy.properties` — or leave the file alone and set an
environment variable before starting GlassFish:

```bash
set DEADLINEBUDDY_JDBC_PASSWORD=your-password
```

---

## 3. NetBeans, step by step

1. **File → Open Project…**, select `D:\Java college lab\DeadlineBuddy`, **Open
   Project**. It opens as a Java Web application with `Source Packages`,
   `Configuration Files`, `Web Pages` and `Libraries` nodes.
2. Right-click the project → **Properties → Run**.
   - **Server**: your GlassFish Server. If the list is empty: **Add…** →
     *GlassFish Server* → *Local* → installation `D:\glassfish7` → domain
     `domain1`.
   - **Context Path**: `/DeadlineBuddy`.
   - **Java EE Version**: *Jakarta EE 10 Web*.
   - **OK**.
3. Check **Properties → Libraries**. Two jars must be listed:
   - `mysql-connector-j-9.7.0.jar` at `D:\mysql-connector-j-9.7.0\`
   - `jakarta.servlet.jsp.jstl-3.0.1.jar` at `lib\` inside the project

   If either is missing, **Add JAR/Folder…** and point at it. The JSTL one is
   not optional — GlassFish ships the JSTL API but not an implementation, and
   without it every `<c:…>` tag fails.
4. **Run → Clean and Build Project** (Shift+F11). Expect `BUILD SUCCESSFUL` and
   `dist\DeadlineBuddy.war`.
5. **Run → Run Project** (**F6**). NetBeans starts GlassFish, deploys, and opens
   <http://localhost:8080/DeadlineBuddy/>.
6. Sign in as `ananya.pawar@student.edu` / `123456789`.

**One warning you can ignore.** The build prints
`warning: [options] location of system modules is not set in conjunction with
-source 17`. That is javac noting the project targets Java 17 while running on a
newer JDK. It is expected — see README §1.5 for why the target is 17.

### Without NetBeans

```bash
ant clean dist
set AS_JAVA=D:\Apache NetBeans\jdk
asadmin deploy --force=true --contextroot /DeadlineBuddy --name DeadlineBuddy dist\DeadlineBuddy.war
```

`asadmin` needs `JAVA_HOME` or `AS_JAVA` set, otherwise it stops with "The java
command is not executable".

To remove it again: `asadmin undeploy DeadlineBuddy`.

---

## 3A. Setting it up on a different machine

If you received this as a zip, there are exactly three things that depend on the
machine. Nothing else needs touching.

**1. Your MySQL password.** The project ships with `root` / `123456789`, which is
almost certainly not yours. Change it in one place —
`src/java/deadlinebuddy.properties`:

```properties
jdbc.user=root
jdbc.password=your-password
```

Or leave the file alone and set an environment variable before starting
GlassFish, which is tidier if you do not want to edit a tracked file:

```bash
set DEADLINEBUDDY_JDBC_PASSWORD=your-password
```

If you get this wrong, every page shows "Database unavailable" — a clear
message, not a stack trace.

**2. Your GlassFish and your NetBeans.** The zip does **not** include
`nbproject/private/`, which is where NetBeans records which server you picked and
where your IDE user directory is. NetBeans recreates it the first time you open
the project and set the server, so just follow §3 step 2.

**3. Your MySQL client path.** The commands in §2 assume
`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`. Use whatever `mysql` is
on your machine, or run the two scripts from MySQL Workbench.

### Building from the command line instead of the IDE

**Open it in NetBeans and press F6 — that is the supported path**, and it needs
none of what follows. NetBeans writes `nbproject/private/` itself the first time
you set the server, and that file is what tells the build where GlassFish is.

If you want to run Ant directly on a machine where NetBeans has never opened
this project, that file does not exist yet, and a bare `ant dist` stops with:

```
The Java EE server classpath is not correctly set up - server home directory is missing.
```

and then, once that is supplied:

```
The libs.CopyLibs.classpath property is not set up.
```

Both come from NetBeans' own settings, not from the project. Supply them on the
command line — adjust the two paths to your machine:

```bash
set JAVA_HOME=D:\Apache NetBeans\jdk
ant -Dj2ee.server.home=D:\glassfish7\glassfish ^
    -Duser.properties.file=%APPDATA%\NetBeans\30\build.properties ^
    clean dist
```

`30` is the NetBeans version directory — check what is actually under
`%APPDATA%\NetBeans\`. This was verified by extracting the zip to an unrelated
folder and building it there from scratch: `BUILD SUCCESSFUL`, with both jars
copied into `WEB-INF/lib` from the project's own `lib/`.

**The libraries are not a problem.** Both jars — the MySQL connector and the
JSTL implementation — are inside the project's own `lib/` folder and referenced
by a relative path, so the build needs nothing installed anywhere else. You do
not need to download Connector/J.

Then: create the database (§2), open and run it (§3), and check it with §3B.

## 3B. Checking you got the same database

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -t < db\verify.sql
```

It prints a table per check, each ending in **OK**. It verifies the structure
(3 tables, 3 foreign keys, 5 check constraints, the composite foreign key), the
row counts (2 users, 7 subjects, 20 tasks), the per-account figures, that every
password is a PBKDF2 hash, and three integrity invariants that must all be zero.

Any **MISMATCH** means the database is not in the starting state — re-run
`schema.sql` then `seed.sql`.

### One thing to know about the dates

`seed.sql` writes due dates **relative to the day you run it** (`CURDATE()`),
not as fixed dates. That is deliberate: it means a fresh install always has
something overdue, something due today and something coming up, whenever it is
installed, instead of a demo full of tasks from 2026 that are all overdue.

The consequence: **seed on Monday, demo on Wednesday, and the two "due today"
tasks will have become overdue.** That is the deadline logic working correctly —
but your dashboard will read 4 overdue / 0 due today instead of 2 / 2.

So: **re-run `seed.sql` on the day you demo.** It is safe to re-run — it clears
the tables and restarts the ids first. `verify.sql`'s last table tells you
whether you need to; it says either `as seeded (2 overdue, 2 due today)` or
`seeded on an earlier day - re-run seed.sql before demoing`.

The counts that do *not* depend on the day — 16 tasks, 6 completed, 2 in
progress, 8 pending, 38% — are checked as hard assertions and will be identical
on any machine on any day.

## 4. Walkthrough — about five minutes

**The dashboard** (`/dashboard`)

1. Six counts across the top: total, completed, pending, in progress, overdue,
   due today. Every one is read from the database on this request. Each tile is
   a link into the task list filtered to the same set, so a count and the list
   behind it can never disagree — click **Overdue** and count the rows.
2. **Completion** shows 38% with a progress bar: 6 of 16 completed.
3. **Overdue** lists what is late, in red, most overdue first. **Upcoming
   deadlines** lists what is next, nearest first; an overdue task is not in it,
   because the panel above already has it.
4. **Quick add** creates a task with three fields. It posts to the same endpoint
   as the full form, with the defaults the full form would have used.
5. **Subjects** at the bottom: a card per course with its own progress bar.

**Complete something**

6. Click **Complete** on a task in the Upcoming panel. You land back on the
   dashboard with "Completed …", the Completed count goes up by one, Pending
   goes down by one, and the percentage moves. Press **F5** — nothing happens
   twice, because every write ends in a redirect.
7. Find it under **Tasks → Completed** and click **Reopen**. The counts go back.

**The task list** (`/tasks`)

8. Five tabs: All, Overdue, Due today, Upcoming, Completed.
9. Search for `banker` — it matches the *description* of the deadlock
   assignment, not just titles.
10. Set Subject = DBMS, Priority = High, Status = In Progress. The filters
    combine. **Reset filters** clears them. Every combination is a plain URL, so
    it is bookmarkable — and the whole bar works with JavaScript switched off.
11. **Sort by** → Priority: High first, then Medium, then Low, each group by due
    date.
12. Search for something that does not exist: a useful empty state with a way
    back, not a blank page.

**Create and edit**

13. **New task**: leave everything blank and submit. Every field reports its own
    problem at once, and what you did type is still there.
14. Fill it in properly and save. Then **Edit** it, change the priority and the
    due date, save: the list shows the change immediately.
15. Set the due date to **yesterday** and save — it turns red and is counted as
    overdue straight away. Past dates are allowed on purpose: students do add
    work they are already late on.

**Subjects** (`/subjects`)

16. **New subject**: pick a name and a colour. Try the name of a subject you
    already have — "You already have a subject called …".
17. Delete a subject that has tasks. The confirmation page says how many, and
    makes you choose: move them to another subject, or delete them with it.
    Choose **move**, then check that the task count on the target card went up
    and nothing was lost.

**Security, in one minute**

18. Open a second browser (or a private window) and sign in as Rohan. He sees 4
    tasks and 2 subjects, none of them Ananya's.
19. In Rohan's window, edit the URL to one of Ananya's task ids —
    `/tasks/edit?id=3` — and you get a **404**, exactly as you would for a task
    that does not exist. Telling him it exists but is not his would already be a
    leak.
20. **Sign out**, then press the browser's back button. The dashboard does not
    come back from cache; you land on the sign-in page.

---

## 5. Inspection queries

Run these next to the browser to show that the pages are reading the database
and not making numbers up.

**The dashboard's six counts, straight from SQL** — these must match the tiles:

```sql
USE deadlinebuddy_db;
SELECT COUNT(*)                                              AS total,
       SUM(status = 'COMPLETED')                             AS completed,
       SUM(status = 'PENDING')                               AS pending,
       SUM(status = 'IN_PROGRESS')                           AS in_progress,
       SUM(status <> 'COMPLETED' AND due_date <  CURDATE())  AS overdue,
       SUM(status <> 'COMPLETED' AND due_date =  CURDATE())  AS due_today,
       ROUND(SUM(status = 'COMPLETED') / COUNT(*) * 100)     AS pct
FROM tasks
WHERE user_id = (SELECT id FROM users WHERE email = 'ananya.pawar@student.edu');
```

**Per-subject progress** — matches the subject cards:

```sql
SELECT s.name,
       COUNT(t.id)                                   AS tasks,
       SUM(t.status = 'COMPLETED')                   AS done,
       IFNULL(ROUND(SUM(t.status='COMPLETED')/COUNT(t.id)*100), 0) AS pct
FROM subjects s
LEFT JOIN tasks t ON t.subject_id = s.id
WHERE s.user_id = 1
GROUP BY s.id, s.name
ORDER BY s.name;
```

**What a task looks like before and after you complete it** — watch `status`,
`completed_at` and `updated_at`:

```sql
SELECT id, title, status, due_date, due_time, completed_at, updated_at
FROM tasks WHERE user_id = 1 ORDER BY due_date;
```

**Passwords are hashed, not stored:**

```sql
SELECT email, LEFT(password_hash, 22) AS algorithm_and_cost, LENGTH(password_hash)
FROM users;
-- pbkdf2_sha256$210000$...
```

**Nothing is orphaned** — should always return 0:

```sql
SELECT COUNT(*) AS orphaned_tasks
FROM tasks t LEFT JOIN subjects s ON s.id = t.subject_id
WHERE s.id IS NULL;
```

**No task is filed under another student's subject** — should always return 0.
This one cannot happen even if you try: the composite foreign key refuses it.

```sql
SELECT COUNT(*) AS cross_owner_tasks
FROM tasks t JOIN subjects s ON s.id = t.subject_id
WHERE t.user_id <> s.user_id;
```

**Prove the constraint by trying to break it** — MySQL refuses:

```sql
-- task for user 1, filed under one of user 2's subjects
INSERT INTO tasks (user_id, subject_id, title, task_type, priority, status,
                   due_date, created_at, updated_at)
VALUES (1, (SELECT id FROM subjects WHERE user_id = 2 LIMIT 1),
        'Should not exist', 'OTHER', 'LOW', 'PENDING', CURDATE(), NOW(), NOW());
-- ERROR 1452: Cannot add or update a child row: a foreign key constraint fails
```

**And the status/completed_at invariant:**

```sql
UPDATE tasks SET status = 'COMPLETED' WHERE id = 1;
-- ERROR 3819: Check constraint 'ck_tasks_completed_at' is violated.
-- (COMPLETED requires completed_at; the application always sets both together)
```

**Deleting a subject that still has tasks is refused** — which is why the
application asks you what to do with them first:

```sql
DELETE FROM subjects WHERE id = 1;
-- ERROR 1451: Cannot delete or update a parent row: a foreign key constraint fails
```

---

## 6. If something does not work

| Symptom | Cause | Fix |
|---|---|---|
| Every page: "Database unavailable" | MySQL is not running, or the credentials are wrong | Start the `MySQL80` service; check `jdbc.*` in `src/java/deadlinebuddy.properties`; confirm `db/schema.sql` was applied |
| `No suitable driver found for jdbc:mysql://…` | The connector did not make it into the WAR | Check `WEB-INF/lib` inside `dist\DeadlineBuddy.war`; see README §7.2 — a jar has to be declared in `project.xml`, `project.properties` *and* `build-impl.xml` |
| `ant dist`: `Could not find file ${file.reference…} to copy` | The same three files disagree about a library | README §7.2 |
| Every page 500s with `PWC6307: The prefix c … has been previously used` | A JSP used a `<c:…>` tag before its own `taglib` directive | Each JSP declares its own `page` and `taglib` directives at the very top; the shared fragments declare none |
| `<c:…>` tags render as literal text, or "unable to find taglib" | The JSTL *implementation* is missing | `lib\jakarta.servlet.jsp.jstl-3.0.1.jar` must be in Libraries and in `WEB-INF/lib` |
| `asadmin`: "The java command is not executable" | No JDK for asadmin | `set AS_JAVA=D:\Apache NetBeans\jdk` |
| Port 8080 already in use | Another server, or a GlassFish that did not stop | `asadmin stop-domain domain1`, then start it again |
| A form gives "Request rejected" | The CSRF token expired with the session while the page sat open | Reload the page and submit again — this is the filter working |
| Deployment fails with a `javax.servlet` error | Wrong server generation | GlassFish 7 is Jakarta EE 10; this project is `jakarta.*` throughout and will not run on GlassFish 5 — README §1.5 |

Server log: `D:\glassfish7\glassfish\domains\domain1\logs\server.log`. Every
unexpected failure is logged there in full; the browser only ever gets a
sentence.

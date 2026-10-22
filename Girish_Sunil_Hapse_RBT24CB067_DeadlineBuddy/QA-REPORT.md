# DeadlineBuddy — QA report

What was actually run against the deployed application, what it found, and what
was not verified.

Nothing in this report is a claim about code that was only read. Every result
below came from driving the running application over HTTP, inspecting MySQL
afterwards, or looking at the rendered page in a browser.

---

## 1. How it was tested

| | |
|---|---|
| Build | NetBeans' own Ant (`D:\Apache NetBeans\extide\ant\bin\ant.bat`), `clean dist` |
| Deployed to | Eclipse GlassFish 7.1.1, domain1, context root `/DeadlineBuddy` |
| Running on | Azul Zulu OpenJDK 25.0.2 (the JDK the GlassFish process was verified to be using) |
| Database | MySQL 8.0.44, `deadlinebuddy_db`, created from `db/schema.sql` + `db/seed.sql` |
| Driver | `curl` with per-user cookie jars, CSRF tokens scraped from each page |
| Assertions | Compared against the **database**, not against the application's own output, wherever a number was involved |
| Browser | The rendered pages checked at 1280px and at 375px, console watched |
| Harness | Kept outside the project folder, in the session scratchpad |

The suite is 27 numbered sections. Each run starts from a freshly dropped and
reseeded database so the numbers are reproducible.

---

## 2. Results

**264 HTTP assertions, 264 passed, 0 failed**, in the final run against the
final build.

| Batch | Sections | Assertions | Passed | Failed |
|---|---|---|---|---|
| 1 | Anonymous access, CSRF, registration validation, registration, login | 42 | 42 | 0 |
| 2 | Dashboard vs database, subject CRUD, task create and validation, status machine, overdue accounting | 61 | 61 | 0 |
| 3 | Task edit, search / filter / sort / reset, task delete | 54 | 54 | 0 |
| 4 | Subject delete policies, cross-user authorisation, unknown routes, open redirect, sign-out, XSS | 70 | 70 | 0 |
| 5 | Crawl every linked page as three users, dead links, no-JavaScript | 20 | 20 | 0 |
| 6 | Persistence across sign-out and a new session | 6 | 6 | 0 |
| 7 | Persistence across a full GlassFish restart | 11 | 11 | 0 |

Plus **6 database constraint probes** run directly in MySQL (§6), all of which
refused the write as documented, and the browser checks in §5.

### 2.1 The crawl

Every in-application link was followed from `/dashboard`, `/tasks` and
`/subjects`, transitively, as three different users:

| User | Pages crawled | Non-200 |
|---|---|---|
| Ananya (16 tasks, 5 subjects) | 311 | 0 |
| Rohan (4 tasks, 2 subjects) | 129 | 0 |
| Priya (new account, no data at all) | 26 | 0 |

466 page loads, every one a 200. This also proves every JSP translates and
compiles: `compile.jsps=false`, so each page is translated on first access, and
all of them were accessed.

The new-account crawl is the empty-state pass: a user with no subjects and no
tasks reaches every page without a single error, and each page offers the next
step rather than showing a blank panel.

---

## 3. What each area was checked for

**Build and deployment.** `ant clean dist` from NetBeans' own Ant produced
`DeadlineBuddy.war` with both libraries present in `WEB-INF/lib` and
`persistence.xml` at `WEB-INF/classes/META-INF/`. `asadmin deploy` succeeded.
Rebuilt and redeployed six times during testing without a failure.

**Database and JPA start-up.** The persistence unit connected to MySQL on
deploy. Every dashboard figure was compared against the same figure computed in
SQL, and matched on every run.

**Registration, login, logout, sessions.** Empty form reports all four field
errors at once; a malformed email, a short password, a mismatched confirmation
and a duplicate email are each rejected with their own message and no row
written. A successful registration writes one row whose `password_hash` starts
`pbkdf2_sha256$210000$`, and the plaintext appears nowhere in the table. Wrong
password and unknown email both give one identical message and a 401. The
session id changes on sign-in. Sign-out invalidates the session and the
dashboard is protected again immediately.

**Subjects.** Create, rename, recolour, delete. Duplicate names rejected per
user; renaming a subject to the name it already has is allowed. A colour that is
not `#rrggbb` is rejected rather than silently dropped. Progress on each card
matched a SQL `GROUP BY` of the same subject.

**Tasks.** Create, edit, delete, complete, reopen, start. Every column written
was read back from MySQL and compared, including `due_time` and the timestamps.
A blank form reports five field errors at once and inserts nothing. A malformed
date, a malformed time, a year ten years out and a mistyped year like `0202` are
each rejected with their own message.

**Deadline logic.** Overdue, due today, upcoming and completed were each
compared against the equivalent SQL predicate — matching counts every time.
Completing an overdue task lowers the overdue count by exactly one and
reopening it restores it; a completed task is never counted as overdue.

**Search, filtering, sorting.** Search matches titles *and* descriptions; a `%`
or `_` typed into the search box is a literal character and not a wildcard.
Each of the four filters, each of the five views, and combinations of three at
once were counted against SQL. All three sort orders were compared row-for-row
against `ORDER BY` in SQL, including the priority order, which is a JPQL `CASE`
expression — the rendered order matched
`ORDER BY FIELD(priority,'HIGH','MEDIUM','LOW'), due_date, id` exactly. Reset
clears everything. A URL full of nonsense filter values renders the full list
with a 200 rather than an error.

**Progress and completion.** 6 of 16 → 38%, matching `ROUND(6/16*100)`. A new
account with no tasks shows 0%, not a division error. The figures move
immediately after every create, complete, reopen and delete.

**Repeated submissions and conflicts.** Every write ends in a redirect. Posting
the same status change twice leaves the task in the state the first one set and
reports "already in progress" instead of applying twice. A completed task
cannot jump to in progress, from the button or from the edit form; it has to be
reopened. `completed_at` is set on completion and cleared on reopen, verified in
MySQL both ways.

**Authorisation.** Signed in as Rohan, against Ananya's ids: GET on a task edit,
task delete, subject edit and subject delete each returned **404** — the same
answer as a nonexistent id. Forged POSTs to `/tasks/status`, `/tasks/delete`,
`/tasks/update`, `/subjects/update` and `/subjects/delete` each returned 404 and
MySQL confirmed the target rows were untouched. Creating a task under another
user's subject was rejected with a field error and no row. Deleting a subject
with "move the tasks to" pointed at another user's subject was rejected and no
task changed owner.

**CSRF.** A POST with no token, with a wrong token, and with a token belonging to
a *different signed-in session* were each rejected with a real **403** and the
database was unchanged.

**XSS.** `<script>alert(1)</script>` as a task title and `<img src=x
onerror=alert(2)>` as a description are stored verbatim in MySQL and rendered
escaped on the list, the dashboard and the edit form's `value` attribute.

**Open redirect.** `next=http://evil.example/x` and `next=//evil.example/x` are
both ignored in favour of the dashboard; `next=/tasks?view=OVERDUE` is honoured;
`next=/login` is ignored.

**Error handling.** Unmapped URLs 404. `/WEB-INF/jsp/dashboard.jsp` 404s —
the views are not reachable by URL. `WEB-INF/classes/deadlinebuddy.properties`
404s. Missing, non-numeric, negative and out-of-range ids all 404 rather than
500. No stack trace, SQL fragment or credential appeared in any response body.

**Persistence.** A task created, then signed out, then signed back in with a
brand-new cookie jar: still there, status and due time intact. Then the whole
GlassFish domain was **stopped and started**: every row count identical, the
same password still worked, the dashboard tiles still matched the database, the
pre-restart task was still listed and still in progress, and a new task could
still be created and deleted afterwards.

---

## 4. Defects found and fixed

All five were found by this testing and fixed in the delivered build. Each was
re-tested after the fix.

### D-1 — A crafted enum value in a form crashed the redisplay (500)

**Found by** section 8, posting an empty task form (and any POST with
`taskType=NOPE`).
**Symptom** 500 instead of the form coming back with its validation messages.
Server log: `java.lang.IllegalArgumentException: No enum constant
com.entity.TaskType.NOPE`.
**Cause** The service rejected the value correctly, but the JSP redisplaying the
form compared the *raw submitted string* with the enum constant
(`${taskType eq t}`). EL coerces the String to the enum to compare them, and an
unknown value throws — inside the view, after the status was already set.
**Fix** Compare string to string (`${taskType eq t.name()}`), so nothing coerces
unvalidated input into an enum. The same class of bug was closed for
`subjectId`: the servlet now puts a `Long` (or null) on the request rather than
raw text, so `subjectId=abc` cannot throw during rendering either.
**Retest** All six validation assertions in section 8 pass; the form returns 400
with five field messages and inserts nothing.

### D-2 — "1task is overdue" — words glued together across the UI

**Found by** looking at the rendered dashboard.
**Symptom** `${stats.overdue} ${stats.overdue eq 1 ? 'task is' : 'tasks are'}`
rendered as `1task is`. Affected every count sentence on the dashboard, the task
list and the subject list.
**Cause** `<trim-directive-whitespaces>true</trim-directive-whitespaces>` in
`web.xml`. It makes Jasper drop template text that is only whitespace — including
the single space between two adjacent EL expressions.
**Fix** Removed the setting, with a comment in `web.xml` saying why, so it does
not get helpfully re-added.
**Retest** Every page re-checked for glued output; none found.

### D-3 — Bootstrap's `.d-block` defeated the mobile header rule

**Found by** the 375px pass.
**Symptom** The header took two rows on a phone; the stylesheet's rule to hide
the email had no effect.
**Cause** The markup used Bootstrap's `.d-block` utility, which is
`display: block !important`. The `!important` beat the narrow-screen rule in
`app.css`.
**Fix** Dropped the utility class and set `display: block` in `app.css` itself,
where the media query can override it. The name now stays on a phone — it is how
you tell whose data you are looking at — and only the email is hidden.
**Retest** 375px header is one row, name visible, no horizontal scroll.

### D-4 — A warning logged on every single POST

**Found by** reading the server log while testing CSRF.
**Symptom** `AS-WEB-CORE-00059: Unable to set request character encoding to
UTF-8 … because request parameters have already been read` on every POST.
**Cause** `BaseServlet.service` called `setCharacterEncoding("UTF-8")`, but
`CsrfFilter` has already read a parameter by then, so the call is both too late
and redundant — `web.xml` already declares `<request-character-encoding>`.
**Fix** Removed the call, with a comment explaining why it is not needed.
**Retest** The warning no longer appears.

### D-5 — Completed tasks printed their date twice

**Found by** the 375px pass.
**Symptom** `20 Sept 2026 · was due 20 Sept 2026`.
**Cause** The row prints the due date, and `getDueLabel()` also returned "was
due <the same date>" for completed tasks.
**Fix** A completed task's label now says when it was *finished*
("completed 6 Oct 2026"), which is information the row did not already carry.
**Retest** Looked at in the browser; the two now say different things.

### Not defects

Four failures during the run turned out to be bugs in the test harness, not the
application, and are recorded here so the pass counts are honest: a cookie jar
reused across an open-redirect probe (the signed-in session was bounced off
`/login` before the token could be read), a `$JAR` variable referenced before it
was set, an assertion looking for `"% "` where the markup has `"%</span>"`, and
one looking for status buttons on a filtered view that was legitimately empty.
All four were fixed in the harness and the assertions then passed against the
unchanged application.

---

## 5. Browser checks

Done by eye and by reading the console, in the Chromium pane, signed in as the
demo account.

| Check | Result |
|---|---|
| Console errors, dashboard and task list | None. The console is empty. |
| Desktop layout (1280px) | Six tiles in a row, three-column dashboard, dense and readable |
| Phone layout (375px) | Tiles reflow to two columns, filters stack one per row, task rows stack with their buttons below, **no horizontal scroll** |
| Colour | One accent (blue). No purple, no gradients, no glassmorphism, no emoji anywhere |
| Numbers | Every figure on screen traced to a database row; no decorative statistics |
| Buttons and links | Every one exercised by the crawl and the workflow tests; no `href="#"`, no placeholder actions |
| Completed tasks | Struck through and visually quiet, still actionable |

**JavaScript disabled.** The entire HTTP suite — 264 assertions, including every
filter combination, every form and every destructive action — ran through `curl`,
which executes no JavaScript at all. The filter bar is a GET form with an Apply
button, every state change is a real POST form, and both destructive actions have
their own server-rendered confirmation page. `app.js` only adds submit-on-change,
a second `confirm()` on destructive submits, the colour preview and dismissing a
flash message. The pages were also checked for `onclick=` and `javascript:`
attributes: there are none.

---

## 6. Database constraints, probed directly

Each of these was run in the MySQL client against the live database. All six were
refused, and the row counts were unchanged afterwards.

| Attempt | Result |
|---|---|
| Insert a task for user 1 under one of user 2's subjects | `ERROR 1452` — `fk_tasks_subject_owner` |
| Set a task to `COMPLETED` without `completed_at` | `ERROR 3819` — `ck_tasks_completed_at` |
| Delete a subject that still has tasks | `ERROR 1451` — `fk_tasks_subject_owner` (ON DELETE RESTRICT) |
| Insert a subject with `color = 'red'` | `ERROR 3819` — `ck_subjects_color` |
| Insert a second subject called `DBMS` for the same user | `ERROR 1062` — `uq_subjects_user_name` |
| Insert a second user with an existing email | `ERROR 1062` — `uq_users_email` |

The first one is the important one: **a task cannot be filed under another
student's subject even by writing SQL directly.** That rule is enforced by the
schema, not only by the application.

Two invariants were also asserted after every destructive test in the suite and
never once failed:

```sql
SELECT COUNT(*) FROM tasks t LEFT JOIN subjects s ON s.id = t.subject_id
WHERE s.id IS NULL;                                       -- orphaned tasks: 0

SELECT COUNT(*) FROM tasks t JOIN subjects s ON s.id = t.subject_id
WHERE t.user_id <> s.user_id;                             -- cross-owner tasks: 0
```

---

## 7. What was not verified

Stated plainly, because "tested" should mean tested.

- **NetBeans' own GUI.** The project was built with the Ant that ships inside
  NetBeans (`extide\ant\bin\ant.bat`) and its metadata was cloned from a working
  NetBeans web project in this same workspace, but nobody clicked **Open
  Project → F6** in the IDE. If the IDE's Libraries dialog shows something
  unexpected, HOW-TO-RUN.md §3 step 3 says what to check.
- **Other GlassFish or JDK versions.** Verified on GlassFish 7.1.1 running on
  Zulu OpenJDK 25.0.2 only. JDK 25 is outside GlassFish 7.1.1's supported range
  (11–21); it worked here throughout, including a full restart, but that is an
  observation, not a guarantee — see README §1.5.
- **A JTA data source.** The application runs `RESOURCE_LOCAL`. The migration
  steps in README §6.4 are written from the documentation and were **not
  executed**; the `asadmin create-jdbc-connection-pool` commands there are
  untested.
- **Real browsers other than the Chromium pane.** No Firefox, Safari or Edge, and
  no real phone — the 375px check was an emulated viewport.
- **Concurrency under load.** The conflict path was tested by submitting the same
  transition twice in sequence, which is what a double click or a refresh does.
  Two genuinely simultaneous requests were not fired. The protection is a
  conditional `UPDATE ... WHERE status = ?`, which the database serialises, so
  the mechanism does not depend on the timing — but that specific race was not
  reproduced.
- **Accessibility.** ARIA roles and labels are present on the progress bars and
  the alerts, and the forms use real `<label for>` elements, but no screen reader
  or audit tool was run.
- **Volume.** The largest account tested has 16 tasks and 5 subjects. There is no
  pagination; the filters are the answer to volume. Behaviour with thousands of
  rows is untested.
- **Time zones and the date boundary.** All testing happened in one time zone on
  one machine. The logic compares `LocalDate` against `DATE` columns, with no
  instant conversion anywhere, which is what makes the boundary correct — but
  nobody moved the clock over midnight to watch a task roll from "due today" to
  "overdue".

---

## 8. Handoff: verified from the zip

The project was zipped for another machine and the zip itself was tested, not
assumed.

| Step | Result |
|---|---|
| Extract `DeadlineBuddy.zip` to an unrelated folder | 11.4 MB, extracted clean |
| `ant clean dist` there, with no NetBeans project metadata of its own | **BUILD SUCCESSFUL** |
| Both jars resolved | Copied into `WEB-INF/lib` from the project's own `lib/` |
| Deploy that WAR under a separate context root `/dbzip` | Deployed, signed in, dashboard tiles `16 6 8 2 2 2`, 16 task rows, subjects page 200 |
| Test deployment removed afterwards | `undeploy` clean; the other applications on the server untouched |

Three portability problems were found and fixed in the process:

1. **The connector was referenced by an absolute path**
   (`D:\mysql-connector-j-9.7.0\...`). On any machine without Connector/J in
   that exact place, `ant dist` stops with `Could not find file
   ${file.reference...} to copy`. The jar is now vendored in the project's
   `lib/` and referenced relatively. README §7.2 records the change and how to
   revert it.
2. **`nbproject/private/` is machine-specific** — it names this machine's
   GlassFish and NetBeans user directory. It is excluded from the zip; NetBeans
   writes its own when the recipient sets the server.
3. **A bare command-line `ant dist` fails without NetBeans' settings**, first
   with `The Java EE server classpath is not correctly set up` and then with
   `The libs.CopyLibs.classpath property is not set up`. Both come from
   NetBeans, not the project. HOW-TO-RUN §3A gives the two `-D` properties that
   make a command-line build work; opening in NetBeans needs neither.

A fourth, smaller thing was removed: `nbproject/build-impl.xml~`, a stale
pre-rename backup left over from scaffolding, still containing the old project
name and referenced by nothing.

### The one thing that is not byte-identical, on purpose

`seed.sql` writes due dates relative to `CURDATE()`, so the absolute dates in
`due_date` differ between installs. The day-invariant figures — 16 tasks, 6
completed, 2 in progress, 8 pending, 38%, and Rohan's 4/1/1/2/25 — are identical
everywhere, and `db/verify.sql` asserts them. The deadline split (2 overdue, 2
due today) is identical **on the day you seed**, and drifts afterwards as real
dates pass, which is the deadline logic working. `verify.sql` reports which of
the two situations you are in and says to re-run `seed.sql` if needed.

This was caught in testing: a database seeded on 14 October read 4 overdue /
0 due today when checked on 15 October. The application was right; the expected
values in the first draft of `verify.sql` were wrong, and were corrected.

## 9. Reproducing this

The harness lives in the session scratchpad, outside the project, as `t.sh`
(helpers) and `run1.sh` … `run7.sh`. To run it again:

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/seed.sql
# ant clean dist && asadmin deploy --force=true --contextroot /DeadlineBuddy dist/DeadlineBuddy.war
bash run1.sh; bash run2.sh; bash run3.sh; bash run4.sh; bash run5.sh; bash run6.sh
# asadmin restart-domain domain1
bash run7.sh
```

Batches 1–6 must run in order against one seeded database — later sections build
on records earlier ones create. `run6.sh` writes the pre-restart snapshot that
`run7.sh` compares against, so the restart goes between them.

# HostelManagement RBAC

Database-backed authentication and role-based access control for the
HostelManagement Jakarta EE application.

## Roles and permissions

| Capability | ADMIN | WARDEN | VIEWER |
| --- | --- | --- | --- |
| View hostels and rooms | Yes | Yes | Yes |
| Create / update / delete hostels | Yes | No | No |
| Create / update rooms | Yes | Yes | No |
| Delete rooms | Yes | No | No |
| Search available rooms | Yes | Yes | Yes |
| Submit and cancel own room requests | Yes | Yes | Yes |
| Review room requests and confirm allocations | Yes | Yes | No |
| Manage user accounts | Yes | No | No |

Unknown roles, missing roles, and inactive accounts are denied by default.
Every server-side servlet and REST endpoint performs its own authorization
check; hiding a button in the UI is never the only control.

The inline **Update** and **Delete** actions on both All Hostels and All Rooms
are shown only to `ADMIN`. `WARDEN` retains the existing separate room-update
page, while room deletion remains restricted to `ADMIN`.

## Prerequisites

- JDK 21
- GlassFish 7.1 (`domain1`) with MySQL 8 reachable on `localhost:3306`
- `jbcrypt-0.4.jar` in `lib/` and `web/WEB-INF/lib/`
- A backup of `hostel_db`

## Database setup

Run the migrations once against the application database, in order:

```sql
SOURCE database/migrations/V0__base_schema.sql;
SOURCE database/migrations/V1__rbac.sql;
SOURCE database/migrations/V2__room_requests.sql;
```

`V0` creates the pre-RBAC `hostel` and `room` tables when they do not exist.
`V1` creates `app_users`, `app_roles`, and `user_roles`, and seeds the
`ADMIN`, `WARDEN`, and `VIEWER` roles. `V2` adds `room_request` and
`room_allocation` for availability-based selection and capacity tracking.
The scripts are safe to re-run and do not modify existing hostel/room rows.

The application no longer generates or extends schema at startup
(`eclipselink.ddl-generation=none`). Apply migrations before deploying a new
installation.

## Build and deploy

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
$env:AS_JAVA = $env:JAVA_HOME

& "C:\Program Files\Apache NetBeans\extide\ant\bin\ant.bat" `
  -f "C:\NetBeansProjects\HostelManagement\build.xml" clean dist

& "C:\Users\Dell\GlassFish_Server\glassfish\bin\asadmin.bat" `
  undeploy HostelManagement
& "C:\Users\Dell\GlassFish_Server\glassfish\bin\asadmin.bat" `
  deploy --name HostelManagement `
  "C:\NetBeansProjects\HostelManagement\dist\HostelManagement.war"
```

The application is available at
`http://localhost:8080/HostelManagement/`.

## First-time bootstrap

1. Deploy against a database with no rows in `app_users`.
2. Read the one-time key from the GlassFish log:

   ```powershell
   Select-String -Path `
     "C:\Users\Dell\GlassFish_Server\glassfish\domains\domain1\logs\server.log" `
     -Pattern "RBAC_SETUP_KEY="
   ```

3. Open `http://localhost:8080/HostelManagement/setup` and create the first
   `ADMIN` account using that key.
4. The setup endpoint disables itself permanently as soon as any account
   exists. The setup JSP lives under `WEB-INF` and cannot be requested
   directly.

An operator may supply the bootstrap key instead of using the generated one:

```text
-Dhostel.bootstrap.key=<one-time-key>
```

Set that JVM option in the GlassFish domain configuration before startup.
Remove it after setup.

## Account management

Sign in at `/login`. Administrators manage accounts at `/admin/users`:

- create `ADMIN`, `WARDEN`, or `VIEWER` accounts;
- activate or deactivate an account;
- an administrator cannot deactivate their own account.

Deactivation blocks new sign-ins immediately. Sessions that are already open
remain valid until they expire (30-minute timeout); shorten the timeout or
redeploy if immediate revocation is required.

Passwords must be at least 10 characters and contain at least one letter and
one digit. They are stored as BCrypt hashes (cost 12); plaintext passwords are
never stored or logged. The local development database has three test accounts
(`admin`, `warden`, `viewer`); their passwords are supplied separately and are
not committed to this repository.

## Security controls

- BCrypt password verification with a dummy hash for unknown usernames.
- Session fixation protection via `changeSessionId()` after login.
- Login throttling: five failed attempts per username/IP in ten minutes locks
  further attempts for five minutes.
- CSRF synchronizer token required for every non-safe request, including
  login, setup, logout, create, update, and delete. Tokens are accepted from
  the `_csrf` form field or the `X-CSRF-Token` header.
- Session cookies are `HttpOnly`; session tracking is cookie-only and the
  timeout is 30 minutes.
- `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, and
  `Referrer-Policy: same-origin` on application responses.
- Authenticated pages are sent with `Cache-Control: no-store`.
- Anonymous page requests redirect to `/login`; anonymous REST requests to
  `/api/*` receive a JSON `401`.
- Logout is POST-only. A GET request to `/logout` returns `405`.
- Use HTTPS in production so passwords and session cookies are encrypted in
  transit.

The database credentials still live in `src/conf/persistence.xml`. For
production, move them to a JNDI data source or environment-specific secrets
management rather than committing production credentials.

## Tests

### Unit checks

```powershell
$out = "build\test-classes"
New-Item -ItemType Directory -Force -Path $out | Out-Null
& "$env:JAVA_HOME\bin\javac.exe" `
  -cp "build\web\WEB-INF\classes;build\web\WEB-INF\lib\jbcrypt-0.4.jar" `
  -d $out test\SecuritySelfTest.java
& "$env:JAVA_HOME\bin\java.exe" `
  -cp "$out;build\web\WEB-INF\classes;build\web\WEB-INF\lib\jbcrypt-0.4.jar" `
  SecuritySelfTest
```

Covers the permission matrix, default-deny behavior, BCrypt verification, and
password strength rules.

### HTTP regression

The HTTP suite is bootstrap-aware, creates temporary `TEST-RBAC-*` records,
and cleans them up at the end. Run it after deploying and after the test
accounts exist:

```powershell
& powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\test\rbac-http-tests.ps1 `
  -AdminUser admin -AdminPassword "<admin-password>" `
  -WardenUser warden -WardenPassword "<warden-password>" `
  -ViewerUser viewer -ViewerPassword "<viewer-password>"
```

It verifies anonymous redirects/API 401s, login and session rotation, admin
CRUD, the admin-only All Hostels and All Rooms update/delete actions, CSRF
rejection, viewer/warden page and endpoint denials, user
activation/deactivation, self-deactivation protection, logout hardening,
security headers, and direct setup-JSP blocking.

Passwords passed on the command line can appear in shell history. Clear the
history or use an interactive wrapper in shared environments.

## Key files

| Path | Purpose |
| --- | --- |
| `database/migrations/V0__base_schema.sql` | Base hostel/room schema |
| `database/migrations/V1__rbac.sql` | RBAC tables and role seed |
| `database/migrations/V2__room_requests.sql` | Room requests and allocations |
| `src/java/com/entity/AppUser.java`, `AppRole.java` | RBAC entities |
| `src/java/com/entity/RoomRequest.java`, `RoomAllocation.java` | Selection entities |
| `src/java/com/room/RoomRequestDAO.java` | Availability and allocation logic |
| `src/java/com/web/FindRoomServlet.java` | Room search and filters |
| `src/java/com/web/ManageRoomRequestsServlet.java` | Request review and release |
| `src/java/com/rest/AvailableRoomResource.java` | `GET /api/available-rooms` |
| `ROOM_FEATURE.md` | Room selection design and operations |
| `src/java/com/security/AuthorizationService.java` | Permission matrix |
| `src/java/com/security/UserDAO.java` | User/role persistence |
| `src/java/com/security/PasswordUtil.java` | BCrypt hashing and policy |
| `src/java/com/security/CsrfUtil.java` | CSRF token generation/validation |
| `src/java/com/security/SessionAuth.java` | Session authentication state |
| `src/java/com/security/LoginRateLimiter.java` | Failed-login throttling |
| `src/java/com/security/BootstrapListener.java` | First-run setup key |
| `src/java/com/web/AuthFilter.java` | Authentication gate and headers |
| `src/java/com/web/CsrfFilter.java` | CSRF enforcement |
| `src/java/com/web/Access.java` | Servlet permission helper |
| `src/java/com/web/Layout.java` | Role-aware shared layout |
| `src/java/com/web/LoginServlet.java` | Login and session rotation |
| `src/java/com/web/LogoutServlet.java` | POST-only logout |
| `src/java/com/web/SetupServlet.java` | One-time admin bootstrap |
| `src/java/com/web/UserAdminServlet.java` | User management |
| `src/java/com/rest/RestAuthFilter.java` | JSON 401 for REST API |
| `web/WEB-INF/web.xml` | Filter order and session policy |
| `test/SecuritySelfTest.java` | Authorization/password unit checks |
| `test/rbac-http-tests.ps1` | End-to-end RBAC HTTP regression suite |
| `test/room-http-tests.ps1` | Room search/request/concurrency tests |

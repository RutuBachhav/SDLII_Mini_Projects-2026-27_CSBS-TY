# Room Preference and Availability-Based Selection

This feature adds database-backed room search, pending room requests, and
capacity-safe allocation confirmation to HostelManagement.

## Core model

A room **request** is not a reservation. A request starts as `PENDING` and
does not consume capacity. An authorized `ADMIN` or `WARDEN` reviews it and
either rejects it or approves it. Approval creates a `room_allocation` row,
which is the only record that consumes a place.

```text
app_users 1 ---- N room_request N ---- 1 room
app_users 1 ---- 1 room_allocation N -- 1 room
room_request 1 -- 0..1 room_allocation
```

`room_allocation` has unique keys on `user_id` and `request_id`:

- a user can hold at most one active allocation;
- a request can produce at most one allocation.

Request statuses are `PENDING`, `APPROVED`, `REJECTED`, and `CANCELLED`.
Releasing an approved allocation deletes the allocation row, marks the request
`CANCELLED`, and makes the room available again when tracked occupancy is
below capacity.

## Database migration

`database/migrations/V2__room_requests.sql` creates:

- `room_request` (`request_id`, `user_id`, `room_id`, `status`,
  `requested_at`, `decided_at`, `decided_by`);
- `room_allocation` (`allocation_id`, `user_id`, `room_id`, `request_id`,
  `allocated_at`, `allocated_by`).

The foreign keys reference the existing room primary key `room (ROOMID)`,
which is uppercase in the legacy schema. Room and request relationships use
`ON DELETE CASCADE`; decider/allocator users use `ON DELETE SET NULL`.

Apply all migrations in order before deploying this build:

```sql
SOURCE database/migrations/V0__base_schema.sql;
SOURCE database/migrations/V1__rbac.sql;
SOURCE database/migrations/V2__room_requests.sql;
```

`eclipselink.ddl-generation` remains `none`. Back up `hostel_db` before
applying schema changes.

Rollback is destructive and normally unnecessary: restore the backup taken
before V2, or drop `room_allocation` first and then `room_request`. Do not drop
tables while the application is serving traffic.

## Search and allocation behavior

`RoomRequestDAO.search` filters the live database by:

- hostel;
- minimum and maximum monthly fee;
- minimum capacity;
- room number substring;
- availability (available only, or all rooms).

A room is available when `Room.status = 'AVAILABLE'`, capacity is positive, and
the number of `room_allocation` rows is below capacity. Pending requests are
never counted.

The UI does not claim an exact number of free beds for legacy occupancy that
has no allocation records. It shows the room capacity, the tracked approved
allocation count, and the computed availability badge.

## Routes

| Route | Method | Permission | Purpose |
| --- | --- | --- | --- |
| `/findRoom` | GET | `ROOM_SEARCH` | Filtered room search |
| `/roomDetails` | GET | `ROOM_SEARCH` | Room facts and request action |
| `/requestRoom` | POST | `ROOM_REQUEST_CREATE` | Submit a pending request |
| `/myRoomRequests` | GET | `ROOM_REQUEST_READ_OWN` | Own request history |
| `/cancelRoomRequest` | POST | `ROOM_REQUEST_CREATE` | Cancel own pending request |
| `/manageRoomRequests` | GET | `ROOM_REQUEST_MANAGE` | Review requests |
| `/manageRoomRequests` | POST | `ROOM_REQUEST_MANAGE` | Reject a pending request |
| `/manageRoomRequests` | POST | `ROOM_REQUEST_MANAGE` + `ROOM_ALLOCATION_CONFIRM` | Approve or release an allocation |
| `/api/available-rooms` | GET | `ROOM_SEARCH` | JSON availability search |
| `/viewRoom` | GET | `ROOM_READ` | All Rooms, with ADMIN-only inline update/delete actions |
| `/getRoomForUpdate` | POST | `ROOM_UPDATE` | Load the existing prefilled room form |
| `/deleteRoom` | POST | `ROOM_DELETE` | Delete a room; redirects to `/viewRoom` when requested |

All state-changing forms use the existing CSRF filter. The authenticated user
comes from the session, never from a request parameter.

### API example

```text
GET /HostelManagement/api/available-rooms?hostelId=2&minFee=1000&maxFee=6000&minCapacity=2&roomNumber=A&availableOnly=true
```

Each result contains `room`, `activeAllocations`,
`trackedFreeCapacity`, and
`available`. Set `availableOnly=false` to include full or occupied rooms.
Invalid query values return `400`; anonymous callers receive `401`; callers
without `ROOM_SEARCH` receive `403`.

## Concurrency and capacity safety

Approval runs in one transaction:

1. Load the request to discover its room.
2. Lock the room row with `PESSIMISTIC_WRITE` and refresh its state.
3. Lock and re-read the request.
4. Reject self-approval and inactive requesters.
5. Recheck room status and capacity using a locking allocation query.
6. Enforce one active allocation per user.
7. Insert the allocation and mark the room `OCCUPIED` when full.

Because every approval locks the room row first, concurrent approvals for the
same room serialize. The allocation count is read with a locking query rather
than a stale repeatable-read snapshot. Unique database keys provide a final
safety net against duplicate active allocations.

Testing with a capacity-1 room and two concurrent approvals produced exactly
one `success=approved` and one `error=unavailable`.

## Security rules

- Search requires `ROOM_SEARCH`.
- A user may only cancel their own `PENDING` request; attempts on other users'
  requests return not-found.
- A user may not approve or release allocations without the manager and
  allocation-confirm permissions.
- Self-approval is rejected even for an administrator.
- A rejected, cancelled, or released request cannot be approved again.
- Deleting a room with active allocations is blocked by `DeleteRoomServlet`.
- Room request and allocation rows cascade when an unallocated room is deleted.
- Existing hostels, rooms, REST endpoints, RBAC, CSRF, and session handling are
  unchanged.

## Setup and deployment

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
$env:AS_JAVA = $env:JAVA_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

& "C:\Program Files\Apache NetBeans\extide\ant\bin\ant.bat" `
  -f "C:\NetBeansProjects\HostelManagement\build.xml" clean dist

& "C:\Users\Dell\GlassFish_Server\glassfish\bin\asadmin.bat" `
  undeploy HostelManagement

& "C:\Users\Dell\GlassFish_Server\glassfish\bin\asadmin.bat" `
  deploy --name HostelManagement --force=true `
  "C:\NetBeansProjects\HostelManagement\dist\HostelManagement.war"
```

Application URL: `http://localhost:8080/HostelManagement/`.

## Manual walkthrough

1. Apply `V2` to `hostel_db` and deploy the build.
2. Sign in as a `VIEWER`, open **Find a Room**, filter by hostel, budget,
   capacity, and room number, then open a room's details.
3. Submit a room request. Confirm **My Room Requests** shows `PENDING`.
4. Sign in as `ADMIN` or `WARDEN`, open **Manage Requests**, and review the
   pending request.
5. Approve it. Confirm a tracked allocation appears and the room becomes
   `OCCUPIED` when its capacity is reached.
6. Confirm the room disappears from the default available-only search and is
   still visible with **All rooms**.
7. Release the allocation and confirm the room is available again.
8. Try cancelling another user's request, approving your own request, and
   approving a request without permission; each operation must be denied.

## Automated tests

RBAC and password checks:

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

HTTP regression suites:

```powershell
& powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\test\rbac-http-tests.ps1 `
  -AdminUser admin -AdminPassword "<admin-password>" `
  -WardenUser warden -WardenPassword "<warden-password>" `
  -ViewerUser viewer -ViewerPassword "<viewer-password>"

& powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\test\room-http-tests.ps1 `
  -AdminUser admin -AdminPassword "<admin-password>" `
  -WardenUser warden -WardenPassword "<warden-password>" `
  -ViewerUser viewer -ViewerPassword "<viewer-password>"
```

The room suite creates temporary `TEST-ROOM-*` rooms, cleans them up, and
checks filters, details, REST availability, duplicate requests, ownership,
IDOR, self-approval, rejection/cancellation, release, occupied-room filtering,
deletion guard, admin room actions and redirects, concurrency, existing CRUD,
and permission denials.

Current local results:

- `SecuritySelfTest`: 59 checks passed;
- `rbac-http-tests.ps1`: 92 passed, 0 failed;
- `room-http-tests.ps1`: 86 passed, 0 failed;
- `ant clean dist`: successful.

## Limitations

- The legacy `room` table has no floor column, so floor preference is not
  implemented. Adding it requires a new schema migration and UI fields.
- Occupancy that existed before the allocation feature is not represented by
  `room_allocation` rows. Availability still relies on the existing room
  status for those records, so the UI reports tracked allocations rather than
  claiming exact free beds.
- One active allocation per user is enforced. Transferring a user directly
  from one room to another requires releasing the old allocation first.
- There is no waitlist, notification, payment, or automatic allocation.
- Room deletion is blocked only for active allocations; pending request
  history is removed with the room by the cascade.
- The search page is not paginated. Large room inventories should add paging
  and server-side limits.
- The feature was validated on MySQL 8 and GlassFish 7.1 with the existing
  local deployment.

## Key files

| Path | Purpose |
| --- | --- |
| `database/migrations/V2__room_requests.sql` | Request/allocation tables and constraints |
| `src/java/com/entity/RoomRequest.java` | Request entity |
| `src/java/com/entity/RoomAllocation.java` | Active allocation entity |
| `src/java/com/entity/RoomRequestStatus.java` | Status enum |
| `src/java/com/room/RoomRequestDAO.java` | Search, request, approve, reject, release |
| `src/java/com/room/AvailableRoom.java` | Room plus tracked allocation data |
| `src/java/com/room/RoomRequestException.java` | Domain error codes |
| `src/java/com/web/FindRoomServlet.java` | Search UI |
| `src/java/com/web/RoomDetailsServlet.java` | Details UI |
| `src/java/com/web/RequestRoomServlet.java` | Request submission |
| `src/java/com/web/MyRoomRequestsServlet.java` | Owner history |
| `src/java/com/web/CancelRoomRequestServlet.java` | Owner cancellation |
| `src/java/com/web/ManageRoomRequestsServlet.java` | Review and release |
| `src/java/com/web/RoomRequestSupport.java` | Shared badges/messages |
| `src/java/com/web/ViewRoomServlet.java` | Room directory and admin inline actions |
| `src/java/com/web/GetRoomForUpdateServlet.java` | Prefilled room update form |
| `src/java/com/web/DeleteRoomServlet.java` | Active-allocation deletion guard and directory redirects |
| `src/java/com/rest/AvailableRoomResource.java` | `GET /api/available-rooms` |
| `web/css/style.css` | Search, cards, detail, table, responsive styles |
| `test/room-http-tests.ps1` | End-to-end room feature tests |

param(
    [string]$BaseUrl = "http://localhost:8080/HostelManagement",
    [Parameter(Mandatory = $true)][string]$AdminUser,
    [Parameter(Mandatory = $true)][string]$AdminPassword,
    [Parameter(Mandatory = $true)][string]$WardenUser,
    [Parameter(Mandatory = $true)][string]$WardenPassword,
    [Parameter(Mandatory = $true)][string]$ViewerUser,
    [Parameter(Mandatory = $true)][string]$ViewerPassword
)

$ErrorActionPreference = "Stop"
$script:Temp = Join-Path $env:TEMP ("hostel-rbac-tests-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $script:Temp | Out-Null
$script:Pass = 0
$script:Fail = 0
$script:AdminJar = $null
$script:AdminToken = $null
$script:AdminSessionActive = $false

function Assert-True($condition, [string]$name) {
    if ($condition) {
        $script:Pass++
        Write-Host "PASS  $name" -ForegroundColor Green
    } else {
        $script:Fail++
        Write-Host "FAIL  $name" -ForegroundColor Red
    }
}

function New-Jar {
    return (Join-Path $script:Temp ("cookies-" + [guid]::NewGuid().ToString("N") + ".txt"))
}

function New-BodyFile {
    return (Join-Path $script:Temp ("body-" + [guid]::NewGuid().ToString("N") + ".html"))
}

function Get-CsrfToken([string]$html) {
    if ($null -eq $html) { return "" }
    $match = [regex]::Match($html, 'name="_csrf" value="([^"]+)"')
    if ($match.Success) { return $match.Groups[1].Value }
    return ""
}

function Get-Snippet([string]$html) {
    if ([string]::IsNullOrEmpty($html)) { return "" }
    $text = ($html -replace '<[^>]+>', ' ' -replace '\s+', ' ').Trim()
    if ($text.Length -gt 300) { return $text.Substring(0, 300) }
    return $text
}

function Get-SessionId([string]$jar) {
    if (-not (Test-Path -LiteralPath $jar)) { return "" }
    $content = Get-Content -Raw -LiteralPath $jar
    $match = [regex]::Match($content, 'JSESSIONID\s+(\S+)')
    if ($match.Success) { return $match.Groups[1].Value }
    return ""
}

function Http-Get([string]$jar, [string]$path) {
    $bodyFile = New-BodyFile
    $meta = & curl.exe -s -b $jar -c $jar -o $bodyFile -w "%{http_code}|%{redirect_url}" ($BaseUrl + $path)
    $parts = "$meta".Split("|")
    $body = ""
    if (Test-Path -LiteralPath $bodyFile) {
        $body = Get-Content -Raw -LiteralPath $bodyFile
    }
    return [pscustomobject]@{
        Status   = [int]$parts[0]
        Redirect = if ($parts.Length -gt 1) { $parts[1] } else { "" }
        Body     = $body
    }
}

function Http-Post([string]$jar, [string]$path, [System.Collections.IDictionary]$fields) {
    $bodyFile = New-BodyFile
    $arguments = @(
        "-s", "-b", $jar, "-c", $jar, "-X", "POST",
        "-o", $bodyFile, "-w", "%{http_code}|%{redirect_url}"
    )
    foreach ($key in $fields.Keys) {
        $arguments += "--data-urlencode"
        $arguments += ("{0}={1}" -f $key, $fields[$key])
    }
    $arguments += ($BaseUrl + $path)
    $meta = & curl.exe @arguments
    $parts = "$meta".Split("|")
    $body = ""
    if (Test-Path -LiteralPath $bodyFile) {
        $body = Get-Content -Raw -LiteralPath $bodyFile
    }
    return [pscustomobject]@{
        Status   = [int]$parts[0]
        Redirect = if ($parts.Length -gt 1) { $parts[1] } else { "" }
        Body     = $body
    }
}

function Login([string]$user, [string]$password) {
    $jar = New-Jar
    $page = Http-Get $jar "/login"
    $token = Get-CsrfToken $page.Body
    $before = Get-SessionId $jar
    $result = Http-Post $jar "/login" ([ordered]@{
        _csrf    = $token
        username = $user
        password = $password
    })
    $after = Get-SessionId $jar
    Assert-True ($result.Status -eq 302 -and $result.Redirect -like "*/index.jsp") "login succeeds for $user"
    Assert-True ($before -ne "" -and $after -ne "" -and $before -ne $after) "session id rotates on login for $user"
    return [pscustomobject]@{ Jar = $jar; Token = $token }
}

function Cleanup-TestData([string]$jar, [string]$token) {
    $rooms = Http-Get $jar "/api/rooms"
    if ($rooms.Status -eq 200) {
        foreach ($room in @($rooms.Body | ConvertFrom-Json | ForEach-Object { $_ })) {
            if ("$($room.roomNumber)".StartsWith("TEST-RBAC-")) {
                Http-Post $jar "/deleteRoom" ([ordered]@{
                    _csrf  = $token
                    roomId = $room.roomId
                }) | Out-Null
            }
        }
    }
    $hostels = Http-Get $jar "/api/hostels"
    if ($hostels.Status -eq 200) {
        foreach ($hostel in @($hostels.Body | ConvertFrom-Json | ForEach-Object { $_ })) {
            if ("$($hostel.hostelName)".StartsWith("TEST-RBAC-")) {
                Http-Post $jar "/deleteHostel" ([ordered]@{
                    _csrf    = $token
                    hostelId = $hostel.hostelId
                }) | Out-Null
            }
        }
    }
}

try {
    Write-Host "`nAnonymous access" -ForegroundColor Cyan
    $anon = New-Jar
    $r = Http-Get $anon "/index.jsp"
    Assert-True ($r.Status -eq 302 -and $r.Redirect -like "*/login") "dashboard redirects anonymous user to login"
    $r = Http-Get $anon "/admin/users"
    Assert-True ($r.Status -eq 302 -and $r.Redirect -like "*/login") "admin page redirects anonymous user to login"
    $r = Http-Get $anon "/api/hostels"
    Assert-True ($r.Status -eq 401 -and $r.Body -match "Unauthorized") "REST API returns JSON 401 when anonymous"
    $r = Http-Get $anon "/setup"
    Assert-True ($r.Status -eq 404) "setup endpoint is disabled after initialization"
    $r = Http-Get $anon "/logout"
    Assert-True ($r.Status -eq 302) "anonymous GET logout is safely redirected"

    Write-Host "`nAdmin access and CRUD" -ForegroundColor Cyan
    $admin = Login $AdminUser $AdminPassword
    $script:AdminJar = $admin.Jar
    $script:AdminToken = $admin.Token
    $script:AdminSessionActive = $true
    Cleanup-TestData $admin.Jar $admin.Token

    $r = Http-Get $admin.Jar "/index.jsp"
    Assert-True ($r.Status -eq 200 -and $r.Body -match "ADMIN" -and $r.Body -match "Manage Users") "admin dashboard shows admin navigation"
    $headers = (& curl.exe -s -D - -o NUL -b $admin.Jar ($BaseUrl + "/index.jsp")) -join "`n"
    Assert-True ($headers -match "X-Content-Type-Options: nosniff") "responses send nosniff header"
    Assert-True ($headers -match "X-Frame-Options: DENY") "responses send frame denial header"
    Assert-True ($headers -match "Referrer-Policy: same-origin") "responses send referrer policy"
    $loginHeaders = (& curl.exe -s -D - -o NUL ($BaseUrl + "/login")) -join "`n"
    Assert-True ($loginHeaders -match "Set-Cookie: JSESSIONID=[^;]+;.*HttpOnly") "session cookie is HttpOnly"
    $r = Http-Get $admin.Jar "/WEB-INF/setup.jsp"
    Assert-True ($r.Status -eq 404) "protected setup JSP cannot be requested directly"
    $r = Http-Get $anon "/WEB-INF/setup.jsp"
    Assert-True ($r.Status -eq 404) "anonymous direct setup JSP access is blocked by the container"
    $r = Http-Get $admin.Jar "/viewHostel"
    Assert-True ($r.Status -eq 200 -and $r.Body -match ">Actions</th>") "admin sees hostel table actions"
    Assert-True ($r.Body -match "/getHostelForUpdate" -and $r.Body -match "/deleteHostel") "admin sees hostel update and delete buttons"
    Assert-True ($r.Body -match 'name="returnTo" value="viewHostel"') "hostel delete action returns to the directory"
    Assert-True ($r.Body -match "Are you sure you want to delete this hostel") "hostel delete action asks for confirmation"
    $r = Http-Get $admin.Jar "/viewRoom"
    Assert-True ($r.Status -eq 200 -and $r.Body -match ">Actions</th>") "admin sees room table actions"
    Assert-True ($r.Body -match "/getRoomForUpdate" -and $r.Body -match "/deleteRoom") "admin sees room update and delete buttons"
    Assert-True ($r.Body -match 'name="returnTo" value="viewRoom"') "room delete action returns to the directory"
    Assert-True ($r.Body -match "Are you sure you want to delete this room") "room delete action asks for confirmation"
    $r = Http-Get $admin.Jar "/admin/users"
    Assert-True ($r.Status -eq 200 -and $r.Body -match "Create User") "admin can open user management"
    $r = Http-Get $admin.Jar "/deleteHostel"
    Assert-True ($r.Status -eq 200) "admin can open hostel delete list"
    $r = Http-Get $admin.Jar "/deleteRoom"
    Assert-True ($r.Status -eq 200) "admin can open room delete list"

    $r = Http-Post $admin.Jar "/saveHostel" ([ordered]@{
        hostelName  = "TEST-RBAC-HOSTEL"
        address     = "Test address"
        totalFloors = 2
    })
    Assert-True ($r.Status -eq 403) "missing CSRF token is rejected"
    $r = Http-Post $admin.Jar "/saveHostel" ([ordered]@{
        _csrf       = $admin.Token
        hostelName  = "TEST-RBAC-HOSTEL"
        address     = "Test address"
        totalFloors = 2
    })
    Assert-True ($r.Status -eq 200 -and $r.Body -match "Hostel Added Successfully") "admin can create a hostel with CSRF"
    $hostelMatch = [regex]::Match($r.Body, 'Hostel <b>#(\d+)')
    Assert-True ($hostelMatch.Success) "created hostel id is available"
    $testHostelId = $hostelMatch.Groups[1].Value

    if ($testHostelId) {
        $r = Http-Post $admin.Jar "/getHostelForUpdate" ([ordered]@{
            _csrf    = $admin.Token
            hostelId = $testHostelId
        })
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Edit Hostel") "admin can load hostel update form"
        Assert-True ($r.Body -match 'value="TEST-RBAC-HOSTEL"' -and $r.Body -match 'value="Test address"') "hostel update form is prefilled"
        $r = Http-Post $admin.Jar "/updateHostel" ([ordered]@{
            _csrf       = $admin.Token
            hostelId    = $testHostelId
            hostelName  = "TEST-RBAC-HOSTEL-UPDATED"
            address     = "Updated address"
            totalFloors = 3
        })
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Hostel Updated Successfully") "admin can update a hostel"
        $r = Http-Get $admin.Jar "/api/hostels/$testHostelId"
        Assert-True ($r.Status -eq 200 -and $r.Body -match "TEST-RBAC-HOSTEL-UPDATED") "hostel update is visible through REST API"
    }

    Write-Host "`nViewer access" -ForegroundColor Cyan
    $viewer = Login $ViewerUser $ViewerPassword
    $r = Http-Get $viewer.Jar "/index.jsp"
    Assert-True ($r.Status -eq 200 -and $r.Body -match "VIEWER" -and $r.Body -notmatch "/addHostel.jsp") "viewer dashboard is read-only"
    foreach ($path in @("/viewHostel", "/viewRoom")) {
        $r = Http-Get $viewer.Jar $path
        Assert-True ($r.Status -eq 200) "viewer can read $path"
    }
    $r = Http-Get $viewer.Jar "/viewHostel"
    Assert-True ($r.Body -notmatch "/getHostelForUpdate" -and $r.Body -notmatch "/deleteHostel") "viewer does not see hostel actions"
    $r = Http-Get $viewer.Jar "/viewRoom"
    Assert-True ($r.Body -notmatch "/getRoomForUpdate" -and $r.Body -notmatch "/deleteRoom") "viewer does not see room actions"
    if ($testHostelId) {
        $r = Http-Post $viewer.Jar "/getHostelForUpdate" ([ordered]@{
            _csrf    = $viewer.Token
            hostelId = $testHostelId
        })
        Assert-True ($r.Status -eq 403) "viewer cannot load the hostel update form"
        $r = Http-Post $viewer.Jar "/deleteHostel" ([ordered]@{
            _csrf     = $viewer.Token
            hostelId  = $testHostelId
            returnTo  = "viewHostel"
        })
        Assert-True ($r.Status -eq 403) "viewer cannot delete hostels"
    }
    $r = Http-Get $viewer.Jar "/api/hostels"
    Assert-True ($r.Status -eq 200 -and $r.Body -match "hostelId") "viewer can read REST API"
    foreach ($path in @("/addHostel.jsp", "/updateHostel.jsp", "/deleteHostel",
                        "/addRoom.jsp", "/updateRoom.jsp", "/deleteRoom",
                        "/admin/users")) {
        $r = Http-Get $viewer.Jar $path
        Assert-True ($r.Status -eq 403) "viewer is denied $path"
    }
    $r = Http-Post $viewer.Jar "/saveHostel" ([ordered]@{
        _csrf       = $viewer.Token
        hostelName  = "TEST-RBAC-DENIED"
        totalFloors = 1
    })
    Assert-True ($r.Status -eq 403) "viewer cannot create hostels"
    $r = Http-Post $viewer.Jar "/deleteRoom" ([ordered]@{
        _csrf  = $viewer.Token
        roomId = 3
    })
    Assert-True ($r.Status -eq 403) "viewer cannot delete rooms"

    Write-Host "`nWarden access" -ForegroundColor Cyan
    $warden = Login $WardenUser $WardenPassword
    $r = Http-Get $warden.Jar "/index.jsp"
    Assert-True ($r.Status -eq 200 -and $r.Body -match "WARDEN" -and $r.Body -match "/addRoom.jsp" -and $r.Body -notmatch "/deleteRoom") "warden dashboard allows room management only"
    foreach ($path in @("/viewHostel", "/viewRoom", "/addRoom.jsp", "/updateRoom.jsp")) {
        $r = Http-Get $warden.Jar $path
        Assert-True ($r.Status -eq 200) "warden can open $path"
    }
    $r = Http-Get $warden.Jar "/viewHostel"
    Assert-True ($r.Body -notmatch "/getHostelForUpdate" -and $r.Body -notmatch "/deleteHostel") "warden does not see hostel actions"
    $r = Http-Get $warden.Jar "/viewRoom"
    Assert-True ($r.Body -notmatch "/getRoomForUpdate" -and $r.Body -notmatch "/deleteRoom") "warden does not see room actions"
    if ($testHostelId) {
        $r = Http-Post $warden.Jar "/getHostelForUpdate" ([ordered]@{
            _csrf    = $warden.Token
            hostelId = $testHostelId
        })
        Assert-True ($r.Status -eq 403) "warden cannot load the hostel update form"
    }
    foreach ($path in @("/addHostel.jsp", "/updateHostel.jsp", "/deleteHostel",
                        "/deleteRoom", "/admin/users")) {
        $r = Http-Get $warden.Jar $path
        Assert-True ($r.Status -eq 403) "warden is denied $path"
    }
    $r = Http-Post $warden.Jar "/saveHostel" ([ordered]@{
        _csrf       = $warden.Token
        hostelName  = "TEST-RBAC-DENIED"
        totalFloors = 1
    })
    Assert-True ($r.Status -eq 403) "warden cannot create hostels"
    $r = Http-Post $warden.Jar "/deleteHostel" ([ordered]@{
        _csrf    = $warden.Token
        hostelId = 7
    })
    Assert-True ($r.Status -eq 403) "warden cannot delete hostels"

    $r = Http-Get $warden.Jar "/api/rooms"
    Assert-True ($r.Status -eq 200) "warden can read rooms through REST API"
    $rooms = @($r.Body | ConvertFrom-Json | ForEach-Object { $_ })
    if ($rooms.Count -gt 0) {
        $room = $rooms[0]
        $r = Http-Post $warden.Jar "/updateRoom" ([ordered]@{
            _csrf       = $warden.Token
            roomId      = $room.roomId
            roomNumber  = $room.roomNumber
            capacity    = $room.capacity
            monthlyFee  = $room.monthlyFee
            status      = $room.status
            hostelId    = $room.hostel.hostelId
        })
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Room Updated Successfully") "warden can update a room [status=$($r.Status); id=$($room.roomId); room=$($room.roomNumber); hostel=$($room.hostel.hostelId)] [$(Get-Snippet $r.Body)]"
    }

    $testRoomNumber = "TEST-RBAC-" + (Get-Date -Format "HHmmss")
    $r = Http-Post $warden.Jar "/saveRoom" ([ordered]@{
        _csrf       = $warden.Token
        roomNumber  = $testRoomNumber
        capacity    = 2
        monthlyFee  = 1500
        status      = "AVAILABLE"
        hostelId    = $testHostelId
    })
    Assert-True ($r.Status -eq 200 -and $r.Body -match "Room Saved Successfully") "warden can create a room"

    $r = Http-Get $warden.Jar "/api/rooms"
    $testRoom = @($r.Body | ConvertFrom-Json | ForEach-Object { $_ } | Where-Object { $_.roomNumber -eq $testRoomNumber })
    Assert-True ($testRoom.Count -eq 1) "warden-created room is visible"
    $testRoomId = if ($testRoom.Count -eq 1) { $testRoom[0].roomId } else { "" }

    if ($testHostelId) {
        $r = Http-Post $admin.Jar "/deleteHostel" ([ordered]@{
            hostelId = $testHostelId
            returnTo = "viewHostel"
        })
        Assert-True ($r.Status -eq 403) "hostel delete requires a CSRF token"
        $r = Http-Post $admin.Jar "/deleteHostel" ([ordered]@{
            _csrf     = $admin.Token
            hostelId  = $testHostelId
            returnTo  = "viewHostel"
        })
        Assert-True ($r.Status -eq 302 -and $r.Redirect -match "error=hasRooms") "hostel delete is refused while rooms exist"
        $r = Http-Get $admin.Jar "/viewHostel?error=hasRooms&hostelId=$testHostelId"
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Cannot Delete Hostel" -and $r.Body -match "still has rooms assigned") "blocked hostel delete shows a clear directory error"
        Assert-True ($r.Body -match "TEST-RBAC-HOSTEL-UPDATED") "blocked hostel delete preserves the hostel record"
    }

    Write-Host "`nAdmin cleanup and account controls" -ForegroundColor Cyan
    if ($testRoomId) {
        $r = Http-Post $admin.Jar "/deleteRoom" ([ordered]@{
            _csrf  = $admin.Token
            roomId = $testRoomId
        })
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Room Deleted Successfully") "admin can delete a room [status=$($r.Status); id=$testRoomId] [$(Get-Snippet $r.Body)]"
        $r = Http-Get $admin.Jar "/api/rooms/$testRoomId"
        Assert-True ($r.Status -eq 404) "deleted room is gone from REST API [status=$($r.Status)] [$(Get-Snippet $r.Body)]"
    }
    if ($testHostelId) {
        $r = Http-Post $admin.Jar "/deleteHostel" ([ordered]@{
            _csrf    = $admin.Token
            hostelId = $testHostelId
            returnTo = "viewHostel"
        })
        Assert-True ($r.Status -eq 302 -and $r.Redirect -match "success=deleted") "admin can delete an empty hostel [status=$($r.Status); id=$testHostelId] [$(Get-Snippet $r.Body)]"
        $r = Http-Get $admin.Jar "/viewHostel?success=deleted&hostelId=$testHostelId"
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Hostel Deleted Successfully" -and $r.Body -notmatch "TEST-RBAC-HOSTEL-UPDATED") "deleted hostel shows success and leaves the directory"
        $r = Http-Get $admin.Jar "/api/hostels/$testHostelId"
        Assert-True ($r.Status -eq 404) "deleted hostel is gone from REST API [status=$($r.Status)] [$(Get-Snippet $r.Body)]"
    }

    $usersPage = Http-Get $admin.Jar "/admin/users"
    $viewerRow = [regex]::Match($usersPage.Body,
        '<td><span class="badge badge-blue">#(\d+)</span></td>\s*<td><b>' + [regex]::Escape($ViewerUser) + '</b>')
    Assert-True ($viewerRow.Success) "viewer account is listed for admin"
    if ($viewerRow.Success) {
        $viewerId = $viewerRow.Groups[1].Value
        $r = Http-Post $admin.Jar "/admin/users" ([ordered]@{
            _csrf  = $admin.Token
            action = "toggle"
            userId = $viewerId
            active = "false"
        })
        Assert-True ($r.Status -eq 302 -and $r.Redirect -match "msg=deactivated") "admin can deactivate viewer"
        $blockedJar = New-Jar
        $page = Http-Get $blockedJar "/login"
        $blockedToken = Get-CsrfToken $page.Body
        $r = Http-Post $blockedJar "/login" ([ordered]@{
            _csrf    = $blockedToken
            username = $ViewerUser
            password = $ViewerPassword
        })
        Assert-True ($r.Status -eq 200 -and $r.Body -match "Invalid username or password") "inactive viewer cannot sign in"
        $r = Http-Post $admin.Jar "/admin/users" ([ordered]@{
            _csrf  = $admin.Token
            action = "toggle"
            userId = $viewerId
            active = "true"
        })
        Assert-True ($r.Status -eq 302 -and $r.Redirect -match "msg=activated") "admin can reactivate viewer"

        $adminRow = [regex]::Match($usersPage.Body,
            '<td><span class="badge badge-blue">#(\d+)</span></td>\s*<td><b>' + [regex]::Escape($AdminUser) + '</b>')
        if ($adminRow.Success) {
            $self = Http-Post $admin.Jar "/admin/users" ([ordered]@{
                _csrf  = $admin.Token
                action = "toggle"
                userId = $adminRow.Groups[1].Value
                active = "false"
            })
            Assert-True ($self.Status -eq 302 -and $self.Redirect -match "msg=self") "admin cannot deactivate own account"
        } else {
            Assert-True $false "admin account row is listed"
        }
    }

    Write-Host "`nCSRF, logout and login hardening" -ForegroundColor Cyan
    $r = Http-Get $admin.Jar "/logout"
    Assert-True ($r.Status -eq 405) "authenticated GET logout returns 405"
    $r = Http-Post $admin.Jar "/logout" ([ordered]@{})
    Assert-True ($r.Status -eq 403) "logout requires a CSRF token"
    $r = Http-Post $admin.Jar "/logout" ([ordered]@{ _csrf = $admin.Token })
    Assert-True ($r.Status -eq 302 -and $r.Redirect -match "/login") "logout succeeds with CSRF token"
    $script:AdminSessionActive = $false
    $r = Http-Get $admin.Jar "/index.jsp"
    Assert-True ($r.Status -eq 302 -and $r.Redirect -like "*/login") "session is invalid after logout"
    $r = Http-Get $admin.Jar "/api/hostels"
    Assert-True ($r.Status -eq 401) "REST API is unauthorized after logout"

    $unknownJar = New-Jar
    $page = Http-Get $unknownJar "/login"
    $token = Get-CsrfToken $page.Body
    $r = Http-Post $unknownJar "/login" ([ordered]@{
        _csrf    = $token
        username = "no_such_user_" + (Get-Date -Format "HHmmss")
        password = "irrelevant1"
    })
    Assert-True ($r.Status -eq 200 -and $r.Body -match "Invalid username or password") "unknown user gets a generic login error"
}
finally {
    try {
        if ($script:AdminSessionActive -and $script:AdminJar -and $script:AdminToken) {
            Cleanup-TestData $script:AdminJar $script:AdminToken
        } else {
            $rescueJar = New-Jar
            $page = Http-Get $rescueJar "/login"
            $token = Get-CsrfToken $page.Body
            $result = Http-Post $rescueJar "/login" ([ordered]@{
                _csrf    = $token
                username = $AdminUser
                password = $AdminPassword
            })
            if ($result.Status -eq 302) {
                Cleanup-TestData $rescueJar $token
            }
        }
    } catch {
        Write-Host "Cleanup warning: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    Remove-Item -Recurse -Force -LiteralPath $script:Temp -ErrorAction SilentlyContinue
}

Write-Host "`nResults: $script:Pass passed, $script:Fail failed." -ForegroundColor Cyan
if ($script:Fail -gt 0) {
    exit 1
}
exit 0

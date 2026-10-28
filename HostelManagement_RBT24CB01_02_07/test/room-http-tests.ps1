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
$script:Temp = Join-Path $env:TEMP ("hostel-room-tests-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $script:Temp | Out-Null
$script:Pass = 0
$script:Fail = 0
$script:TestPrefix = "TEST-ROOM-"
$script:Admin = $null
$script:Rooms = @()

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
    $result = Http-Post $jar "/login" ([ordered]@{
        _csrf    = $token
        username = $user
        password = $password
    })
    Assert-True ($result.Status -eq 302 -and $result.Redirect -like "*/index.jsp") "login succeeds for $user"
    return [pscustomobject]@{ Jar = $jar; Token = $token }
}

function Get-FindToken([string]$jar) {
    $page = Http-Get $jar "/findRoom"
    Assert-True ($page.Status -eq 200) "room search page is available"
    return Get-CsrfToken $page.Body
}

function Get-ManagePage([string]$jar, [string]$status) {
    return Http-Get $jar ("/manageRoomRequests?status=" + $status)
}

function Get-RequestRow([string]$jar, [string]$status, [string]$roomNumber, [string]$username) {
    $page = Get-ManagePage $jar $status
    $csrf = Get-CsrfToken $page.Body
    $roomPattern = [regex]::Escape("Room " + $roomNumber)
    $userPattern = [regex]::Escape("<b>" + $username + "</b>")
    foreach ($block in ($page.Body -split "<tr>")) {
        if ($block -match $roomPattern -and $block -match $userPattern) {
            $idMatch = [regex]::Match($block, 'name="requestId" value="(\d+)"')
            if ($idMatch.Success) {
                return [pscustomobject]@{
                    Id   = [int]$idMatch.Groups[1].Value
                    Csrf = $csrf
                }
            }
        }
    }
    return $null
}

function Get-AvailableRooms([string]$jar, [string]$query) {
    $result = Http-Get $jar ("/api/available-rooms" + $query)
    if ($result.Status -ne 200) {
        return [pscustomobject]@{ Status = $result.Status; Rooms = @() }
    }
    $rooms = @($result.Body | ConvertFrom-Json | ForEach-Object { $_ })
    return [pscustomobject]@{ Status = $result.Status; Rooms = $rooms }
}

function Create-TestRoom([string]$jar, [string]$token, [int]$hostelId,
                         [string]$roomNumber, [int]$capacity, [double]$fee) {
    $result = Http-Post $jar "/saveRoom" ([ordered]@{
        _csrf      = $token
        roomNumber = $roomNumber
        capacity   = $capacity
        monthlyFee = $fee
        status     = "AVAILABLE"
        hostelId   = $hostelId
    })
    Assert-True ($result.Status -eq 200 -and $result.Body -match "Room Saved Successfully") "test room $roomNumber is created"
    $match = [regex]::Match($result.Body, 'Room <b>#(\d+)')
    if ($match.Success) {
        $script:Rooms += [int]$match.Groups[1].Value
        return [int]$match.Groups[1].Value
    }
    return 0
}

function Suggest-Room([string]$jar, [int]$roomId) {
    $token = Get-FindToken $jar
    return Http-Post $jar "/requestRoom" ([ordered]@{
        _csrf  = $token
        roomId = $roomId
    })
}

function Manage-Request([string]$jar, [int]$requestId, [string]$action) {
    $page = Get-ManagePage $jar "PENDING"
    $token = Get-CsrfToken $page.Body
    return Http-Post $jar "/manageRoomRequests" ([ordered]@{
        _csrf     = $token
        requestId = $requestId
        action    = $action
    })
}

function Cleanup-TestData([string]$jar) {
    $roomsResult = Http-Get $jar "/api/rooms"
    if ($roomsResult.Status -ne 200) { return }
    $testRooms = @($roomsResult.Body | ConvertFrom-Json |
        ForEach-Object { $_ } |
        Where-Object { "$($_.roomNumber)".StartsWith($script:TestPrefix) })
    if ($testRooms.Count -eq 0) { return }

    $manage = Get-ManagePage $jar "APPROVED"
    $token = Get-CsrfToken $manage.Body
    foreach ($block in ($manage.Body -split "<tr>")) {
        if ($block -match [regex]::Escape($script:TestPrefix)) {
            $match = [regex]::Match($block, 'name="requestId" value="(\d+)"')
            if ($match.Success) {
                Http-Post $jar "/manageRoomRequests" ([ordered]@{
                    _csrf    = $token
                    requestId = [int]$match.Groups[1].Value
                    action   = "release"
                }) | Out-Null
            }
        }
    }

    foreach ($room in $testRooms) {
        Http-Post $jar "/deleteRoom" ([ordered]@{
            _csrf  = $token
            roomId = $room.roomId
        }) | Out-Null
    }
}

try {
    Write-Host "`nRoom search and filters" -ForegroundColor Cyan
    $script:Admin = Login $AdminUser $AdminPassword
    Cleanup-TestData $script:Admin.Jar

    $hostels = @((Http-Get $script:Admin.Jar "/api/hostels").Body |
        ConvertFrom-Json | ForEach-Object { $_ })
    Assert-True ($hostels.Count -gt 0) "at least one hostel exists for test rooms"
    $hostelId = if ($hostels.Count -gt 0) { [int]$hostels[0].hostelId } else { 0 }

    $stamp = Get-Date -Format "HHmmss"
    $roomANumber = $script:TestPrefix + "A-" + $stamp
    $roomBNumber = $script:TestPrefix + "B-" + $stamp
    $roomCNumber = $script:TestPrefix + "C-" + $stamp
    $roomA = Create-TestRoom $script:Admin.Jar $script:Admin.Token $hostelId $roomANumber 1 1200
    $roomB = Create-TestRoom $script:Admin.Jar $script:Admin.Token $hostelId $roomBNumber 4 7200
    $roomC = Create-TestRoom $script:Admin.Jar $script:Admin.Token $hostelId $roomCNumber 1 3100
    Assert-True ($roomA -gt 0 -and $roomB -gt 0 -and $roomC -gt 0) "three test rooms were created"

    $page = Http-Get $script:Admin.Jar ("/findRoom?hostelId=" + $hostelId + "&minFee=1000&maxFee=2000&minCapacity=1&roomNumber=" + $roomANumber)
    Assert-True ($page.Status -eq 200 -and $page.Body -match ("<h4>Room " + [regex]::Escape($roomANumber) + "</h4>")) "hostel, budget, capacity, and room number filters return the budget room"
    Assert-True (-not ($page.Body -match ("<h4>Room " + [regex]::Escape($roomBNumber) + "</h4>"))) "budget filter excludes the expensive room"
    $page = Http-Get $script:Admin.Jar ("/findRoom?hostelId=" + $hostelId + "&minFee=7000&minCapacity=3&roomNumber=" + $roomBNumber)
    Assert-True ($page.Status -eq 200 -and $page.Body -match ("<h4>Room " + [regex]::Escape($roomBNumber) + "</h4>")) "higher budget and capacity filters return the larger room"
    $page = Http-Get $script:Admin.Jar ("/findRoom?minFee=not-a-number")
    Assert-True ($page.Status -eq 200 -and $page.Body -match "must be a number") "invalid numeric filter is rejected with a message"

    $page = Http-Get $script:Admin.Jar ("/roomDetails?roomId=" + $roomA)
    Assert-True ($page.Status -eq 200 -and $page.Body -match ("Room " + [regex]::Escape($roomANumber)) -and $page.Body -match "1200.00") "room details show the selected room and fee"
    $page = Http-Get $script:Admin.Jar "/roomDetails?roomId=999999999"
    Assert-True ($page.Status -eq 404) "nonexistent room details return 404"

    $api = Get-AvailableRooms $script:Admin.Jar ("?hostelId=" + $hostelId + "&roomNumber=" + $roomANumber)
    Assert-True ($api.Status -eq 200 -and $api.Rooms.Count -eq 1 -and "$($api.Rooms[0].room.roomNumber)" -eq $roomANumber) "REST availability search returns the matching room"
    $api = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomANumber + "&maxFee=1000")
    Assert-True ($api.Status -eq 200 -and $api.Rooms.Count -eq 0) "REST budget filter excludes the room"
    $badApi = Http-Get $script:Admin.Jar "/api/available-rooms?minCapacity=bad"
    Assert-True ($badApi.Status -eq 400) "REST invalid capacity returns 400"
    $anonJar = New-Jar
    $anonApi = Http-Get $anonJar "/api/available-rooms"
    Assert-True ($anonApi.Status -eq 401) "anonymous REST availability request returns 401"

    $viewer = Login $ViewerUser $ViewerPassword
    $warden = Login $WardenUser $WardenPassword

    Write-Host "`nRoom requests, ownership, and approval" -ForegroundColor Cyan
    $result = Http-Post $viewer.Jar "/requestRoom" ([ordered]@{ roomId = $roomA })
    Assert-True ($result.Status -eq 403) "request submission without CSRF is rejected"
    $result = Suggest-Room $viewer.Jar 999999999
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=notfound") "request for a nonexistent room is rejected"
    $result = Suggest-Room $viewer.Jar $roomA
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "viewer submits a pending request"
    $result = Suggest-Room $viewer.Jar $roomA
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=duplicate") "duplicate pending request is rejected"
    $result = Get-RequestRow $script:Admin.Jar "PENDING" $roomANumber $ViewerUser
    Assert-True ($null -ne $result) "pending request is visible to the reviewer"
    $viewerRequest = if ($null -ne $result) { $result.Id } else { 0 }

    $result = Suggest-Room $warden.Jar $roomA
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "warden submits a second pending request"
    $result = Get-RequestRow $script:Admin.Jar "PENDING" $roomANumber $WardenUser
    Assert-True ($null -ne $result) "warden request is visible to the reviewer"
    $wardenRequest = if ($null -ne $result) { $result.Id } else { 0 }

    $result = Manage-Request $warden.Jar $wardenRequest "approve"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=self") "warden cannot approve their own request"
    $result = Http-Post $viewer.Jar "/cancelRoomRequest" ([ordered]@{
        _csrf     = $viewer.Token
        requestId = $wardenRequest
    })
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=notfound") "viewer cannot cancel another user's request"
    $result = Http-Post $viewer.Jar "/manageRoomRequests" ([ordered]@{
        _csrf     = $viewer.Token
        requestId = $viewerRequest
        action    = "approve"
    })
    Assert-True ($result.Status -eq 403) "viewer cannot approve a room request"
    $result = Http-Post $viewer.Jar "/manageRoomRequests" ([ordered]@{
        _csrf     = $viewer.Token
        requestId = $viewerRequest
        action    = "release"
    })
    Assert-True ($result.Status -eq 403) "viewer cannot release an allocation"

    $result = Manage-Request $script:Admin.Jar $wardenRequest "approve"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=approved") "admin approves the first request for the capacity-1 room"
    $room = Http-Get $script:Admin.Jar ("/api/rooms/" + $roomA)
    Assert-True ($room.Status -eq 200 -and $room.Body -match '"status":"OCCUPIED"') "approved room becomes occupied"
    $all = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomANumber + "&availableOnly=false")
    Assert-True ($all.Status -eq 200 -and $all.Rooms.Count -eq 1 -and "$($all.Rooms[0].activeAllocations)" -eq "1") "tracked allocation count appears in REST availability data"
    $available = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomANumber)
    Assert-True ($available.Status -eq 200 -and $available.Rooms.Count -eq 0) "occupied capacity-1 room is excluded from available-only search"
    $page = Http-Get $script:Admin.Jar ("/findRoom?roomNumber=" + $roomANumber + "&availability=all")
    Assert-True ($page.Status -eq 200 -and $page.Body -match ("<h4>Room " + [regex]::Escape($roomANumber) + "</h4>")) "all-rooms search still shows the occupied room"

    $result = Manage-Request $script:Admin.Jar $viewerRequest "approve"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=unavailable") "second approval for full room is rejected without overbooking"
    $viewerPending = Get-RequestRow $script:Admin.Jar "PENDING" $roomANumber $ViewerUser
    Assert-True ($null -ne $viewerPending) "competing request remains pending while capacity is full"

    $result = Manage-Request $script:Admin.Jar $wardenRequest "release"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=released") "admin releases the allocation"
    $all = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomANumber + "&availableOnly=false")
    Assert-True ($all.Status -eq 200 -and $all.Rooms.Count -eq 1 -and "$($all.Rooms[0].activeAllocations)" -eq "0") "released allocation no longer counts against capacity"
    $room = Http-Get $script:Admin.Jar ("/api/rooms/" + $roomA)
    Assert-True ($room.Status -eq 200 -and $room.Body -match '"status":"AVAILABLE"') "released room becomes available again"
    $result = Manage-Request $script:Admin.Jar $viewerRequest "approve"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=approved") "the remaining pending request can be approved after release"
    $result = Http-Post $viewer.Jar "/cancelRoomRequest" ([ordered]@{
        _csrf     = $viewer.Token
        requestId = $viewerRequest
    })
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=conflict") "an approved request cannot be cancelled by its owner"
    $result = Manage-Request $script:Admin.Jar $viewerRequest "release"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=released") "second allocation is released cleanly"

    Write-Host "`nCancellation and rejection rules" -ForegroundColor Cyan
    $result = Suggest-Room $viewer.Jar $roomB
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "viewer requests the larger room"
    $viewerB = Get-RequestRow $script:Admin.Jar "PENDING" $roomBNumber $ViewerUser
    Assert-True ($null -ne $viewerB) "larger-room request is visible"
    $result = Http-Post $viewer.Jar "/cancelRoomRequest" ([ordered]@{
        _csrf     = $viewer.Token
        requestId = $viewerB.Id
    })
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=cancelled") "owner cancels a pending request"
    $page = Http-Get $viewer.Jar "/myRoomRequests"
    Assert-True ($page.Status -eq 200 -and $page.Body -match "CANCELLED") "cancelled request appears in the owner's history"

    $result = Suggest-Room $warden.Jar $roomB
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "warden requests the larger room"
    $wardenB = Get-RequestRow $script:Admin.Jar "PENDING" $roomBNumber $WardenUser
    Assert-True ($null -ne $wardenB) "warden larger-room request is visible"
    $result = Manage-Request $script:Admin.Jar $wardenB.Id "reject"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=rejected") "admin rejects a pending request"
    $result = Manage-Request $script:Admin.Jar $wardenB.Id "approve"
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "error=conflict") "rejected request cannot be approved later"

    Write-Host "`nConcurrent capacity enforcement" -ForegroundColor Cyan
    $result = Suggest-Room $viewer.Jar $roomC
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "viewer requests the concurrency-test room"
    $result = Suggest-Room $warden.Jar $roomC
    Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=requested") "warden requests the concurrency-test room"
    $viewerC = Get-RequestRow $script:Admin.Jar "PENDING" $roomCNumber $ViewerUser
    $wardenC = Get-RequestRow $script:Admin.Jar "PENDING" $roomCNumber $WardenUser
    Assert-True ($null -ne $viewerC -and $null -ne $wardenC) "both concurrency requests are pending"
    $admin2 = Login $AdminUser $AdminPassword
    $adminPage = Get-ManagePage $script:Admin.Jar "PENDING"
    $adminPage2 = Get-ManagePage $admin2.Jar "PENDING"
    $adminToken = Get-CsrfToken $adminPage.Body
    $adminToken2 = Get-CsrfToken $adminPage2.Body
    $out1 = Join-Path $script:Temp "approve-1.txt"
    $out2 = Join-Path $script:Temp "approve-2.txt"
    $curl = (Get-Command curl.exe).Source
    $args1 = @(
        "-s", "-b", $script:Admin.Jar, "-c", $script:Admin.Jar,
        "-o", "NUL", "-w", "%{http_code}|%{redirect_url}",
        "--data-urlencode", "_csrf=$adminToken",
        "--data-urlencode", "requestId=$($viewerC.Id)",
        "--data-urlencode", "action=approve",
        ($BaseUrl + "/manageRoomRequests")
    )
    $args2 = @(
        "-s", "-b", $admin2.Jar, "-c", $admin2.Jar,
        "-o", "NUL", "-w", "%{http_code}|%{redirect_url}",
        "--data-urlencode", "_csrf=$adminToken2",
        "--data-urlencode", "requestId=$($wardenC.Id)",
        "--data-urlencode", "action=approve",
        ($BaseUrl + "/manageRoomRequests")
    )
    $p1 = Start-Process -FilePath $curl -ArgumentList $args1 -NoNewWindow -PassThru -RedirectStandardOutput $out1
    $p2 = Start-Process -FilePath $curl -ArgumentList $args2 -NoNewWindow -PassThru -RedirectStandardOutput $out2
    $p1.WaitForExit()
    $p2.WaitForExit()
    $approve1 = if (Test-Path -LiteralPath $out1) { Get-Content -Raw -LiteralPath $out1 } else { "" }
    $approve2 = if (Test-Path -LiteralPath $out2) { Get-Content -Raw -LiteralPath $out2 } else { "" }
    $successes = @(@($approve1, $approve2) | Where-Object { $_ -match "success=approved" }).Count
    $failures = @(@($approve1, $approve2) | Where-Object { $_ -match "error=unavailable|error=conflict" }).Count
    Assert-True ($successes -eq 1 -and $failures -eq 1) "concurrent approvals allow exactly one allocation [first=$(Get-Snippet $approve1); second=$(Get-Snippet $approve2)]"
    $all = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomCNumber + "&availableOnly=false")
    Assert-True ($all.Status -eq 200 -and $all.Rooms.Count -eq 1 -and "$($all.Rooms[0].activeAllocations)" -eq "1") "concurrency test leaves exactly one tracked allocation"
    $page = Http-Get $script:Admin.Jar ("/roomDetails?roomId=" + $roomC)
    Assert-True ($page.Status -eq 200 -and $page.Body -match "Availability") "occupied concurrency room details still render"

    Write-Host "`nRoom deletion guard and existing CRUD" -ForegroundColor Cyan
    $delete = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        _csrf  = $script:Admin.Token
        roomId = $roomC
    })
    Assert-True ($delete.Status -eq 200 -and $delete.Body -match "Cannot delete this room") "room with an active allocation cannot be deleted"
    $roomPage = Http-Get $script:Admin.Jar "/api/rooms"
    Assert-True ($roomPage.Status -eq 200 -and $roomPage.Body -match "roomId") "existing room REST endpoint still works"
    $hostelPage = Http-Get $script:Admin.Jar "/api/hostels"
    Assert-True ($hostelPage.Status -eq 200 -and $hostelPage.Body -match "hostelId") "existing hostel REST endpoint still works"
    $viewPage = Http-Get $script:Admin.Jar "/viewRoom"
    Assert-True ($viewPage.Status -eq 200) "existing room list page still works"
    Assert-True ($viewPage.Body -match ">Actions</th>") "admin sees room table actions"
    Assert-True ($viewPage.Body -match "/getRoomForUpdate" -and $viewPage.Body -match "/deleteRoom") "admin sees room update and delete buttons"
    Assert-True ($viewPage.Body -match 'name="returnTo" value="viewRoom"') "room delete action returns to the directory"
    Assert-True ($viewPage.Body -match "Are you sure you want to delete this room") "room delete action asks for confirmation"

    $updatePage = Http-Post $script:Admin.Jar "/getRoomForUpdate" ([ordered]@{
        _csrf  = $script:Admin.Token
        roomId = $roomA
    })
    Assert-True ($updatePage.Status -eq 200 -and ($updatePage.Body -match ('name="roomId" value="' + $roomA + '"')) -and $updatePage.Body -match "Save Changes") "room update action pre-fills the existing form"

    $csrfGuard = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        roomId   = $roomC
        returnTo = "viewRoom"
    })
    Assert-True ($csrfGuard.Status -eq 403) "room delete requires a CSRF token"

    $guard = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        _csrf    = $script:Admin.Token
        roomId   = $roomC
        returnTo = "viewRoom"
    })
    Assert-True ($guard.Status -eq 302 -and $guard.Redirect -match "error=hasAllocations" -and $guard.Redirect -match "roomId=$roomC") "room delete guard redirects to the directory"
    $guardPage = Http-Get $script:Admin.Jar "/viewRoom?error=hasAllocations&roomId=$roomC"
    Assert-True ($guardPage.Status -eq 200 -and $guardPage.Body -match "Cannot Delete Room" -and $guardPage.Body -match "active allocations") "room directory shows the allocation guard message"

    $roomDNumber = $script:TestPrefix + "D-" + $stamp
    $roomD = Create-TestRoom $script:Admin.Jar $script:Admin.Token $hostelId $roomDNumber 1 1100
    $delete = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        _csrf    = $script:Admin.Token
        roomId   = $roomD
        returnTo = "viewRoom"
    })
    Assert-True ($delete.Status -eq 302 -and $delete.Redirect -match "success=deleted" -and $delete.Redirect -match "roomId=$roomD") "successful room delete redirects to the directory"
    $flashPage = Http-Get $script:Admin.Jar "/viewRoom?success=deleted&roomId=$roomD"
    Assert-True ($flashPage.Status -eq 200 -and $flashPage.Body -match "Room Deleted Successfully") "room directory shows the delete success message"
    $deletedRoom = Get-AvailableRooms $script:Admin.Jar ("?roomNumber=" + $roomDNumber + "&availableOnly=false")
    Assert-True ($deletedRoom.Status -eq 200 -and $deletedRoom.Rooms.Count -eq 0) "deleted room is removed from the inventory"

    $invalidDelete = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        _csrf    = $script:Admin.Token
        roomId   = "not-a-number"
        returnTo = "viewRoom"
    })
    Assert-True ($invalidDelete.Status -eq 302 -and $invalidDelete.Redirect -match "error=invalid") "invalid room delete redirects with an error"

    $missingDelete = Http-Post $script:Admin.Jar "/deleteRoom" ([ordered]@{
        _csrf    = $script:Admin.Token
        roomId   = 999999999
        returnTo = "viewRoom"
    })
    Assert-True ($missingDelete.Status -eq 302 -and $missingDelete.Redirect -match "error=notfound") "missing room delete redirects with an error"

    $approvedC = Get-RequestRow $script:Admin.Jar "APPROVED" $roomCNumber $ViewerUser
    if ($null -eq $approvedC) {
        $approvedC = Get-RequestRow $script:Admin.Jar "APPROVED" $roomCNumber $WardenUser
    }
    Assert-True ($null -ne $approvedC) "concurrency allocation request can be found for release"
    if ($null -ne $approvedC) {
        $result = Http-Post $script:Admin.Jar "/manageRoomRequests" ([ordered]@{
            _csrf     = $approvedC.Csrf
            requestId = $approvedC.Id
            action    = "release"
        })
        Assert-True ($result.Status -eq 302 -and $result.Redirect -match "success=released") "concurrency allocation is released"
    }

    Cleanup-TestData $script:Admin.Jar
    $remaining = @((Http-Get $script:Admin.Jar "/api/rooms").Body |
        ConvertFrom-Json | ForEach-Object { $_ } |
        Where-Object { "$($_.roomNumber)".StartsWith($script:TestPrefix) })
    Assert-True ($remaining.Count -eq 0) "test rooms are removed"
}
finally {
    try {
        if ($null -eq $script:Admin) {
            $rescue = Login $AdminUser $AdminPassword
            Cleanup-TestData $rescue.Jar
        } else {
            Cleanup-TestData $script:Admin.Jar
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

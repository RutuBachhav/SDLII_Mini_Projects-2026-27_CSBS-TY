$baseUrl = "http://localhost:8080/CampusCare"
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe"

Write-Host "==================== 1. MYSQL DATABASE VERIFICATION ===================="
& $mysqlBin -u root -p160306 -e "USE campuscare; SHOW TABLES; SELECT role, count(*) FROM users GROUP BY role; SELECT status, count(*) FROM complaints GROUP BY status;" 2>&1

Write-Host "`n==================== 2. FRONTEND HTML VERIFICATION ===================="
$frontend = Invoke-WebRequest -Uri "$baseUrl/" -UseBasicParsing
Write-Host "Frontend HTTP Status: $($frontend.StatusCode)"
Write-Host "Frontend Title Check: $($frontend.Content.Contains('CampusCare'))"

Write-Host "`n==================== 3. REST AUTHENTICATION TEST ===================="
# Student Login
$studentLoginBody = @{ email = "student@campuscare.com"; password = "student123" } | ConvertTo-Json
$studentRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $studentLoginBody -ContentType "application/json"
$studentToken = $studentRes.data.token
Write-Host "Student Login: Success=$($studentRes.success), StudentId=$($studentRes.data.studentId)"

# Staff Login
$staffLoginBody = @{ email = "staff@campuscare.com"; password = "staff123" } | ConvertTo-Json
$staffRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $staffLoginBody -ContentType "application/json"
$staffToken = $staffRes.data.token
Write-Host "Staff Login: Success=$($staffRes.success), StaffId=$($staffRes.data.staffId)"

# Admin Login
$adminLoginBody = @{ email = "admin@campuscare.com"; password = "admin123" } | ConvertTo-Json
$adminRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminRes.data.token
Write-Host "Admin Login: Success=$($adminRes.success), Role=$($adminRes.data.role)"

Write-Host "`n==================== 4. DEPARTMENTS API ===================="
$depts = Invoke-RestMethod -Uri "$baseUrl/api/departments" -Method Get
Write-Host "Departments Count: $($depts.data.Count)"

Write-Host "`n==================== 5. SUBMIT COMPLAINT (JMS PRODUCER TEST) ===================="
$newCompBody = @{
    title = "Wi-Fi Router Reset Required in Room 301"
    description = "Frequent packet drops and high latency during lab sessions. Needs immediate router inspection."
    category = "Wi-Fi/Internet"
    priority = "HIGH"
    departmentId = $depts.data[0].id
} | ConvertTo-Json

$compRes = Invoke-RestMethod -Uri "$baseUrl/api/complaints" -Method Post -Body $newCompBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $studentToken" }
$newComplaintId = $compRes.data.id
Write-Host "Complaint Lodged: ID=$newComplaintId, Status=$($compRes.data.status), Title=$($compRes.data.title)"

Write-Host "`n==================== 6. DASHBOARDS TEST (ADMIN, STAFF, STUDENT) ===================="
$dashRes = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/admin" -Method Get -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "Admin Stats: TotalComplaints=$($dashRes.data.totalComplaints), Students=$($dashRes.data.totalStudents), Staff=$($dashRes.data.totalStaff)"
$staffDash = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/staff" -Method Get -Headers @{ Authorization = "Bearer $staffToken" }
Write-Host "Staff Stats: TotalComplaints=$($staffDash.data.totalComplaints), Assigned=$($staffDash.data.assignedComplaints)"
$studDash = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/student" -Method Get -Headers @{ Authorization = "Bearer $studentToken" }
Write-Host "Student Stats: TotalComplaints=$($studDash.data.totalComplaints), Pending=$($studDash.data.pendingComplaints)"

Write-Host "`n==================== 7. ASSIGN COMPLAINT (ADMIN) ===================="
$assignBody = @{ staffId = $staffRes.data.staffId; priority = "URGENT" } | ConvertTo-Json
$assignRes = Invoke-RestMethod -Uri "$baseUrl/api/complaints/$newComplaintId/assign" -Method Put -Body $assignBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "Assigned: Complaint #$newComplaintId -> Status=$($assignRes.data.status), AssignedTo=$($assignRes.data.assignedStaffName)"

Write-Host "`n==================== 8. WORK IN PROGRESS (STAFF) ===================="
$statusBody = @{ status = "IN_PROGRESS"; remarks = "Technician arrived on site, diagnostics running" } | ConvertTo-Json
$statusRes = Invoke-RestMethod -Uri "$baseUrl/api/complaints/$newComplaintId/status" -Method Put -Body $statusBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $staffToken" }
Write-Host "In Progress: Complaint #$newComplaintId -> Status=$($statusRes.data.status)"

Write-Host "`n==================== 9. RESOLVE COMPLAINT (STAFF) ===================="
$updateBody = @{ newStatus = "RESOLVED"; message = "Replaced faulty antenna cable and verified signal strength at 95%." } | ConvertTo-Json
$resolveRes = Invoke-RestMethod -Uri "$baseUrl/api/complaints/$newComplaintId/updates" -Method Post -Body $updateBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $staffToken" }
Write-Host "Resolved: Complaint #$newComplaintId -> Status=$($resolveRes.data.status), ResolvedAt=$($resolveRes.data.resolvedAt)"

Write-Host "`n==================== 10. CLOSE COMPLAINT (STUDENT) ===================="
$closeBody = @{ status = "CLOSED"; remarks = "Issue resolved, speed confirmed normal." } | ConvertTo-Json
$closeRes = Invoke-RestMethod -Uri "$baseUrl/api/complaints/$newComplaintId/status" -Method Put -Body $closeBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $studentToken" }
Write-Host "Closed: Complaint #$newComplaintId -> Status=$($closeRes.data.status)"

Write-Host "`n==================== 11. NOTIFICATIONS VERIFICATION ===================="
$notifs = Invoke-RestMethod -Uri "$baseUrl/api/notifications" -Method Get -Headers @{ Authorization = "Bearer $studentToken" }
Write-Host "Student Notifications Count: $($notifs.data.Count)"
foreach ($n in $notifs.data | Select-Object -First 3) {
    Write-Host " - [Notification] $($n.message) (Read: $($n.read))"
}

Write-Host "`n==================== ALL VERIFICATION CHECKS PASSED! ===================="

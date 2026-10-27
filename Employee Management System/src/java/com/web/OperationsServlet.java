package com.web;

import com.util.Ctx;
import com.util.Db;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;

/** POST /ems/operations : QR, attendance, leave, payroll, payslip, notifications */
@WebServlet("/ems/operations")
public class OperationsServlet extends BaseServlet {

    @Override
    protected Map<String, Object> handle(Ctx c) throws Exception {
        switch (c.action) {
            case "generate_qr":         return generateQr(c);
            case "attendance_report":   return attendance(c);
            case "apply_leave":         return applyLeave(c);
            case "leave_action":        return leaveAction(c);
            case "leave_status":        return leaveStatus(c);
            case "generate_payroll":    return generatePayroll(c);
            case "payslip":             return payslip(c);
            case "notifications_list":  return notifications(c);
            case "mark_read":           return markRead(c);
            default:                    return Ctx.fail("Invalid action");
        }
    }

    /** Minutes a QR token stays valid (the frontend timer also uses 5 minutes). */
    private static final int QR_VALID_MINUTES = 5;

    private Map<String, Object> generateQr(Ctx c) throws SQLException {
        String token = "QR_" + c.empId + "_" + System.currentTimeMillis();
        Db.update("INSERT INTO qr_tokens (token, emp_id, generated_at, expires_at, used) "
                + "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 0)", token, c.empId, QR_VALID_MINUTES);
        return Ctx.ok("emp_id", c.empId, "qr_token", token,
                "message", "Scan this token at the attendance terminal", "generated_at", Instant.now().toString());
    }

    private Map<String, Object> attendance(Ctx c) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT a.emp_id, e.name, a.`date`, a.check_in, a.check_out, a.status "
                + "FROM attendance a JOIN employees e ON e.emp_id = a.emp_id WHERE 1=1");
        List<Object> p = new ArrayList<>();
        String emp = c.isManagerUp() ? c.str("emp_id") : c.empId;
        if (emp != null)              { sql.append(" AND a.emp_id = ?");  p.add(emp); }
        if (c.str("from_date") != null) { sql.append(" AND a.`date` >= ?"); p.add(c.str("from_date")); }
        if (c.str("to_date") != null)   { sql.append(" AND a.`date` <= ?"); p.add(c.str("to_date")); }
        List<Map<String, Object>> rows = Db.query(sql.append(" ORDER BY a.`date`").toString(), p.toArray());
        return Ctx.ok("total", rows.size(), "records", rows);
    }

    private Map<String, Object> applyLeave(Ctx c) throws SQLException {
        if (c.str("type") == null || c.str("from_date") == null || c.str("to_date") == null)
            return Ctx.fail("leave_type, from_date, to_date are required", 400);
        String id = "LV" + String.valueOf(System.currentTimeMillis()).substring(5);
        Db.update("INSERT INTO leave_requests (leave_id,emp_id,type,from_date,to_date,reason,status,manager_note,applied_at) "
                + "VALUES (?,?,?,?,?,?, 'pending','',CURRENT_DATE)",
                id, c.empId, c.str("type"), c.str("from_date"), c.str("to_date"), Ctx.pick(c.str("reason"), ""));
        return Ctx.ok("message", "Leave applied successfully", "leave_id", id);
    }

    private Map<String, Object> leaveAction(Ctx c) throws SQLException {
        if (!c.isManagerUp()) return Ctx.fail("Forbidden: Only manager/admin/hr can action leave", 403);
        if (c.str("leave_id") == null || c.str("action") == null)
            return Ctx.fail("leave_id and action are required", 400);
        String a = c.str("action").toLowerCase();
        String status = a.equals("approve") ? "approved" : a.equals("reject") ? "rejected" : a;
        if (!status.equals("approved") && !status.equals("rejected"))
            return Ctx.fail("action must be approved or rejected", 400);
        if (Db.update("UPDATE leave_requests SET status = ?, manager_note = ? WHERE leave_id = ?",
                status, Ctx.pick(c.str("manager_note"), ""), c.str("leave_id")) == 0)
            return Ctx.fail("Leave request not found", 404);
        return Ctx.ok("message", "Leave " + status, "leave_id", c.str("leave_id"), "status", status);
    }

    private Map<String, Object> leaveStatus(Ctx c) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT l.*, e.name AS emp_name FROM leave_requests l LEFT JOIN employees e ON e.emp_id = l.emp_id WHERE 1=1");
        List<Object> p = new ArrayList<>();
        // Admin/HR/manager see everyone's requests. The frontend always sends the caller's
        // own emp_id, so that value is ignored for them (otherwise "Manage Requests" is empty).
        String emp = c.str("emp_id");
        if (c.isManagerUp()) { if (emp != null && emp.equalsIgnoreCase(c.empId)) emp = null; }
        else emp = c.empId;
        if (emp != null)                   { sql.append(" AND l.emp_id = ?"); p.add(emp); }
        if (c.str("filter_status") != null) { sql.append(" AND l.status = ?"); p.add(c.str("filter_status")); }
        List<Map<String, Object>> rows = Db.query(sql.append(" ORDER BY l.applied_at DESC").toString(), p.toArray());
        return Ctx.ok("total", rows.size(), "leaves", rows);
    }

    private Map<String, Object> generatePayroll(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can generate payroll", 403);
        Integer month = c.num("month"), year = c.num("year");
        if (month == null || year == null) return Ctx.fail("month and year are required", 400);

        List<Map<String, Object>> emps = EmployeeServlet.find(null, null, "active");
        Db.tx(cn -> {
            for (Map<String, Object> e : emps) {
                BigDecimal base = (BigDecimal) e.get("salary_base");
                BigDecimal ded = base.multiply(new BigDecimal("0.12")).setScale(0, RoundingMode.HALF_UP);
                // re-running for the same month/year overwrites instead of failing
                Db.update(cn, "INSERT INTO payroll (payroll_id,emp_id,name,month,year,salary_base,deductions,net_salary,generated_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,CURRENT_DATE) ON DUPLICATE KEY UPDATE "
                        + "name=VALUES(name), salary_base=VALUES(salary_base), deductions=VALUES(deductions), "
                        + "net_salary=VALUES(net_salary), generated_at=VALUES(generated_at)",
                        "PAY" + e.get("emp_id") + "_" + month + "_" + year, e.get("emp_id"), e.get("name"),
                        month, year, base, ded, base.subtract(ded));
            }
        });
        return Ctx.ok("message", "Payroll generated successfully");
    }

    private Map<String, Object> payslip(Ctx c) throws SQLException {
        String emp = c.isAdminHr() ? Ctx.pick(c.str("emp_id"), c.empId) : c.empId;
        Map<String, Object> slip = Db.one("SELECT * FROM payroll WHERE emp_id = ? AND month = ? AND year = ?",
                emp, c.num("month"), c.num("year"));
        if (slip == null) return Ctx.fail("No payslip found for this period", 404);
        return Ctx.ok("payslip", slip);
    }

    private Map<String, Object> notifications(Ctx c) throws SQLException {
        String emp = c.isAdminHr() ? c.str("emp_id") : c.empId;
        List<Map<String, Object>> rows = emp == null
                ? Db.query("SELECT * FROM notifications ORDER BY created_at DESC")
                : Db.query("SELECT * FROM notifications WHERE emp_id = ? ORDER BY created_at DESC", emp);
        return Ctx.ok("total", rows.size(), "notifications", rows);
    }

    private Map<String, Object> markRead(Ctx c) throws SQLException {
        String id = c.str("notif_id");
        if (id == null) return Ctx.fail("notif_id is required", 400);
        Db.update("UPDATE notifications SET is_read = 1 WHERE notif_id = ?", id);
        return Ctx.ok("message", "Notification marked as read", "notif_id", id);
    }
}

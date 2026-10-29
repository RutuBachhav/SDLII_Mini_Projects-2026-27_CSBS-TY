package com.web;

import com.util.Ctx;
import com.util.Db;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;

/** POST /ems/reports : employee, attendance, leave, payroll */
@WebServlet("/ems/reports")
public class ReportsServlet extends BaseServlet {

    @Override
    protected Map<String, Object> handle(Ctx c) throws Exception {
        switch (c.action) {
            case "employee":   return employee(c);
            case "attendance": return attendance(c);
            case "leave":      return leave(c);
            case "payroll":    return payroll(c);
            default:           return Ctx.fail("Invalid action");
        }
    }

    private Map<String, Object> employee(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Reports are admin/hr only", 403);
        List<Map<String, Object>> emps = EmployeeServlet.find(c.str("dept_id"), c.str("role"), c.str("status"));
        Map<String, Integer> byStatus = Ctx.countBy(emps, "status");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", emps.size());
        summary.put("active", byStatus.getOrDefault("active", 0));
        summary.put("inactive", byStatus.getOrDefault("inactive", 0));
        summary.put("by_role", Ctx.countBy(emps, "role"));
        summary.put("by_dept", Ctx.countBy(emps, "dept_id"));
        return Ctx.ok("summary", summary, "employees", emps);
    }

    private Map<String, Object> attendance(Ctx c) throws SQLException {
        Integer month = c.num("month"), year = c.num("year");
        if (month == null || year == null) return Ctx.fail("month and year are required", 400);
        List<Map<String, Object>> recs = Db.query(
                "SELECT emp_id, `date`, check_in, check_out, status FROM attendance "
                + "WHERE MONTH(`date`) = ? AND YEAR(`date`) = ? ORDER BY `date`, record_id", month, year);

        Map<String, Map<String, Object>> byEmp = new LinkedHashMap<>();
        Map<String, Map<String, Object>> byDate = new LinkedHashMap<>();
        for (Map<String, Object> r : recs) {
            Map<String, Object> e = byEmp.computeIfAbsent((String) r.get("emp_id"), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("emp_id", k); m.put("days_present", 0); m.put("late_days", 0);
                m.put("last_check_in", ""); m.put("last_check_out", "");
                return m;
            });
            e.merge("days_present", 1, (a, b) -> (Integer) a + 1);
            if ("late".equals(r.get("status"))) e.merge("late_days", 1, (a, b) -> (Integer) a + 1);
            e.put("last_check_in", r.get("check_in"));
            e.put("last_check_out", r.get("check_out"));

            Map<String, Object> d = byDate.computeIfAbsent((String) r.get("date"), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("date", k); m.put("present", 0); m.put("absent", 0); m.put("late", 0);
                return m;
            });
            String st = (String) r.get("status");
            if ("present".equals(st) || "absent".equals(st) || "late".equals(st))
                d.merge(st, 1, (a, b) -> (Integer) a + 1);
        }
        return Ctx.ok("month", month, "year", year, "total_records", recs.size(),
                "by_employee", new ArrayList<>(byEmp.values()), "attendanceData", new ArrayList<>(byDate.values()));
    }

    private Map<String, Object> leave(Ctx c) throws SQLException {
        Integer month = c.num("month"), year = c.num("year");
        if (month == null || year == null) return Ctx.fail("month and year are required", 400);
        List<Map<String, Object>> leaves = Db.query(
                "SELECT * FROM leave_requests WHERE MONTH(applied_at) = ? AND YEAR(applied_at) = ?", month, year);
        Map<String, Integer> byStatus = Ctx.countBy(leaves, "status");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", leaves.size());
        summary.put("approved", byStatus.getOrDefault("approved", 0));
        summary.put("rejected", byStatus.getOrDefault("rejected", 0));
        summary.put("pending", byStatus.getOrDefault("pending", 0));
        summary.put("by_type", Ctx.countBy(leaves, "type"));
        return Ctx.ok("month", month, "year", year, "summary", summary, "leaves", leaves);
    }

    private Map<String, Object> payroll(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Payroll reports are admin/hr only", 403);
        Integer month = c.num("month"), year = c.num("year");
        if (month == null || year == null) return Ctx.fail("month and year are required", 400);
        List<Map<String, Object>> recs = Db.query("SELECT * FROM payroll WHERE month = ? AND year = ?", month, year);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_employees", recs.size());
        summary.put("total_basic", sum(recs, "salary_base"));
        summary.put("total_deductions", sum(recs, "deductions"));
        summary.put("total_net_salary", sum(recs, "net_salary"));
        return Ctx.ok("month", month, "year", year, "summary", summary, "records", recs);
    }

    private static BigDecimal sum(List<Map<String, Object>> rows, String key) {
        BigDecimal t = BigDecimal.ZERO;
        for (Map<String, Object> r : rows) t = t.add((BigDecimal) r.get(key));
        return t;
    }
}

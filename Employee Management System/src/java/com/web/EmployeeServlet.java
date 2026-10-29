package com.web;

import com.google.gson.JsonObject;
import com.util.Ctx;
import com.util.Db;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;

/** POST /ems/employee : login, add, list, update, deactivate */
@WebServlet("/ems/employee")
public class EmployeeServlet extends BaseServlet {

    @Override
    protected Map<String, Object> preAuth(String action, JsonObject body) throws SQLException {
        if (!"login".equals(action)) return null;
        Map<String, Object> e = Db.one("SELECT * FROM employees WHERE LOWER(TRIM(emp_id)) = ?",
                text(body, "emp_id").trim().toLowerCase());
        if (e == null) return Ctx.fail("Employee not found", 404);
        if ("inactive".equalsIgnoreCase((String) e.get("status"))) return Ctx.fail("Account is inactive", 403);
        return Ctx.ok("emp_id", e.get("emp_id"), "name", e.get("name"), "role", e.get("role"),
                "dept_id", e.get("dept_id"), "status", e.get("status"));
    }

    @Override
    protected Map<String, Object> handle(Ctx c) throws Exception {
        switch (c.action) {
            case "add":        return add(c);
            case "list":       return list(c);
            case "update":     return update(c);
            case "deactivate": return deactivate(c);
            default:           return Ctx.fail("Invalid module or action");
        }
    }

    private Map<String, Object> add(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can add employees", 403);
        String name = c.str("name"), email = c.str("email"), role = c.str("role"), dept = c.str("dept_id");
        BigDecimal salary = c.dec("salary_base");
        if (name == null || email == null || role == null || dept == null || salary == null)
            return Ctx.fail("Missing required fields: name, email, role, dept_id, salary_base", 400);

        String id = "EMP" + String.valueOf(System.currentTimeMillis()).substring(7);
        Db.tx(cn -> {
            Db.update(cn, "INSERT INTO employees (emp_id,name,email,phone,role,dept_id,salary_base,joining_date,status) "
                    + "VALUES (?,?,?,?,?,?,?,CURRENT_DATE,'active')",
                    id, name, email, Ctx.pick(c.str("phone"), ""), role.toLowerCase(), dept, salary);
            Db.update(cn, "INSERT INTO leave_balance (emp_id,casual_left,sick_left,annual_left) VALUES (?,12,8,15)", id);
        });
        return Ctx.ok("message", "Employee added successfully", "emp_id", id, "name", name);
    }

    /** Shared with ReportsServlet. Null filters are ignored. */
    static List<Map<String, Object>> find(String dept, String role, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM employees WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (dept != null)   { sql.append(" AND dept_id = ?"); p.add(dept); }
        if (role != null)   { sql.append(" AND role = ?");    p.add(role); }
        if (status != null) { sql.append(" AND status = ?");  p.add(status); }
        return Db.query(sql.toString(), p.toArray());
    }

    private Map<String, Object> list(Ctx c) throws SQLException {
        String status = c.isAdminHr() ? null : Ctx.pick(c.str("status"), "active");
        List<Map<String, Object>> emps = find(c.str("dept_id"), c.str("role"), status);
        return Ctx.ok("total", emps.size(), "employees", emps);
    }

    private Map<String, Object> update(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can update employees", 403);
        String id = c.str("emp_id");
        if (id == null) return Ctx.fail("emp_id is required", 400);
        Map<String, Object> e = Db.one("SELECT * FROM employees WHERE emp_id = ?", id);
        if (e == null) return Ctx.fail("Employee not found", 404);

        Db.update("UPDATE employees SET name=?, email=?, phone=?, role=?, dept_id=?, salary_base=?, status=? WHERE emp_id=?",
                Ctx.pick(c.str("name"), e.get("name")),
                Ctx.pick(c.str("email"), e.get("email")),
                Ctx.pick(c.str("phone"), e.get("phone")),
                Ctx.pick(Ctx.pick(c.str("role"), c.str("role_update")), e.get("role")),
                Ctx.pick(c.str("dept_id"), e.get("dept_id")),
                Ctx.pick(c.dec("salary_base"), e.get("salary_base")),
                Ctx.pick(c.str("status"), e.get("status")),
                id);
        return Ctx.ok("message", "Employee updated successfully", "emp_id", id);
    }

    private Map<String, Object> deactivate(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can deactivate employees", 403);
        String id = c.str("emp_id");
        if (id == null) return Ctx.fail("emp_id is required", 400);
        Map<String, Object> e = Db.one("SELECT status FROM employees WHERE emp_id = ?", id);
        if (e == null) return Ctx.fail("Employee not found", 404);
        if ("inactive".equals(e.get("status"))) return Ctx.fail("Employee already inactive", 409);
        Db.update("UPDATE employees SET status='inactive' WHERE emp_id = ?", id);
        return Ctx.ok("message", "Employee deactivated successfully", "emp_id", id);
    }
}

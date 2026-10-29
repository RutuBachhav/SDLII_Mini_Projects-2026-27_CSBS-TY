package com.web;

import com.util.Ctx;
import com.util.Db;
import java.sql.SQLException;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;

/** POST /ems/department : list, add, assign */
@WebServlet("/ems/department")
public class DepartmentServlet extends BaseServlet {

    @Override
    protected Map<String, Object> handle(Ctx c) throws Exception {
        switch (c.action) {
            case "list":   return list();
            case "add":    return add(c);
            case "assign": return assign(c);
            default:       return Ctx.fail("Invalid action");
        }
    }

    private Map<String, Object> list() throws SQLException {
        List<Map<String, Object>> depts = Db.query("SELECT * FROM departments");
        return Ctx.ok("total", depts.size(), "departments", depts);
    }

    private Map<String, Object> add(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can add departments", 403);
        String name = c.str("dept_name");
        if (name == null) return Ctx.fail("dept_name is required", 400);
        String id = "DEPT" + String.valueOf(System.currentTimeMillis()).substring(8);
        Db.update("INSERT INTO departments (dept_id, dept_name, manager_emp_id, created_at) VALUES (?,?,?,CURRENT_DATE)",
                id, name, Ctx.pick(c.str("manager_emp_id"), ""));
        return Ctx.ok("message", "Department added successfully", "dept_id", id, "dept_name", name);
    }

    private Map<String, Object> assign(Ctx c) throws SQLException {
        if (!c.isAdminHr()) return Ctx.fail("Forbidden: Only admin/hr can assign departments", 403);
        String emp = c.str("emp_id"), dept = c.str("dept_id");
        if (emp == null || dept == null) return Ctx.fail("emp_id and dept_id are required", 400);
        if (Db.update("UPDATE employees SET dept_id = ? WHERE emp_id = ?", dept, emp) == 0)
            return Ctx.fail("Employee not found", 404);
        return Ctx.ok("message", "Employee assigned to department", "emp_id", emp, "dept_id", dept);
    }
}

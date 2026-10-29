package com.util;

import java.math.BigDecimal;
import java.util.*;

/** Per-request context (the n8n "Prepare Context" node) plus response helpers. */
public class Ctx {
    public final String action, role, empId, deptId;
    public final Map<String, Object> data;

    public Ctx(String action, Map<String, Object> caller, Map<String, Object> data) {
        this.action = action;
        this.role = (String) caller.get("role");
        this.empId = (String) caller.get("emp_id");
        this.deptId = (String) caller.get("dept_id");
        this.data = data;
    }

    public boolean isAdminHr() { return in("admin", "hr"); }
    public boolean isManagerUp() { return in("admin", "hr", "manager"); }

    private boolean in(String... roles) {
        for (String r : roles) if (r.equalsIgnoreCase(role)) return true;
        return false;
    }

    /** Value as text; null when missing or empty (like JS falsy for strings). */
    public String str(String k) {
        Object v = data.get(k);
        if (v == null) return null;
        String s = (v instanceof Double && (Double) v % 1 == 0) ? String.valueOf(((Double) v).longValue()) : v.toString();
        return s.isEmpty() ? null : s;
    }

    public BigDecimal dec(String k) { String s = str(k); return s == null ? null : new BigDecimal(s); }

    public Integer num(String k) { BigDecimal d = dec(k); return d == null ? null : d.intValue(); }

    public static <T> T pick(T a, T b) { return a != null ? a : b; }

    // ---- response helpers ----
    public static Map<String, Object> fail(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", msg);
        return m;
    }

    public static Map<String, Object> fail(String msg, int code) {
        Map<String, Object> m = fail(msg);
        m.put("code", code);
        return m;
    }

    /** ok("key", value, "key2", value2 ...) */
    public static Map<String, Object> ok(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    public static Map<String, Integer> countBy(List<Map<String, Object>> rows, String key) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) out.merge(String.valueOf(r.get(key)), 1, Integer::sum);
        return out;
    }
}

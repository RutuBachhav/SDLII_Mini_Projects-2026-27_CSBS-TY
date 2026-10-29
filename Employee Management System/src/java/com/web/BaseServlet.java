package com.web;

import com.google.gson.*;
import com.util.Ctx;
import com.util.Db;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import jakarta.servlet.http.*;

/**
 * Shared webhook behaviour from the n8n workflow: CORS, x-api-key check,
 * caller lookup + active check, then dispatch to handle().
 */
public abstract class BaseServlet extends HttpServlet {
    private static final String API_KEY = "iamBOSS";
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    protected abstract Map<String, Object> handle(Ctx c) throws Exception;

    /** Runs before caller lookup; return a response to short-circuit (used by login). */
    protected Map<String, Object> preAuth(String action, JsonObject body) throws Exception { return null; }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        cors(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        cors(resp);
        Map<String, Object> out;
        try {
            out = process(req);
        } catch (SQLIntegrityConstraintViolationException e) {
            String m = String.valueOf(e.getMessage()).toLowerCase();
            out = Ctx.fail(m.contains("duplicate") && m.contains("email") ? "This email is already used by another employee"
                    : m.contains("duplicate") ? "This record already exists"
                    : "Invalid value: it refers to an employee or department that does not exist", 409);
        } catch (Exception e) {
            log("EMS error", e);
            out = Ctx.fail("Server error: " + e.getMessage(), 500);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        GSON.toJson(out, resp.getWriter());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> process(HttpServletRequest req) throws Exception {
        if (!API_KEY.equals(req.getHeader("x-api-key"))) return Ctx.fail("Unauthorized");

        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        String action = text(body, "action");

        Map<String, Object> early = preAuth(action, body);
        if (early != null) return early;

        Map<String, Object> caller = Db.one("SELECT * FROM employees WHERE LOWER(TRIM(emp_id)) = ?",
                text(body, "auth_emp_id").trim().toLowerCase());
        if (caller == null || !"active".equals(caller.get("status")))
            return Ctx.fail("Employee inactive or not found");

        Map<String, Object> data = new HashMap<>();
        if (body.has("data") && body.get("data").isJsonObject())
            data = GSON.fromJson(body.get("data"), Map.class);

        return handle(new Ctx(action, caller, data));
    }

    protected static String text(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private static void cors(HttpServletResponse r) {
        r.setHeader("Access-Control-Allow-Origin", "*");
        r.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        r.setHeader("Access-Control-Allow-Headers", "Content-Type, x-api-key, ngrok-skip-browser-warning");
    }
}

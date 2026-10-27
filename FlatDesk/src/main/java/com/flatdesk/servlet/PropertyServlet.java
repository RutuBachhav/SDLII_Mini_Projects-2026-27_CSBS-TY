
package com.flatdesk.servlet;

import com.flatdesk.dao.PropertyDAO;
import com.flatdesk.model.Property;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

@WebServlet("/api/properties")
public class PropertyServlet extends HttpServlet {

    private final PropertyDAO dao = new PropertyDAO();
    private final Gson gson = new Gson();

    private HttpSession getLoggedInSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }

        return session;
    }

    private void sendError(HttpServletResponse resp, int status,
                           String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        resp.getWriter().write(
            gson.toJson(Collections.singletonMap("error", message))
        );
    }

    private int getUserId(HttpSession session) {
        Object value = session.getAttribute("userId");
        return value instanceof Integer ? (Integer) value : -1;
    }

    private boolean canManage(Property property, String role, int userId) {
        if (property == null) {
            return false;
        }

        if ("ADMIN".equals(role)) {
            return true;
        }

        if ("OWNER".equals(role)) {
            return property.getOwnerId() == userId;
        }

        if ("BROKER".equals(role)) {
            return property.getBrokerId() == userId;
        }

        return false;
    }

    private boolean isValidStatus(String status) {
        return status != null
                && Arrays.asList("Available", "Rented", "Sold")
                         .contains(status);
    }

    // Validate property contact number.
    private boolean isValidContactNumber(String contactNumber) {
        return contactNumber != null
                && contactNumber.trim().matches("[0-9+() -]{7,20}")
                && contactNumber.replaceAll("[^0-9]", "").length() >= 7;
    }

    // GET: List properties
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = getLoggedInSession(req);

        try {
            List<Property> properties;

            if (session != null
                    && "OWNER".equals(session.getAttribute("role"))) {

                int ownerId = getUserId(session);

                if (ownerId <= 0) {
                    sendError(resp, 401, "Invalid login session");
                    return;
                }

                properties = dao.findByOwnerId(ownerId);

            } else {
                properties = dao.all();
            }

            resp.getWriter().write(gson.toJson(properties));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Unable to load properties");
        }
    }

    // POST: Add property
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = getLoggedInSession(req);

        if (session == null) {
            sendError(resp, 401, "Please login first");
            return;
        }

        String role = (String) session.getAttribute("role");
        int userId = getUserId(session);

        if (userId <= 0) {
            sendError(resp, 401, "Invalid login session");
            return;
        }

        if (!"ADMIN".equals(role)
                && !"BROKER".equals(role)
                && !"OWNER".equals(role)) {
            sendError(resp, 403, "You cannot add properties");
            return;
        }

        try {
            Property p = gson.fromJson(req.getReader(), Property.class);

            if (p == null || p.getTitle() == null
                    || p.getTitle().trim().isEmpty()) {
                sendError(resp, 400, "Property title is required");
                return;
            }

            // CONTACT NUMBER IS REQUIRED FOR EVERY PROPERTY
            if (!isValidContactNumber(p.getContactNumber())) {
                sendError(resp, 400,
                        "A valid contact number is required");
                return;
            }

            p.setContactNumber(p.getContactNumber().trim());

            // Set ownership using the authenticated session.
            p.setCreatedBy(userId);
            p.setCreatedByRole(role);
            p.setStatus("Available");

            if ("OWNER".equals(role)) {
                p.setOwnerId(userId);
                p.setBrokerId(0);

            } else if ("BROKER".equals(role)) {
                p.setOwnerId(0);
                p.setBrokerId(userId);

            } else {
                p.setOwnerId(0);
                p.setBrokerId(0);
            }

            int id = dao.add(p);

            if (id <= 0) {
                sendError(resp, 500, "Property could not be added");
                return;
            }

            p.setId(id);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(p));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 400, "Invalid property data");
        }
    }

    // PUT: Update property details OR update its status
    @Override
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = getLoggedInSession(req);

        if (session == null) {
            sendError(resp, 401, "Please login first");
            return;
        }

        String role = (String) session.getAttribute("role");
        int userId = getUserId(session);

        if (userId <= 0) {
            sendError(resp, 401, "Invalid login session");
            return;
        }

        String idParam = req.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            sendError(resp, 400, "Property ID is required");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);

            if (id <= 0) {
                sendError(resp, 400, "Invalid property ID");
                return;
            }

            Property existing = dao.findById(id);

            if (existing == null) {
                sendError(resp, 404, "Property not found");
                return;
            }

            // Check permission before changing anything.
            if (!canManage(existing, role, userId)) {
                sendError(resp, 403,
                        "You cannot manage this property");
                return;
            }

            Property changes =
                    gson.fromJson(req.getReader(), Property.class);

            if (changes == null) {
                sendError(resp, 400, "Request body is required");
                return;
            }

            /*
             * STATUS UPDATE
             * Send JSON containing only a status, for example:
             * {"status":"Rented"}
             */
            if (changes.getStatus() != null
                    && (changes.getTitle() == null
                        || changes.getTitle().trim().isEmpty())) {

                String status = changes.getStatus().trim();

                if (!isValidStatus(status)) {
                    sendError(resp, 400,
                            "Invalid status. Use Available, Rented, or Sold");
                    return;
                }

                boolean updated = dao.updateStatus(id, status);

                if (!updated) {
                    sendError(resp, 500,
                            "Property status could not be updated");
                    return;
                }

                Property updatedProperty = dao.findById(id);
                resp.getWriter().write(gson.toJson(updatedProperty));
                return;
            }

            /*
             * PROPERTY DETAILS UPDATE
             */
            if (changes.getTitle() == null
                    || changes.getTitle().trim().isEmpty()) {
                sendError(resp, 400, "Property title is required");
                return;
            }

            // Validate contact number when property details are updated.
            if (!isValidContactNumber(changes.getContactNumber())) {
                sendError(resp, 400,
                        "A valid contact number is required");
                return;
            }

            changes.setContactNumber(changes.getContactNumber().trim());

            boolean updated = dao.updateDetails(id, changes);

            if (!updated) {
                sendError(resp, 500,
                        "Property could not be updated");
                return;
            }

            Property updatedProperty = dao.findById(id);
            resp.getWriter().write(gson.toJson(updatedProperty));

        } catch (NumberFormatException e) {
            sendError(resp, 400, "Invalid property ID");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Unable to update property");
        }
    }

    // DELETE: Delete property with permission check
    @Override
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = getLoggedInSession(req);

        if (session == null) {
            sendError(resp, 401, "Please login first");
            return;
        }

        String role = (String) session.getAttribute("role");
        int userId = getUserId(session);
        String idParam = req.getParameter("id");

        if (userId <= 0) {
            sendError(resp, 401, "Invalid login session");
            return;
        }

        if (idParam == null || idParam.trim().isEmpty()) {
            sendError(resp, 400, "Property ID is required");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);

            if (id <= 0) {
                sendError(resp, 400, "Invalid property ID");
                return;
            }

            Property existing = dao.findById(id);

            if (existing == null) {
                sendError(resp, 404, "Property not found");
                return;
            }

            if (!canManage(existing, role, userId)) {
                sendError(resp, 403,
                        "You cannot delete this property");
                return;
            }

            if (dao.delete(id)) {
                resp.getWriter().write("{\"success\":true}");
            } else {
                sendError(resp, 500,
                        "Property could not be deleted");
            }

        } catch (NumberFormatException e) {
            sendError(resp, 400, "Invalid property ID");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Unable to delete property");
        }
    }
}

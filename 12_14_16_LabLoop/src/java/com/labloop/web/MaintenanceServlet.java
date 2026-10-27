package com.labloop.web;

import com.labloop.entity.TicketSeverity;
import com.labloop.entity.TicketStatus;
import com.labloop.service.EquipmentService;
import com.labloop.service.MaintenanceService;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *   GET  /maintenance             tickets (filter by status)
 *   GET  /maintenance/new?equipmentId=   fault report form (anyone)
 *   POST /maintenance/report | start | resolve
 */
@WebServlet(urlPatterns = {"/maintenance", "/maintenance/*"})
public class MaintenanceServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB private MaintenanceService maintenanceService;
    @EJB private EquipmentService equipmentService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (action(req)) {
                case "/":
                    String s = str(req, "status");
                    TicketStatus status = "ALL".equals(s) ? null : enumParam(req, "status", TicketStatus.class);
                    req.setAttribute("tickets", s == null ? maintenanceService.unresolved() : maintenanceService.list(status));
                    req.setAttribute("fStatus", s == null ? "UNRESOLVED" : s);
                    req.setAttribute("serviceDue", equipmentService.serviceDue());
                    render(req, resp, "maintenance-list");
                    break;
                case "/new":
                    req.setAttribute("e", equipmentService.find(longParam(req, "equipmentId")));
                    req.setAttribute("severities", TicketSeverity.values());
                    render(req, resp, "maintenance-form");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
            redirect(req, resp, "/maintenance");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = action(req);
        Long equipmentId = null;
        try {
            switch (action) {
                case "/report":
                    equipmentId = longParam(req, "equipmentId");
                    maintenanceService.open(equipmentId, me.getUserId(), str(req, "issue"),
                            enumParam(req, "severity", TicketSeverity.class));
                    flashOk(req, "Fault reported. Thank you - the lab team has been notified.");
                    redirect(req, resp, "/equipment/view?id=" + equipmentId);
                    return;
                case "/start":
                    if (!staffOnly(resp)) return;
                    maintenanceService.start(longParam(req, "id"), me.getUserId());
                    flashOk(req, "Repair started.");
                    break;
                case "/resolve":
                    if (!staffOnly(resp)) return;
                    maintenanceService.resolve(longParam(req, "id"), me.getUserId(), str(req, "notes"),
                            doubleParam(req, "cost", 0));
                    flashOk(req, "Ticket resolved - item is back in service.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } catch (LabLoopException ex) {
            if ("/report".equals(action) && equipmentId != null) {
                req.setAttribute("errors", ex.getMessages());
                req.setAttribute("issue", str(req, "issue"));
                try {
                    req.setAttribute("e", equipmentService.find(equipmentId));
                    req.setAttribute("severities", TicketSeverity.values());
                    render(req, resp, "maintenance-form");
                    return;
                } catch (LabLoopException ignored) {
                    // fall through to redirect
                }
            }
            flashError(req, ex);
        }
        redirect(req, resp, "/maintenance");
    }
}

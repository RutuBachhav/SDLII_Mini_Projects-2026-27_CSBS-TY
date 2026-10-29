package com.labloop.web;

import com.labloop.service.*;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB private LabMonitorBean monitor;
    @EJB private BookingService bookingService;
    @EJB private MaintenanceService maintenanceService;
    @EJB private EquipmentService equipmentService;
    @EJB private ActivityLogService activityLog;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("stats", monitor.getStats());
        req.setAttribute("myUpcoming", bookingService.upcomingForUser(me.getUserId()));
        if (me.isStaff()) {
            req.setAttribute("pending", bookingService.byStatus(com.labloop.entity.BookingStatus.PENDING));
            req.setAttribute("pickups", bookingService.dueForPickup());
            req.setAttribute("overdue", bookingService.overdue());
            req.setAttribute("tickets", maintenanceService.unresolved());
            req.setAttribute("serviceDue", equipmentService.serviceDue());
            req.setAttribute("recent", activityLog.recent(8));
        }
        render(req, resp, "dashboard");
    }
}

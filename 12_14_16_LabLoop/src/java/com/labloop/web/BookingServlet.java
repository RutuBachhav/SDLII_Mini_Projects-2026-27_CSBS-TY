package com.labloop.web;

import com.labloop.cdi.BookingPolicy;
import com.labloop.entity.*;
import com.labloop.service.BookingService;
import com.labloop.service.EquipmentService;
import com.labloop.service.UserService;
import com.labloop.util.Fmt;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *   GET  /bookings               my bookings (students/faculty) or the approval queue (staff)
 *   GET  /bookings/new?equipmentId=
 *   POST /bookings/create
 *   POST /bookings/approve | reject | checkout | return | cancel
 */
@WebServlet(urlPatterns = {"/bookings", "/bookings/*"})
public class BookingServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB private BookingService bookingService;
    @EJB private EquipmentService equipmentService;
    @EJB private UserService userService;
    @Inject private BookingPolicy policy;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (action(req)) {
                case "/":
                    list(req, resp);
                    break;
                case "/new":
                    Equipment e = equipmentService.find(longParam(req, "equipmentId"));
                    LocalDateTime start = suggestedStart();
                    showForm(req, resp, e, start, start.plusHours(2), null);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
            redirect(req, resp, "/equipment");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = action(req);
        if ("/create".equals(action)) {
            create(req, resp);
            return;
        }
        try {
            Long id = longParam(req, "id");
            String remark = str(req, "remark");
            Booking b;
            switch (action) {
                case "/approve":
                    b = bookingService.approve(id, me.getUserId(), remark);
                    flashOk(req, "Approved booking #" + b.getId() + " for " + b.getRequestedBy().getFullName() + ".");
                    break;
                case "/reject":
                    b = bookingService.reject(id, me.getUserId(), remark);
                    flashOk(req, "Rejected booking #" + b.getId() + ".");
                    break;
                case "/checkout":
                    b = bookingService.checkOut(id, me.getUserId());
                    flashOk(req, b.getEquipment().getAssetTag() + " issued to " + b.getRequestedBy().getFullName()
                            + ". Due back " + b.getEndDisplay() + ".");
                    break;
                case "/return":
                    boolean damaged = req.getParameter("damaged") != null;
                    b = bookingService.returnItem(id, me.getUserId(), damaged, remark);
                    flashOk(req, b.getEquipment().getAssetTag() + " returned"
                            + (damaged ? " - maintenance ticket raised and item taken out of service." : "."));
                    break;
                case "/cancel":
                    b = bookingService.cancel(id, me.getUserId());
                    flashOk(req, "Booking #" + b.getId() + " cancelled.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
        }
        String back = req.getParameter("back");
        redirect(req, resp, (back != null && back.startsWith("/") && !back.startsWith("//")) ? back : "/bookings");
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (me.isStaff()) {
            String s = str(req, "status");
            BookingStatus status = s == null ? BookingStatus.PENDING : ("ALL".equals(s) ? null : enumParam(req, "status", BookingStatus.class));
            req.setAttribute("bookings", bookingService.byStatus(status));
            req.setAttribute("fStatus", status == null ? "ALL" : status.name());
            req.setAttribute("statuses", BookingStatus.values());
            req.setAttribute("overdue", bookingService.overdue());
        } else {
            req.setAttribute("bookings", bookingService.forUser(me.getUserId()));
        }
        render(req, resp, "booking-list");
    }

    private void create(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long equipmentId = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        String purpose = str(req, "purpose");
        try {
            equipmentId = longParam(req, "equipmentId");
            start = dateTimeParam(req, "start");
            end = dateTimeParam(req, "end");
            Booking b = bookingService.create(equipmentId, me.getUserId(), start, end, purpose);
            flashOk(req, b.getStatus() == BookingStatus.APPROVED
                    ? "Booked! " + b.getEquipment().getAssetTag() + " is reserved for you."
                    : "Request sent. A lab assistant will approve it before " + b.getStartDisplay() + ".");
            redirect(req, resp, "/bookings");
        } catch (LabLoopException ex) {
            req.setAttribute("errors", ex.getMessages());
            try {
                showForm(req, resp, equipmentService.find(equipmentId),
                        start == null ? suggestedStart() : start, end == null ? suggestedStart().plusHours(2) : end, purpose);
            } catch (LabLoopException notFound) {
                flashError(req, notFound);
                redirect(req, resp, "/equipment");
            }
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Equipment e,
                          LocalDateTime start, LocalDateTime end, String purpose) throws ServletException, IOException {
        LabUser user = userService.find(me.getUserId());
        req.setAttribute("e", e);
        req.setAttribute("startValue", Fmt.input(start));
        req.setAttribute("endValue", Fmt.input(end));
        req.setAttribute("minValue", Fmt.input(LocalDateTime.now().withSecond(0).withNano(0)));
        req.setAttribute("purpose", purpose);
        req.setAttribute("maxHours", policy.maxHoursFor(user.getRole()));
        req.setAttribute("autoApprove", policy.autoApprove(user));
        req.setAttribute("isStudent", user.getRole() == UserRole.STUDENT);
        LocalDate day = start.toLocalDate();
        req.setAttribute("dayLabel", Fmt.date(day));
        req.setAttribute("taken", bookingService.takenOn(e.getId(), day));
        render(req, resp, "booking-form");
    }

    /** Next sensible slot: the next full hour within lab hours, skipping Sunday. */
    static LocalDateTime suggestedStart() {
        LocalDateTime t = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
        if (t.getHour() < 8) t = t.withHour(9);
        if (t.getHour() >= 18) t = t.plusDays(1).withHour(9);
        if (t.getDayOfWeek() == DayOfWeek.SUNDAY) t = t.plusDays(1).withHour(9);
        return t;
    }
}

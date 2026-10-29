package com.labloop.web;

import com.labloop.service.PracticalKitBean;
import com.labloop.util.Fmt;
import com.labloop.util.LabLoopException;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Practical Kit pages - backed by the STATEFUL PracticalKitBean.
 * Notice @Inject (not @EJB): the kit is @SessionScoped, so each user gets their own kit.
 */
@WebServlet(urlPatterns = {"/kit", "/kit/*"})
public class KitServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private PracticalKitBean kit;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDateTime start = BookingServlet.suggestedStart();
        showKit(req, resp, start, start.plusHours(2), kit.getLastPurpose());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = action(req);
        if ("/submit".equals(action)) {
            submit(req, resp);
            return;
        }
        try {
            switch (action) {
                case "/add":
                    kit.add(longParam(req, "equipmentId"));
                    flashOk(req, "Added to your Practical Kit (" + kit.getSize() + " item" + (kit.getSize() == 1 ? "" : "s") + ").");
                    break;
                case "/remove":
                    kit.remove(longParam(req, "equipmentId"));
                    break;
                case "/clear":
                    kit.clear();
                    flashOk(req, "Kit emptied.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
        }
        String back = req.getParameter("back");
        redirect(req, resp, (back != null && back.startsWith("/") && !back.startsWith("//")) ? back : "/kit");
    }

    private void submit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDateTime start = null;
        LocalDateTime end = null;
        String purpose = str(req, "purpose");
        try {
            start = dateTimeParam(req, "start");
            end = dateTimeParam(req, "end");
            String ref = kit.submit(me.getUserId(), start, end, purpose);
            flashOk(req, "Kit " + ref + " submitted - all items reserved together.");
            redirect(req, resp, "/bookings");
        } catch (LabLoopException ex) {
            req.setAttribute("errors", ex.getMessages());
            LocalDateTime s = start == null ? BookingServlet.suggestedStart() : start;
            showKit(req, resp, s, end == null ? s.plusHours(2) : end, purpose);
        }
    }

    private void showKit(HttpServletRequest req, HttpServletResponse resp, LocalDateTime start, LocalDateTime end,
                         String purpose) throws ServletException, IOException {
        req.setAttribute("items", kit.getItems());
        req.setAttribute("maxItems", PracticalKitBean.MAX_ITEMS);
        req.setAttribute("startValue", Fmt.input(start));
        req.setAttribute("endValue", Fmt.input(end));
        req.setAttribute("minValue", Fmt.input(LocalDateTime.now().withSecond(0).withNano(0)));
        req.setAttribute("purpose", purpose);
        req.setAttribute("kitSize", kit.getSize());
        render(req, resp, "kit");
    }
}

package com.labloop.web;

import com.labloop.entity.*;
import com.labloop.service.*;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Equipment inventory - the ADD / VIEW / UPDATE / DELETE of Assignments 2-4, in one controller.
 *   GET  /equipment            list + filters
 *   GET  /equipment/view?id=   details, booking history, repairs, today's availability
 *   GET  /equipment/new        add form          (staff)
 *   GET  /equipment/edit?id=   update form       (staff)
 *   POST /equipment/save       insert or update  (staff)
 *   POST /equipment/delete     hard delete       (staff, only if no history)
 *   POST /equipment/retire     soft delete       (staff)
 *   POST /equipment/serviced   routine service done (staff)
 */
@WebServlet(urlPatterns = {"/equipment", "/equipment/*"})
public class EquipmentServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB private EquipmentService equipmentService;
    @EJB private LabService labService;
    @EJB private BookingService bookingService;
    @EJB private MaintenanceService maintenanceService;
    @Inject private PracticalKitBean kit;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (action(req)) {
                case "/":
                    list(req, resp);
                    break;
                case "/view":
                    view(req, resp);
                    break;
                case "/new":
                    if (!staffOnly(resp)) return;
                    Equipment blank = new Equipment();
                    blank.setPurchaseDate(LocalDate.now());
                    showForm(req, resp, blank, longParam(req, "labId"));
                    break;
                case "/edit":
                    if (!staffOnly(resp)) return;
                    Equipment e = equipmentService.find(longParam(req, "id"));
                    showForm(req, resp, e, e.getLab().getId());
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
        if (!staffOnly(resp)) return;
        String action = action(req);
        if ("/save".equals(action)) {
            save(req, resp);
            return;
        }
        Long id = null;
        try {
            id = longParam(req, "id");
            switch (action) {
                case "/delete":
                    equipmentService.delete(id, actor());
                    flashOk(req, "Equipment deleted.");
                    redirect(req, resp, "/equipment");
                    return;
                case "/retire":
                    equipmentService.retire(id, actor());
                    flashOk(req, "Equipment retired. It stays in the records but can no longer be booked.");
                    break;
                case "/serviced":
                    maintenanceService.markServiced(id, me.getUserId());
                    flashOk(req, "Service recorded - next due date updated.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
        }
        redirect(req, resp, id == null ? "/equipment" : "/equipment/view?id=" + id);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long labId = longParam(req, "labId");
        EquipmentStatus status = enumParam(req, "status", EquipmentStatus.class);
        EquipmentCategory category = enumParam(req, "category", EquipmentCategory.class);
        String q = str(req, "q");
        req.setAttribute("items", equipmentService.search(labId, status, category, q));
        req.setAttribute("labs", labService.findAll());
        req.setAttribute("statuses", EquipmentStatus.values());
        req.setAttribute("categories", EquipmentCategory.values());
        req.setAttribute("fLab", labId);
        req.setAttribute("fStatus", status);
        req.setAttribute("fCategory", category);
        req.setAttribute("fQ", q);
        req.setAttribute("kit", kit);
        render(req, resp, "equipment-list");
    }

    private void view(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = longParam(req, "id");
        Equipment e = equipmentService.find(id);
        LocalDate day = dateParam(req, "date");
        if (day == null) day = LocalDate.now();
        req.setAttribute("e", e);
        req.setAttribute("bookings", bookingService.forEquipment(id));
        req.setAttribute("tickets", maintenanceService.forEquipment(id));
        req.setAttribute("day", day);
        req.setAttribute("dayLabel", com.labloop.util.Fmt.date(day));
        req.setAttribute("prevDay", day.minusDays(1));
        req.setAttribute("nextDay", day.plusDays(1));
        req.setAttribute("taken", bookingService.takenOn(id, day));
        req.setAttribute("inKit", kit.contains(id));
        render(req, resp, "equipment-view");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Equipment e, Long labId)
            throws ServletException, IOException {
        req.setAttribute("form", e);
        req.setAttribute("labId", labId);
        req.setAttribute("labs", labService.findAll());
        req.setAttribute("categories", EquipmentCategory.values());
        render(req, resp, "equipment-form");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Equipment form = new Equipment();
        Long id = null;
        Long labId = null;
        try {
            id = longParam(req, "id");
            labId = longParam(req, "labId");
            form.setId(id);
            form.setAssetTag(str(req, "assetTag"));
            form.setName(str(req, "name"));
            form.setModel(str(req, "model"));
            form.setCategory(enumParam(req, "category", EquipmentCategory.class));
            form.setPurchaseDate(dateParam(req, "purchaseDate"));
            form.setPurchaseCost(doubleParam(req, "purchaseCost", 0));
            form.setServiceIntervalDays(intParam(req, "serviceIntervalDays", 180));
            form.setLastServicedOn(dateParam(req, "lastServicedOn"));
            form.setNotes(str(req, "notes"));

            Equipment saved = (id == null)
                    ? equipmentService.create(form, labId, actor())
                    : equipmentService.update(id, form, labId, actor());
            flashOk(req, saved.getAssetTag() + " saved.");
            redirect(req, resp, "/equipment/view?id=" + saved.getId());
        } catch (LabLoopException ex) {
            // Re-show the form with what the user typed plus the error list.
            if (id != null) {
                try { form.setAssetTag(equipmentService.find(id).getAssetTag()); } catch (LabLoopException ignored) { }
            }
            req.setAttribute("errors", ex.getMessages());
            showForm(req, resp, form, labId);
        }
    }
}

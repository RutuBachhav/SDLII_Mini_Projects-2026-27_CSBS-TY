package com.labloop.web;

import com.labloop.entity.Lab;
import com.labloop.service.EquipmentService;
import com.labloop.service.LabService;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Lab rooms - the "Department" side of the One-to-Many relationship. */
@WebServlet(urlPatterns = {"/labs", "/labs/*"})
public class LabServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @EJB private LabService labService;
    @EJB private EquipmentService equipmentService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (action(req)) {
                case "/":
                    req.setAttribute("labs", labService.findAll());
                    req.setAttribute("counts", labService.equipmentCounts());
                    render(req, resp, "lab-list");
                    break;
                case "/view":
                    Lab lab = labService.find(longParam(req, "id"));
                    req.setAttribute("lab", lab);
                    req.setAttribute("items", equipmentService.search(lab.getId(), null, null, null));
                    render(req, resp, "lab-view");
                    break;
                case "/new":
                    if (!staffOnly(resp)) return;
                    req.setAttribute("form", new Lab());
                    render(req, resp, "lab-form");
                    break;
                case "/edit":
                    if (!staffOnly(resp)) return;
                    req.setAttribute("form", labService.find(longParam(req, "id")));
                    render(req, resp, "lab-form");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (LabLoopException ex) {
            flashError(req, ex);
            redirect(req, resp, "/labs");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!staffOnly(resp)) return;
        String action = action(req);
        if ("/delete".equals(action)) {
            try {
                labService.delete(longParam(req, "id"), actor());
                flashOk(req, "Lab deleted.");
            } catch (LabLoopException ex) {
                flashError(req, ex);
            }
            redirect(req, resp, "/labs");
            return;
        }
        if (!"/save".equals(action)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Lab form = new Lab();
        try {
            Long id = longParam(req, "id");
            form.setId(id);
            form.setCode(str(req, "code"));
            form.setName(str(req, "name"));
            form.setLocation(str(req, "location"));
            form.setCapacity(intParam(req, "capacity", 0));
            form.setInCharge(str(req, "inCharge"));
            Lab saved = id == null ? labService.create(form, actor()) : labService.update(id, form, actor());
            flashOk(req, "Lab " + saved.getCode() + " saved.");
            redirect(req, resp, "/labs/view?id=" + saved.getId());
        } catch (LabLoopException ex) {
            req.setAttribute("errors", ex.getMessages());
            req.setAttribute("form", form);
            render(req, resp, "lab-form");
        }
    }
}

package com.web;

import com.entity.Subject;
import com.service.SubjectService;
import com.service.SubjectService.DeletePolicy;
import com.service.TaskService;
import com.util.ValidationException;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject management: the cards with their progress, the add and edit forms,
 * and the two-step delete.
 *
 * Deleting is two requests on purpose. The GET shows how many tasks are
 * attached and makes the student choose between moving them to another subject
 * and deleting them; only the POST carries that choice out. A subject with no
 * tasks still goes through the same confirmation, so nothing is destroyed by a
 * single click on a link somebody else sent.
 */
@WebServlet(name = "SubjectServlet", urlPatterns = {"/subjects", "/subjects/*"})
public class SubjectServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    /** The swatches offered on the subject form. Ink colours, no purple. */
    private static final String[] PALETTE = {
        "#1d4ed8", "#0f766e", "#b45309", "#b91c1c",
        "#4d7c0f", "#0369a1", "#9d174d", "#64748b"
    };

    private final SubjectService subjects = new SubjectService();
    private final TaskService tasks = new TaskService();

    // -- GET ----------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long userId = Web.requireUserId(req);

        switch (action(req)) {
            case "":
                list(req, res, userId);
                break;
            case "new":
                newForm(req, res, userId);
                break;
            case "edit":
                editForm(req, res, userId);
                break;
            case "delete":
                confirmDelete(req, res, userId);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        req.setAttribute("subjects", subjects.listWithProgress(userId));
        req.setAttribute("stats", tasks.stats(userId));
        req.setAttribute("welcome", req.getParameter("welcome") != null);
        req.setAttribute("page", "subjects");
        render(req, res, "subjects");
    }

    private void newForm(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        req.setAttribute("formTitle", "New subject");
        req.setAttribute("formAction", "/subjects/create");
        req.setAttribute("color", PALETTE[0]);
        showForm(req, res);
    }

    private void editForm(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        Subject subject = subjects.requireOwned(Web.requireId(req, "id"), userId);
        req.setAttribute("formTitle", "Edit subject");
        req.setAttribute("formAction", "/subjects/update");
        req.setAttribute("subjectId", subject.getId());
        req.setAttribute("name", subject.getName());
        req.setAttribute("color", subject.getDisplayColor());
        showForm(req, res);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setAttribute("palette", PALETTE);
        req.setAttribute("page", "subjects");
        render(req, res, "subject-form");
    }

    /** The delete confirmation page, with the choice of what happens to tasks. */
    private void confirmDelete(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        Subject subject = subjects.requireOwnedWithCounts(Web.requireId(req, "id"), userId);

        List<Subject> others = new ArrayList<>();
        for (Subject s : subjects.listForUser(userId)) {
            if (!s.getId().equals(subject.getId())) {
                others.add(s);
            }
        }

        req.setAttribute("subject", subject);
        req.setAttribute("otherSubjects", others);
        req.setAttribute("page", "subjects");
        render(req, res, "subject-delete");
    }

    // -- POST ---------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long userId = Web.requireUserId(req);

        switch (action(req)) {
            case "create":
                create(req, res, userId);
                break;
            case "update":
                update(req, res, userId);
                break;
            case "delete":
                delete(req, res, userId);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        try {
            Subject subject = subjects.create(userId,
                    req.getParameter("name"), req.getParameter("color"));
            Web.flashSuccess(req, "Added the subject " + subject.getName() + ".");
            redirect(req, res, Web.safePath(req.getParameter("returnTo"), "/subjects"));
        } catch (ValidationException e) {
            keepSubmitted(req);
            req.setAttribute("formTitle", "New subject");
            req.setAttribute("formAction", "/subjects/create");
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            showForm(req, res);
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        long id = Web.requireId(req, "id");
        try {
            Subject subject = subjects.update(id, userId,
                    req.getParameter("name"), req.getParameter("color"));
            Web.flashSuccess(req, "Saved " + subject.getName() + ".");
            redirect(req, res, "/subjects");
        } catch (ValidationException e) {
            keepSubmitted(req);
            req.setAttribute("formTitle", "Edit subject");
            req.setAttribute("formAction", "/subjects/update");
            req.setAttribute("subjectId", id);
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            showForm(req, res);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        long id = Web.requireId(req, "id");
        DeletePolicy policy = "move".equalsIgnoreCase(req.getParameter("policy"))
                ? DeletePolicy.MOVE : DeletePolicy.DELETE;
        Long moveTo = Web.optionalId(req, "moveTo");

        // Read the name before the row is gone, for the message.
        Subject subject = subjects.requireOwned(id, userId);
        String name = subject.getName();

        try {
            int affected = subjects.delete(id, userId, policy, moveTo);
            String detail;
            if (affected == 0) {
                detail = "";
            } else if (policy == DeletePolicy.MOVE) {
                detail = " " + affected + (affected == 1 ? " task was" : " tasks were") + " moved.";
            } else {
                detail = " " + affected + (affected == 1 ? " task was" : " tasks were")
                        + " deleted with it.";
            }
            Web.flashSuccess(req, "Deleted " + name + "." + detail);
            redirect(req, res, "/subjects");
        } catch (ValidationException e) {
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            confirmDelete(req, res, userId);
        }
    }

    private void keepSubmitted(HttpServletRequest req) {
        req.setAttribute("name", Web.strOrEmpty(req, "name"));
        String color = Web.str(req, "color");
        req.setAttribute("color", color == null ? PALETTE[0] : color);
    }
}

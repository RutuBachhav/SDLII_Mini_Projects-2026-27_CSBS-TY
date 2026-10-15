package com.web;

import com.dao.AuditDao;
import com.dao.CategoryDao;
import com.dao.ItemDao;
import com.dao.MatchDao;
import com.dao.PhotoDao;
import com.entity.Item;
import com.entity.User;
import com.service.MatchService;
import com.service.Storage;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creating, editing and closing a report.
 *
 * Reporting is the one place where a photograph can be uploaded, so this is the
 * only multipart endpoint in the application.
 */
@WebServlet({"/report", "/items/edit", "/items/close", "/items/reopen"})
@MultipartConfig(fileSizeThreshold = 512 * 1024,
                 maxFileSize = Storage.MAX_BYTES,
                 maxRequestSize = Storage.MAX_BYTES + 1024 * 1024)
public class ReportServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final CategoryDao categories = new CategoryDao();
    private final PhotoDao photos = new PhotoDao();
    private final MatchDao matches = new MatchDao();
    private final MatchService matching = new MatchService();
    private final Storage storage = new Storage();
    private final AuditDao audit = new AuditDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        try {
            if ("/items/edit".equals(request.getServletPath())) {
                Item item = items.findById(Web.num(request, "id", 0));
                if (item == null) {
                    notFound(request, response, "That report");
                    return;
                }
                if (!canEdit(user, item)) {
                    forbidden(request, response);
                    return;
                }
                request.setAttribute("item", item);
                request.setAttribute("editing", true);
            } else {
                String type = "FOUND".equalsIgnoreCase(Web.str(request, "type")) ? "FOUND" : "LOST";
                Item blank = new Item();
                blank.setType(type);
                blank.setItemDate(LocalDate.now());
                request.setAttribute("item", blank);
            }
            request.setAttribute("categories", categories.listActive());
            render(request, response, "report-form.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/items/close" -> closeReport(request, response, user, "CLOSED");
                case "/items/reopen" -> closeReport(request, response, user, "OPEN");
                case "/items/edit" -> save(request, response, user, true);
                default -> save(request, response, user, false);
            }
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    // ------------------------------------------------------------------ create/edit

    private void save(HttpServletRequest request, HttpServletResponse response, User user,
                      boolean editing) throws ServletException, IOException, SQLException {
        Item item = editing ? items.findById(Web.num(request, "id", 0)) : new Item();
        if (editing) {
            if (item == null) {
                notFound(request, response, "That report");
                return;
            }
            if (!canEdit(user, item)) {
                forbidden(request, response);
                return;
            }
        } else {
            item.setReporterId(user.getId());
            item.setType("FOUND".equalsIgnoreCase(Web.str(request, "type")) ? "FOUND" : "LOST");
        }

        item.setTitle(Web.clip(Web.str(request, "title"), 120));
        item.setCategoryId(Web.num(request, "categoryId", 0));
        item.setDescription(Web.str(request, "description"));
        item.setColor(Web.clip(Web.str(request, "color"), 40));
        item.setPrivateMarks(Web.clip(Web.str(request, "privateMarks"), 255));
        item.setLocation(Web.clip(Web.str(request, "location"), 120));

        List<String> errors = new ArrayList<>();
        if (item.getTitle() == null || item.getTitle().length() < 4) {
            errors.add("Give the report a title of at least 4 characters.");
        }
        if (item.getDescription() == null || item.getDescription().length() < 15) {
            errors.add("Describe the item in at least 15 characters.");
        }
        if (item.getDescription() != null && item.getDescription().length() > 4000) {
            errors.add("The description is too long.");
        }
        if (item.getLocation() == null || item.getLocation().length() < 3) {
            errors.add("Say where the item was lost or found.");
        }
        if (!categories.exists(item.getCategoryId())) {
            errors.add("Choose a category.");
        }
        LocalDate date = parseDate(Web.str(request, "itemDate"));
        if (date == null) {
            errors.add("Enter a valid date.");
        } else if (date.isAfter(LocalDate.now())) {
            errors.add("The date cannot be in the future.");
        } else if (date.isBefore(LocalDate.now().minusYears(2))) {
            errors.add("That date is more than two years ago.");
        } else {
            item.setItemDate(date);
        }

        Part photo = request.getPart("photo");
        boolean hasUpload = photo != null && photo.getSize() > 0;

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("item", item);
            request.setAttribute("editing", editing);
            request.setAttribute("categories", categories.listActive());
            render(request, response, "report-form.jsp");
            return;
        }

        if (editing) {
            items.update(item);
        } else {
            item.setId(items.insert(item));
        }

        if (hasUpload) {
            try {
                Storage.Stored stored = storage.save(photo, "items");
                photos.deleteForItem(item.getId());
                photos.insert(item.getId(), stored.key, stored.contentType, stored.size);
            } catch (Storage.Rejected rejected) {
                // The report is already saved; only the image failed, and saying
                // so is more useful than throwing the whole form away.
                redirect(request, response, "/items/view?id=" + item.getId(), "error",
                        "The report was saved, but the photo was not accepted: "
                        + rejected.getMessage());
                return;
            }
        }

        audit.log(user.getId(), editing ? "REPORT_UPDATED" : "REPORT_CREATED",
                "item", item.getId(), item.getType() + ": " + item.getTitle());

        // Suggestions are recomputed here, which is what keeps them in step with
        // an edited description or a corrected date.
        Item saved = items.findById(item.getId());
        int found = matching.recomputeFor(saved);

        redirect(request, response, "/items/view?id=" + item.getId(), "success",
                (editing ? "Report updated." : "Report published.")
                + (found > 0
                        ? " FoundIt also lined up " + found
                          + (found == 1 ? " possible match." : " possible matches.")
                        : ""));
    }

    // ------------------------------------------------------------------ close/reopen

    private void closeReport(HttpServletRequest request, HttpServletResponse response, User user,
                             String target) throws ServletException, IOException, SQLException {
        Item item = items.findById(Web.num(request, "id", 0));
        if (item == null) {
            notFound(request, response, "That report");
            return;
        }
        if (!canEdit(user, item)) {
            forbidden(request, response);
            return;
        }
        if ("RESOLVED".equals(item.getStatus())) {
            redirect(request, response, "/items/view?id=" + item.getId(), "error",
                    "This report was resolved through a return and cannot be changed.");
            return;
        }
        boolean done = "CLOSED".equals(target)
                ? items.setStatus(item.getId(), "CLOSED", "OPEN")
                : items.setStatus(item.getId(), "OPEN", "CLOSED");
        if (!done) {
            redirect(request, response, "/items/view?id=" + item.getId(), "error",
                    "That change does not apply to a report that is already "
                    + item.getStatus().toLowerCase() + ".");
            return;
        }
        if ("CLOSED".equals(target)) {
            // A closed report must not keep suggesting itself to anybody.
            matches.deleteForItem(item.getId());
        } else {
            matching.recomputeFor(items.findById(item.getId()));
        }
        audit.log(user.getId(), "CLOSED".equals(target) ? "REPORT_CLOSED" : "REPORT_REOPENED",
                "item", item.getId(), item.getTitle());
        redirect(request, response, "/items/view?id=" + item.getId(), "success",
                "CLOSED".equals(target)
                        ? "Report closed. It no longer appears in search or matching."
                        : "Report reopened.");
    }

    private boolean canEdit(User user, Item item) {
        return user != null && (user.isAdmin() || user.getId() == item.getReporterId());
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }
}

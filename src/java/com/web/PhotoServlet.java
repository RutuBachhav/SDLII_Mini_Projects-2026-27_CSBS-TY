package com.web;

import com.dao.PhotoDao;
import com.service.Storage;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * The only way a stored file reaches a browser.
 *
 * The request names a photo id, never a path, so there is nothing in it for a
 * traversal attempt to work on. The id is looked up in MySQL, the stored key is
 * resolved inside the uploads directory, and the content type sent back is the
 * one recorded at upload time rather than anything the request asked for.
 */
@WebServlet("/photo")
public class PhotoServlet extends HttpServlet {

    private final PhotoDao photos = new PhotoDao();
    private final Storage storage = new Storage();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Web.num(request, "id", 0);
        if (id <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            PhotoDao.Photo photo = photos.findById(id);
            if (photo == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Path file = storage.resolve(photo.storageKey);
            if (!Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(photo.contentType);
            response.setContentLengthLong(Files.size(file));
            response.setHeader("Cache-Control", "private, max-age=600");
            response.setHeader("Content-Disposition", "inline");
            response.setHeader("X-Content-Type-Options", "nosniff");
            try (OutputStream out = response.getOutputStream()) {
                Files.copy(file, out);
            }
        } catch (Storage.Rejected rejected) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            log("Could not read photo " + id, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

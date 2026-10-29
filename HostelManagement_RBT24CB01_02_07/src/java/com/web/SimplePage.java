package com.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Small standalone page for login, setup and error states that must not use
 * the authenticated application chrome.
 */
public final class SimplePage {

    private SimplePage() {
    }

    public static void render(HttpServletRequest request,
                              HttpServletResponse response,
                              int status,
                              String title,
                              String heading,
                              String message,
                              String linkPath,
                              String linkText)
            throws IOException {

        response.setStatus(status);
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + Layout.esc(title) + " - Hostel Management</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/css/style.css\">"
                + "</head><body class=\"auth-body\">"
                + "<div class=\"auth-wrap\"><div class=\"auth-card\">"
                + "<div class=\"auth-brand\"><span class=\"mark\">&#127976;</span>"
                + "<h1>Hostel Management</h1></div>"
                + "<h2>" + Layout.esc(heading) + "</h2>"
                + "<p class=\"auth-text\">" + Layout.esc(message) + "</p>"
                + "<a class=\"btn btn-primary btn-block\" href=\"" + ctx
                + Layout.esc(linkPath) + "\">" + Layout.esc(linkText)
                + "</a>"
                + "</div></div></body></html>");
    }
}

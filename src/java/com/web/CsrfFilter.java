package com.web;

import com.util.Web;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Every state-changing request must carry this session's CSRF token.
 *
 * Ordinary forms send it in a hidden field. A file-upload form sends it in the
 * query string instead, because reading a parameter out of a multipart body in
 * a filter would consume the body before the servlet can read the file.
 */
@WebFilter(urlPatterns = "/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(Web.SESSION_CSRF);
        String presented = presentedToken(request);

        if (expected == null || presented == null || !constantTimeEquals(expected, presented)) {
            // 403 and an explanation, rather than a redirect, so that a missing or
            // stale token is visible as a refusal instead of looking like success.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorTitle", "That form could not be submitted");
            request.setAttribute("errorDetail",
                    "The form had expired or did not come from FoundIt, so nothing was changed. "
                    + "Open the page again and resubmit it.");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
            return;
        }
        chain.doFilter(req, res);
    }

    private static String presentedToken(HttpServletRequest request) {
        String type = request.getContentType();
        if (type != null && type.toLowerCase().startsWith("multipart/")) {
            return fromQueryString(request.getQueryString());
        }
        return request.getParameter("csrf");
    }

    private static String fromQueryString(String query) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && "csrf".equals(pair.substring(0, eq))) {
                return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}

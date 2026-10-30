package hackmate;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sends visitors who are not logged in to the login page.
 *
 * <p>Only the pages that need an account are protected, the public pages such
 * as the home page and the team list stay open for everybody.</p>
 */
@WebFilter(urlPatterns = {
        "/dashboard",
        "/create-team",
        "/team-requests",
        "/profile"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to set up.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (SessionUtil.isLoggedIn(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Remember where the visitor wanted to go, so the login page can
        // send them there afterwards.
        httpRequest.getSession().setAttribute("redirectAfterLogin",
                httpRequest.getRequestURI() + queryString(httpRequest));

        httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
    }

    @Override
    public void destroy() {
        // Nothing to clean up.
    }

    private static String queryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? "" : "?" + query;
    }
}

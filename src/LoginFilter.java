import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> employeeURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            System.out.println("- allowed without login");
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            System.out.println("- not allowed without login");
            httpResponse.sendRedirect("login.html");
        } else {
            System.out.println("- already logged in as user");
            // Check if this URL is allowed to access without logging in as employee
            if (this.isUrlAllowedWithoutLoginAsEmployee(httpRequest.getRequestURI())) {
                // Keep default action: pass along the filter chain
                System.out.println("- allowed without login as employee");
                chain.doFilter(request, response);
                return;
            }

            // Redirect to login page if the "user" attribute isn't employee
            if (!((User) httpRequest.getSession().getAttribute("user")).isEmployee()) {
                System.out.println("- not allowed without login as employee");
                httpResponse.sendRedirect("login.html");
            } else {
                System.out.println("- allowed due to already logged in as employee");
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        if (requestURI.contains("_dashboard"))
            return false;
        else
            return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlAllowedWithoutLoginAsEmployee(String requestURI) {
        return !employeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("styles-login.css");
        allowedURIs.add("form-recaptcha");
        allowedURIs.add("api/movielist");

        employeeURIs.add("_dashboard/index.html");
        employeeURIs.add("_dashboard/index.js");
        employeeURIs.add("_dashboard/movie.html");
        employeeURIs.add("_dashboard/movie.js");
        employeeURIs.add("_dashboard/star.html");
        employeeURIs.add("_dashboard/star.js");

    }

    public void destroy() {
        // ignored.
    }

}

package ch.mno.copper.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Filter which validate that /admin/* are only authorized if adminHeader is empty or adminHeader is present and fully matching adminRegex.
 * Exemple:
 * copper.security.adminHeader=my-app-roles
 * copper.security.adminRegex=.*refmon-admin
 */
public class CustomAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationFilter.class);

    private String adminHeader;
    private String adminRegex;

    public CustomAuthenticationFilter(String adminHeader, String adminRegex) {
        this.adminHeader = adminHeader;
        this.adminRegex = adminRegex;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        LOG.debug("Auth filter for {}, adminHeader=[{}], adminRegex=[{}]", httpServletRequest.getRequestURI(), adminHeader, adminRegex);
        if (httpServletRequest.getRequestURI().contains("/admin/")) {
            if (adminHeader==null || adminHeader.isEmpty() || headerOk(httpServletRequest)) {
                LOG.info("auth filter: access ok");
//                Authentication auth = new PreAuthenticatedAuthenticationToken(httpServletRequest.getHeader(adminHeader), "admin header auth");
//                SecurityContextHolder.setContext(new SecurityContextImpl(auth));
                filterChain.doFilter(request, response);
            } else {
                LOG.info("Auth filter access denied for {}, adminHeader=[{}], adminRegex=[{}]", httpServletRequest.getRequestURI(), adminHeader, adminRegex);
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to admin resource without valid authentication");
            }
        } else {
            LOG.debug("auth filter: access ok (no /admin/)");
            filterChain.doFilter(request, response);
        }
    }

    private boolean headerOk(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(adminHeader);
        if (header==null) {
            return false; // Header must be present
        }

        return Pattern.compile(adminRegex).matcher(header).matches();
    }

}
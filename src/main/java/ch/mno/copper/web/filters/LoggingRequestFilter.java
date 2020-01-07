package ch.mno.copper.web.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

public class LoggingRequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger("copper_audit");

    @Autowired
    private SecurityContext securityContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (securityContext != null) {
            String principal = securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName();
            LOG.info("[" + principal + "] " + request.getMethod() + " " + request.getRequestURI()
                    + " -> status=" + response.getStatus() );//+ " l=" + response.getLength());
        }
    }

}
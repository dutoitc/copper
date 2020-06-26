package ch.mno.copper.web.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.SecurityContext;

public class LoggingRequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger("copper_audit");

    @Autowired
    private SecurityContext securityContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        if (securityContext != null) {
            String principal = securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName();
            LOG.info("[{}] {} {} -> status={}", principal, request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }

}
package ch.mno.copper.web.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

public class LoggingRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger("copper_audit");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext!=null) {
            String principal = securityContext.getUserPrincipal()==null?"?":securityContext.getUserPrincipal().getName();
            LOG.info("[" + principal + "] "  + requestContext.getMethod() + " " + requestContext.getUriInfo().getPath(true)
              + " -> status=" + responseContext.getStatus() + " l=" + responseContext.getLength());
        }
    }

}
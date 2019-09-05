package ch.mno.copper.web.filters;

import ch.mno.copper.CopperMediator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// See https://simplapi.wordpress.com/2015/09/19/jersey-jax-rs-securitycontext-in-action/
@Provider
public class RestrictedOperationsRequestFilter implements ContainerRequestFilter {
     
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String method = ctx.getMethod();
        String path = ctx.getUriInfo().getPath(true);

        // Allow WADL
        List<String> ALLOWED = Arrays.asList("application.wadl", "ws/ping", "ws/values", "ws/overview", "ws/values/query/png");
        if(method.equals("GET")) {
            for (String allowed: ALLOWED) {
                if (allowed.equals(path)) {
                    return; // Allowed
                }
            }
        }

        String auth = ctx.getHeaderString("authorization");
        if(auth == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"realm\"")
                    .entity("Page requires login.").build());
        }

        // Check login/password
        String[] lap = BasicAuth.decode(auth);
        if(lap == null || lap.length != 2) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        String users = CopperMediator.getInstance().getProperty("adminUsers");
        if (users==null) throw new RuntimeException("Missing 'adminUsers=user1:pass1;user2:pass2...' in properties");
        String[] passes = users.split(";");
        String ident = lap[0] + ":" + lap[1];
        boolean ok=false;
        for (String tuple: passes) {
            if (tuple.equals(ident)) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }
}
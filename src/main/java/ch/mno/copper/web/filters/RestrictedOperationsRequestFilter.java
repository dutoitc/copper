package ch.mno.copper.web.filters;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;

// See https://simplapi.wordpress.com/2015/09/19/jersey-jax-rs-securitycontext-in-action/

/**
 * Read basic-auth, store as cookie
 */
@Provider
public class RestrictedOperationsRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

    }

    //    private static final List<String> NO_AUTH_PATH = Arrays.asList("application.wadl", "ws/ping", "ws/values", "ws/overview", "ws/values/query/png");
//    private static final String COOKIE_NAME = "CopperAuth";
//    private static final int COOKIE_VALIDITY_MIN = 60;
//
//    @Context
//    private HttpServletRequest httpRequest;
//
//    @Override
//    public void filter(ContainerRequestContext ctx) throws IOException {
//        String method = ctx.getMethod();
//        String path = ctx.getUriInfo().getPath(true);
//
//        // Allow WADL, ping, ... without auth
//        if(method.equals("GET") && doesNotNeedAuth(path)) {
//            return;
//        }
//
//        // Allow valid cookies
//        Cookie cookie = ctx.getCookies().get(COOKIE_NAME);
//        if (cookie!=null && isCookieValid(cookie)) {
//            String auth = ctx.getHeaderString("authorization");
//            ctx.setSecurityContext(buildSecurityContext(auth));
//            return; // ok
//        }
//
//        // Read header 'authorization'
//        try {
//            String auth = ctx.getHeaderString("authorization");
//            if (auth == null) {
//                requireAuth(ctx);
//            } else {
//                if (isAuthValid(auth)) {
//                    Date expirationDate = new Date(new Date().getTime() + 1000 * 60 * COOKIE_VALIDITY_MIN);
//                    String token = UUID.randomUUID().toString();
//                    AuthStorage.addToken(token, expirationDate);
//                    ctx.setProperty("Cookie", new Cookie(COOKIE_NAME, token));
//                    ctx.setProperty("SetCookie", "true");
//                    ctx.setSecurityContext(buildSecurityContext(auth));
//                    return; // Ok
//                } else {
//                    requireAuth(ctx);
//                }
//            }
//        } catch (Exception e) {
//            ctx.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error: " + e.getMessage()).build());
//        }
//
//    }
//
//
//    private SecurityContext buildSecurityContext(String auth) {
//        String[] lap = BasicAuth.decode(auth);
//        assert lap!=null;
//        assert lap.length==2;
//        SecurityContext context = new CopperSecurityContext(lap[0], lap[1]);
//        return context;
//    }
//
//    @Override
//    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
//        //
//        if (requestContext.getProperty("SetCookie")!=null) {
//            Cookie cookie = (Cookie) requestContext.getProperty("Cookie");
//            //responseContext.getCookies().put(COOKIE_NAME, new NewCookie(cookie, "", COOKIE_VALIDITY_MIN*60, false));
//            //
//            responseContext.getHeaders().add("Set-Cookie", new NewCookie(cookie, "", COOKIE_VALIDITY_MIN*60, false));
//        }
//    }
//
//
//    public boolean doesNotNeedAuth(String path) {
//        for (String allowedPath: NO_AUTH_PATH) {
//            if (allowedPath.equals(path)) {
//                return true; // Allowed
//            }
//        }
//        return false;
//    }
//
//    public void requireAuth(ContainerRequestContext ctx) {
//        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
//                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic")
//                .entity("Page requires login.").build());
//    }
//
//    private boolean isCookieValid(Cookie cookie) {
//        return AuthStorage.isTokenValid(cookie.getValue());
//    }
//
//
//    private boolean isAuthValid(String auth) {
//        // Check login/password
//        String[] lap = BasicAuth.decode(auth);
//        if (lap == null || lap.length != 2) {
//            return false;
//        }
//        String users = CopperMediator.getInstance().getProperty("adminUsers");
//        if (users == null)
//            throw new RuntimeException("Missing 'adminUsers=user1:pass1;user2:pass2...' in properties");
//        String[] passes = users.split(";");
//        String ident = lap[0] + ":" + lap[1];
//        for (String tuple : passes) {
//            if (tuple.equals(ident)) {
//                return true;
//            }
//        }
//        return false;
//    }


}
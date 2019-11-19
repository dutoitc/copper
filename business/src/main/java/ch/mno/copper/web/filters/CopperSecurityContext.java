package ch.mno.copper.web.filters;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class CopperSecurityContext implements SecurityContext {

    private String username;
    private String role;

    public CopperSecurityContext(String username, String role) {
        this.username = username;
        this.role = role;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String role) {
        return role.equals(this.role);
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "BASIC";
    }

}
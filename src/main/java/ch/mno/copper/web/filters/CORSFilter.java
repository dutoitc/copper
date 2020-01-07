package ch.mno.copper.web.filters;

import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.*;
import java.io.IOException;

@Service
public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

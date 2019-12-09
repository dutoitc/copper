package ch.mno.copper.web.filters;

import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.*;
import java.io.IOException;

@Service
public class CORSFilter implements Filter {

    /**
     * Constructor accepting a {@link CorsConfigurationSource} used by the filter
     * to find the {@link CorsConfiguration} to use for each incoming request.
     *
     * @param configSource
     * @see UrlBasedCorsConfigurationSource
     */
//    public CORSFilter(CorsConfigurationSource configSource) {
//        super(configSource);
//    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

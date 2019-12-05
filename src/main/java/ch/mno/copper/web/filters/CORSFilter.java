package ch.mno.copper.web.filters;

import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Service
public class CORSFilter extends CorsFilter {

    /**
     * Constructor accepting a {@link CorsConfigurationSource} used by the filter
     * to find the {@link CorsConfiguration} to use for each incoming request.
     *
     * @param configSource
     * @see UrlBasedCorsConfigurationSource
     */
    public CORSFilter(CorsConfigurationSource configSource) {
        super(configSource);
    }
}

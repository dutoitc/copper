package ch.mno.copper.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurity {


    @Value("${copper.security.adminHeader:#{null}}")
    private String adminHeader;

    @Value("${copper.security.adminRegex:#{null}}")
    private String adminRegex;


    // Delegate to CustomAuthenticationFilter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.addFilterAfter(new CustomAuthenticationFilter(adminHeader, adminRegex), WebAsyncManagerIntegrationFilter.class)
                .csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

}

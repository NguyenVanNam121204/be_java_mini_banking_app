package com.bankapp.bankingapp.infrastructure.config;

import com.bankapp.bankingapp.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.bankapp.bankingapp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.bankapp.bankingapp.infrastructure.security.ratelimit.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final Environment environment;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            Environment environment) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Check if running in dev/test environment
        boolean isDevOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equals("dev") || profile.equals("test") || profile.equals("local"));

        // If no active profiles, default to dev (for development)
        boolean allowSwagger = isDevOrTest || environment.getActiveProfiles().length == 0;

        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentTypeOptions(contentType -> contentType.disable())
                        .xssProtection(xss -> xss.disable())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**").permitAll();

                    // Only allow Swagger in dev/test/local environments
                    if (allowSwagger) {
                        auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                    } else {
                        auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").denyAll();
                    }

                    auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

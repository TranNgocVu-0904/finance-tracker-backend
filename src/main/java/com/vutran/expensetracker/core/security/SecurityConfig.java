package com.vutran.expensetracker.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Suggested for professional logging
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; 
import org.springframework.security.config.Customizer; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j // Uses SLF4J for cleaner logging
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    // Defines the password hashing algorithm used across the application.
     
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    // Configures the security filter chain, including CORS, CSRF, and route-based authorization.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain: Configuring CORS, CSRF, and JWT Filter...");

        http
            // Enable Cross-Origin Resource Sharing (CORS) using the source defined below
            .cors(Customizer.withDefaults()) 
            
            // Disable CSRF for stateless REST APIs using JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Exempt public endpoints (Authentication, Password Recovery, and API Documentation)
                .requestMatchers(
                    "/users/register", 
                    "/users/login",
                    "/users/forgot-password",  // Public endpoint for email recovery requests
                    "/users/reset-password",   // Public endpoint for final password reset
                    "/v3/api-docs/**",         // OpenAPI/Swagger JSON documentation
                    "/swagger-ui/**",          // Swagger UI static resources
                    "/swagger-ui.html"         // Swagger UI entry point
                ).permitAll()
                
                // All other requests require a valid JWT token
                .anyRequest().authenticated()
            )
            
            // Register the custom JWT authentication filter before the standard auth filter
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    

    
    // Defines the global CORS policy to allow cross-origin requests from specific frontend environments
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Define permitted origins (Local development and Production Vercel app)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500", 
            "http://localhost:5500",
            "https://finance-tracker-frontend-theta-brown.vercel.app"
        ));
        
        // Allowed HTTP Methods for REST communication
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allowed Headers required for JWT-based communication and JSON payloads
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        
        // Enable credential support (cookies, authorization headers, or TLS client certificates)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
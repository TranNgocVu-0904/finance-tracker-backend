package com.vutran.expensetracker.core.security;

import lombok.RequiredArgsConstructor;

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
public class SecurityConfig {

    

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("-----> SecurityConfig is configuring CORS and Auth... <-----");

        http
            // 1. Kích hoạt cấu hình CORS (phải có dòng này thì @CrossOrigin mới chạy được)
            .cors(Customizer.withDefaults()) 
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Cập nhật dòng này: Cho phép cả Đăng ký, Đăng nhập, Quên mật khẩu và Reset mật khẩu
                .requestMatchers(
                    "/users/register", 
                    "/users/login",
                    "/users/forgot-password", // Cho phép mọi người gửi yêu cầu lấy mail
                    "/users/reset-password"   // Cho phép mọi người gửi mật khẩu mới kèm Token
                ).permitAll()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    

    // 3. ĐỊNH NGHĨA QUY TẮC CORS CHO TOÀN BỘ HỆ THỐNG
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500", 
            "http://localhost:5500",
            "https://finance-tracker-frontend-theta-brown.vercel.app"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
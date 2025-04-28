package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép các origin cụ thể
        config.addAllowedOrigin("http://localhost:3000"); // Frontend React local
        config.addAllowedOrigin("http://localhost:5173"); // Frontend Vite local
        config.addAllowedOrigin("https://apartment-management-vpc4.onrender.com"); // Frontend production
        
        // Cho phép credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Cho phép các headers
        config.addAllowedHeader("*");
        
        // Cho phép các methods
        config.addAllowedMethod("*"); // Cho phép tất cả methods
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 
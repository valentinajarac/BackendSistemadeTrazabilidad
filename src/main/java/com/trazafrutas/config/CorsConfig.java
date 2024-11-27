package com.trazafrutas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Configurar orígenes permitidos
        Arrays.stream(allowedOrigins.split(","))
              .map(String::trim)
              .forEach(config::addAllowedOrigin);

        // Configurar métodos permitidos
        Arrays.stream(allowedMethods.split(","))
              .map(String::trim)
              .forEach(config::addAllowedMethod);

        // Configurar headers permitidos
        Arrays.stream(allowedHeaders.split(","))
              .map(String::trim)
              .forEach(config::addAllowedHeader);

        // Configurar headers expuestos
        Arrays.stream(exposedHeaders.split(","))
              .map(String::trim)
              .forEach(config::addExposedHeader);

        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
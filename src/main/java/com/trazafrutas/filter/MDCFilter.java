package com.trazafrutas.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            // Agregar información útil al contexto de logging
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("ip", request.getRemoteAddr());
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("userAgent", request.getHeader("User-Agent"));

            // Si hay un usuario autenticado, agregar su información
            String username = request.getUserPrincipal() != null ? 
                            request.getUserPrincipal().getName() : "anonymous";
            MDC.put("user", username);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
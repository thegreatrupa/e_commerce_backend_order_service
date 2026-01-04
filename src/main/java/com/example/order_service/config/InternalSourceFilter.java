package com.example.order_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class InternalSourceFilter extends OncePerRequestFilter {

    private final String internalKey;

    public InternalSourceFilter(String internalKey) {
        this.internalKey = internalKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String incomingKey = request.getHeader("X-Internal-Source");

        if (internalKey != null && internalKey.equals(incomingKey)) {
            filterChain.doFilter(request, response); // Trusted source, continue
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Direct access forbidden. Access only via API Gateway.\"}");
    }
}
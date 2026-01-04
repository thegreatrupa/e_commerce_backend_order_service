package com.example.order_service.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

public class InternalHeaderInterceptor implements ClientHttpRequestInterceptor {
    private final String internalKey;

    public InternalHeaderInterceptor(String internalKey) {
        this.internalKey = internalKey;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("X-Internal-Source", internalKey);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String userId = attributes.getRequest().getHeader("X-User-Id");
            String userEmail = attributes.getRequest().getHeader("X-User-Email");

            if (userId != null) request.getHeaders().set("X-User-Id", userId);
            if (userEmail != null) request.getHeaders().set("X-User-Email", userEmail);
        }

        return execution.execute(request, body);
    }
}
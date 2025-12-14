package com.example.order_service.config; // Assuming config package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        AuthTokenInterceptor interceptor = new AuthTokenInterceptor();
        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        return restTemplate;
    }
}
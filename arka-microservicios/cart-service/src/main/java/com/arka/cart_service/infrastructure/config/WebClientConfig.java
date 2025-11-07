package com.arka.cart_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.customer-service.url}")
    private String customerServiceUrl;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    @Bean
    public WebClient notificationServiceWebClient() {
        return WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}

package com.arka.cart_service.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient customerServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://customer-service")
                .build();
    }

    @Bean
    public WebClient productServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://product-service")
                .build();
    }

    @Bean
    public WebClient notificationServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://notification-service")
                .build();
    }
}

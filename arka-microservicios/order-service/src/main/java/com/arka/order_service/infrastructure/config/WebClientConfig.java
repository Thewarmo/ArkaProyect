package com.arka.order_service.infrastructure.config;

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

    @Bean(name = "inventoryWebClient")
    public WebClient inventoryWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://inventory-service")
                .build();
    }

    @Bean(name = "customerWebClient")
    public WebClient customerWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://customer-service")
                .build();
    }

    @Bean(name = "productWebClient")
    public WebClient productWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://product-service")
                .build();
    }
}

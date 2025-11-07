package com.arka.report_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    @Value("${services.customer-service.url}")
    private String customerServiceUrl;

    @Bean(name = "orderServiceWebClient")
    public WebClient orderServiceWebClient() {
        return WebClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    @Bean(name = "productServiceWebClient")
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    @Bean(name = "customerServiceWebClient")
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }
}

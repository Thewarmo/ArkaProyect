package com.arka.order_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.inventory-service.url:http://localhost:8084}")
    private String inventoryServiceUrl;

    @Value("${services.customer-service.url:http://localhost:8083}")
    private String customerServiceUrl;

    @Value("${services.product-service.url:http://localhost:8081}")
    private String productServiceUrl;

    @Bean(name = "inventoryWebClient")
    public WebClient inventoryWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    @Bean(name = "customerWebClient")
    public WebClient customerWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(customerServiceUrl)
                .build();
    }

    @Bean(name = "productWebClient")
    public WebClient productWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(productServiceUrl)
                .build();
    }
}

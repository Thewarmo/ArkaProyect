package com.arka.report_service.infrastructure.clients;

import com.arka.report_service.infrastructure.clients.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    @Qualifier("productServiceWebClient")
    private final WebClient productServiceWebClient;

    public ProductResponse getProductById(Long productId) {
        log.debug("Fetching product {} from Product Service", productId);
        try {
            return productServiceWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching product {} from Product Service: {}", productId, e.getMessage());
            return null; // Return null if product not found
        }
    }
}

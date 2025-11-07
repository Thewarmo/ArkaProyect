package com.arka.order_service.infrastructure.clients;

import com.arka.order_service.infrastructure.clients.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    @Qualifier("productWebClient")
    private final WebClient productWebClient;

    public ProductResponse getProductById(Long productId) {
        try {
            return productWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.error("Producto no encontrado con ID: {}", productId);
            throw new RuntimeException("Producto no encontrado con ID: " + productId);
        } catch (WebClientResponseException e) {
            log.error("Error al consultar producto {}: {}", productId, e.getMessage());
            throw new RuntimeException("No se pudo consultar el producto " + productId, e);
        }
    }

    public boolean productExists(Long productId) {
        try {
            ProductResponse product = getProductById(productId);
            return product != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia del producto {}: {}", productId, e.getMessage());
            return false;
        }
    }

    public boolean productIsActive(Long productId) {
        try {
            ProductResponse product = getProductById(productId);
            return product != null && product.isActive();
        } catch (Exception e) {
            log.error("Error al verificar si el producto {} está activo: {}", productId, e.getMessage());
            return false;
        }
    }
}

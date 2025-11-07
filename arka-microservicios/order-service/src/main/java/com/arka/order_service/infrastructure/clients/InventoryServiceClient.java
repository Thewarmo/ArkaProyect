package com.arka.order_service.infrastructure.clients;

import com.arka.order_service.infrastructure.clients.dto.InventoryResponse;
import com.arka.order_service.infrastructure.clients.dto.ReserveStockRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceClient {

    @Qualifier("inventoryWebClient")
    private final WebClient inventoryWebClient;

    public InventoryResponse getInventoryByProductId(Long productId) {
        try {
            return inventoryWebClient.get()
                    .uri("/api/v1/inventory/product/{productId}", productId)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error al consultar inventario del producto {}: {}", productId, e.getMessage());
            throw new RuntimeException("No se pudo consultar el inventario del producto " + productId, e);
        }
    }

    public void reserveStock(Long productId, Integer quantity) {
        try {
            ReserveStockRequest request = ReserveStockRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();

            inventoryWebClient.post()
                    .uri("/api/v1/inventory/reserve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Stock reservado exitosamente: Producto={}, Cantidad={}", productId, quantity);
        } catch (WebClientResponseException e) {
            log.error("Error al reservar stock del producto {}: {}", productId, e.getMessage());
            throw new RuntimeException("No se pudo reservar el stock del producto " + productId + ": " + e.getResponseBodyAsString(), e);
        }
    }

    public void releaseStock(Long productId, Integer quantity) {
        try {
            ReserveStockRequest request = ReserveStockRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();

            inventoryWebClient.post()
                    .uri("/api/v1/inventory/release")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Stock liberado exitosamente: Producto={}, Cantidad={}", productId, quantity);
        } catch (WebClientResponseException e) {
            log.error("Error al liberar stock del producto {}: {}", productId, e.getMessage());
            throw new RuntimeException("No se pudo liberar el stock del producto " + productId + ": " + e.getResponseBodyAsString(), e);
        }
    }

    public boolean hasAvailableStock(Long productId, Integer quantity) {
        try {
            InventoryResponse inventory = getInventoryByProductId(productId);
            return inventory != null && inventory.getAvailableStock() >= quantity;
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de stock del producto {}: {}", productId, e.getMessage());
            return false;
        }
    }
}
